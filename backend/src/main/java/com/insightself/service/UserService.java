package com.insightself.service;

import com.insightself.common.ApiException;
import com.insightself.domain.AuthSession;
import com.insightself.domain.User;
import com.insightself.dto.AuthRequest;
import com.insightself.dto.ChangePasswordRequest;
import com.insightself.dto.RefreshTokenRequest;
import com.insightself.dto.UserResponse;
import com.insightself.repository.AuthSessionRepository;
import com.insightself.repository.UserRepository;
import com.insightself.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.regex.Pattern;

@Service
public class UserService {
    private static final int TOKEN_BYTES = 32;
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final Duration ACCESS_TOKEN_TTL = Duration.ofHours(12);
    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(30);
    private static final Pattern LEGACY_SHA256_HEX = Pattern.compile("[0-9a-f]{64}");

    private final UserRepository userRepository;
    private final AuthSessionRepository authSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public UserService(
            UserRepository userRepository,
            AuthSessionRepository authSessionRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.authSessionRepository = authSessionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse register(AuthRequest request) {
        validateAuth(request);
        String username = request.username().trim();
        if (userRepository.existsByUsername(username)) {
            throw new ApiException(HttpStatus.CONFLICT, "username already exists");
        }
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setCreatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);
        return toResponse(savedUser, createSession(savedUser));
    }

    @Transactional
    public UserResponse login(AuthRequest request) {
        validateAuth(request);
        User user = userRepository.findByUsername(request.username().trim())
                .orElseThrow(() -> invalidCredentials());
        if (!passwordMatchesAndUpgradeIfNeeded(user, request.password())) {
            throw invalidCredentials();
        }
        return toResponse(user, createSession(user));
    }

    @Transactional
    public UserResponse refresh(RefreshTokenRequest request) {
        if (request == null || blank(request.refreshToken())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "refresh token is required");
        }
        AuthSession existingSession = authSessionRepository.findByRefreshTokenHash(sha256Hex(request.refreshToken()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "session expired"));
        if (isRevokedOrExpired(existingSession.getRevokedAt(), existingSession.getRefreshTokenExpiresAt())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "session expired");
        }
        User user = userRepository.findById(existingSession.getUserId())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "session user not found"));

        // Refresh-token rotation makes replay visible and lets the server revoke a stolen refresh token.
        existingSession.setRevokedAt(Instant.now());
        authSessionRepository.save(existingSession);
        return toResponse(user, createSession(user));
    }

    @Transactional
    public void logout(Long sessionId) {
        AuthSession session = authSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "session not found"));
        if (session.getRevokedAt() == null) {
            session.setRevokedAt(Instant.now());
            authSessionRepository.save(session);
        }
    }

    @Transactional
    public AuthenticatedUser authenticateAccessToken(String accessToken) {
        AuthSession session = authSessionRepository.findByAccessTokenHash(sha256Hex(accessToken))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "session expired"));
        if (isRevokedOrExpired(session.getRevokedAt(), session.getAccessTokenExpiresAt())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "session expired");
        }
        User user = userRepository.findById(session.getUserId())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "session user not found"));
        session.setLastUsedAt(Instant.now());
        authSessionRepository.save(session);
        return new AuthenticatedUser(user.getId(), user.getUsername(), session.getId());
    }

    public void requireUser(Long userId) {
        if (userId == null || !userRepository.existsById(userId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "user not found");
        }
    }

    // 修改密码
    @Transactional
    public boolean changePassword(Long userId, ChangePasswordRequest request, Long currentSessionId) {
        validateChangePassword(request);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        // 兼容旧版 SHA-256 密码摘要，并在验证成功后迁移到 Spring Security 的自适应密码哈希。
        if (!passwordMatchesAndUpgradeIfNeeded(user, request.getOldPassword())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Old password is incorrect");
        }

        // 设置新密码
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        revokeOtherSessions(user.getId(), currentSessionId);
        return true;
    }

    private void validateAuth(AuthRequest request) {
        if (request == null || blank(request.username()) || blank(request.password())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "username and password are required");
        }
        if (request.password().length() < MIN_PASSWORD_LENGTH) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "password must be at least 8 characters");
        }
    }

    private void validateChangePassword(ChangePasswordRequest request) {
        if (request == null || blank(request.getOldPassword()) || blank(request.getNewPassword())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "old password and new password are required");
        }
        if (request.getNewPassword().length() < MIN_PASSWORD_LENGTH) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "password must be at least 8 characters");
        }
    }

    private boolean passwordMatchesAndUpgradeIfNeeded(User user, String rawPassword) {
        String storedHash = user.getPasswordHash();
        if (LEGACY_SHA256_HEX.matcher(storedHash).matches()) {
            boolean matched = storedHash.equals(sha256Hex(rawPassword));
            if (matched) {
                user.setPasswordHash(passwordEncoder.encode(rawPassword));
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);
            }
            return matched;
        }
        return passwordEncoder.matches(rawPassword, storedHash);
    }

    private void revokeOtherSessions(Long userId, Long currentSessionId) {
        Instant now = Instant.now();
        for (AuthSession session : authSessionRepository.findByUserIdAndRevokedAtIsNull(userId)) {
            if (!session.getId().equals(currentSessionId)) {
                session.setRevokedAt(now);
                authSessionRepository.save(session);
            }
        }
    }

    private AuthSessionTokenPair createSession(User user) {
        Instant now = Instant.now();
        String accessToken = generateToken();
        String refreshToken = generateToken();
        AuthSession session = new AuthSession();
        session.setUserId(user.getId());
        session.setAccessTokenHash(sha256Hex(accessToken));
        session.setRefreshTokenHash(sha256Hex(refreshToken));
        session.setAccessTokenExpiresAt(now.plus(ACCESS_TOKEN_TTL));
        session.setRefreshTokenExpiresAt(now.plus(REFRESH_TOKEN_TTL));
        session.setCreatedAt(now);
        authSessionRepository.save(session);
        return new AuthSessionTokenPair(
                accessToken,
                refreshToken,
                session.getAccessTokenExpiresAt(),
                session.getRefreshTokenExpiresAt()
        );
    }

    private String generateToken() {
        byte[] tokenBytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    private boolean isRevokedOrExpired(Instant revokedAt, Instant expiresAt) {
        return revokedAt != null || !expiresAt.isAfter(Instant.now());
    }

    private boolean blank(String value) {
        return value == null || value.trim().isBlank();
    }

    private UserResponse toResponse(User user, AuthSessionTokenPair tokenPair) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getCreatedAt(),
                tokenPair.accessToken(),
                tokenPair.refreshToken(),
                tokenPair.accessTokenExpiresAt(),
                tokenPair.refreshTokenExpiresAt()
        );
    }

    private ApiException invalidCredentials() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "username or password incorrect");
    }

    private String sha256Hex(String raw) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is required by the JDK", ex);
        }
    }

    private record AuthSessionTokenPair(
            String accessToken,
            String refreshToken,
            Instant accessTokenExpiresAt,
            Instant refreshTokenExpiresAt
    ) {
    }
}
