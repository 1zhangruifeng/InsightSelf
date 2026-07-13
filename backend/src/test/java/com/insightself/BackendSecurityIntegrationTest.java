package com.insightself;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insightself.domain.AuthSession;
import com.insightself.domain.User;
import com.insightself.repository.AuthSessionRepository;
import com.insightself.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Map;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "QWEN_API_KEY=")
@AutoConfigureMockMvc
class BackendSecurityIntegrationTest {
    private static final Pattern SHA256_HEX = Pattern.compile("[0-9a-f]{64}");

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AuthSessionRepository authSessionRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    void registerStoresAdaptivePasswordHashAndOnlyTokenDigests() throws Exception {
        AuthFixture fixture = register("secure-register");

        User user = userRepository.findById(fixture.userId()).orElseThrow();
        assertThat(user.getPasswordHash()).startsWith("{bcrypt}");
        assertThat(user.getPasswordHash()).doesNotContain("pass1234");
        assertThat(passwordEncoder.matches("pass1234", user.getPasswordHash())).isTrue();

        AuthSession session = authSessionRepository.findByUserIdAndRevokedAtIsNull(fixture.userId())
                .stream()
                .findFirst()
                .orElseThrow();
        assertThat(session.getAccessTokenHash()).matches(SHA256_HEX);
        assertThat(session.getRefreshTokenHash()).matches(SHA256_HEX);
        assertThat(session.getAccessTokenHash()).isNotEqualTo(fixture.accessToken());
        assertThat(session.getRefreshTokenHash()).isNotEqualTo(fixture.refreshToken());
        assertThat(session.getAccessTokenExpiresAt()).isBefore(session.getRefreshTokenExpiresAt());
    }

    @Test
    void legacySha256PasswordHashMigratesAfterSuccessfulLogin() throws Exception {
        String username = unique("legacy");
        User legacyUser = new User();
        legacyUser.setUsername(username);
        legacyUser.setPasswordHash(sha256("legacy-pass123"));
        legacyUser.setCreatedAt(LocalDateTime.now());
        userRepository.save(legacyUser);

        JsonNode login = postJson("/api/users/login", Map.of(
                "username", username,
                "password", "legacy-pass123"
        ));

        assertThat(login.path("success").asBoolean()).isTrue();
        assertThat(login.path("data").path("accessToken").asText()).isNotBlank();
        User migrated = userRepository.findByUsername(username).orElseThrow();
        assertThat(migrated.getPasswordHash()).startsWith("{bcrypt}");
        assertThat(passwordEncoder.matches("legacy-pass123", migrated.getPasswordHash())).isTrue();
    }

    @Test
    void refreshRotatesSessionAndRevokesOldAccessAndRefreshTokens() throws Exception {
        AuthFixture fixture = register("refresh");

        JsonNode refreshed = postJson("/api/users/refresh", Map.of("refreshToken", fixture.refreshToken()));
        AuthFixture rotated = AuthFixture.from(refreshed.path("data"));

        assertThat(rotated.userId()).isEqualTo(fixture.userId());
        assertThat(rotated.accessToken()).isNotEqualTo(fixture.accessToken());
        assertThat(rotated.refreshToken()).isNotEqualTo(fixture.refreshToken());

        mockMvc.perform(get("/api/assessments/types")
                        .header(HttpHeaders.AUTHORIZATION, bearer(fixture.accessToken())))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/users/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("refreshToken", fixture.refreshToken()))))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/assessments/types")
                        .header(HttpHeaders.AUTHORIZATION, bearer(rotated.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0]").value("BFI10"));
    }

    @Test
    void logoutRevokesCurrentAccessToken() throws Exception {
        AuthFixture fixture = register("logout");

        mockMvc.perform(get("/api/assessments/types")
                        .header(HttpHeaders.AUTHORIZATION, bearer(fixture.accessToken())))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/users/logout")
                        .header(HttpHeaders.AUTHORIZATION, bearer(fixture.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
        mockMvc.perform(get("/api/assessments/types")
                        .header(HttpHeaders.AUTHORIZATION, bearer(fixture.accessToken())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void passwordChangeKeepsCurrentSessionRevokesOtherSessionsAndRequiresNewPassword() throws Exception {
        AuthFixture firstSession = register("change-password");
        JsonNode secondLogin = postJson("/api/users/login", Map.of(
                "username", firstSession.username(),
                "password", "pass1234"
        ));
        AuthFixture secondSession = AuthFixture.from(secondLogin.path("data"), firstSession.username());

        mockMvc.perform(put("/api/users/{userId}/password", firstSession.userId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(firstSession.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("oldPassword", "pass1234", "newPassword", "newpass1234"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));

        mockMvc.perform(get("/api/assessments/types")
                        .header(HttpHeaders.AUTHORIZATION, bearer(firstSession.accessToken())))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/assessments/types")
                        .header(HttpHeaders.AUTHORIZATION, bearer(secondSession.accessToken())))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", firstSession.username(), "password", "pass1234"))))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", firstSession.username(), "password", "newpass1234"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    @Test
    void invalidAuthorizationHeadersFailClosed() throws Exception {
        mockMvc.perform(get("/api/assessments/types")
                        .header(HttpHeaders.AUTHORIZATION, "Token not-bearer"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/api/assessments/types")
                        .header(HttpHeaders.AUTHORIZATION, bearer("not-a-real-token")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void bodyUserIdMustMatchAuthenticatedUser() throws Exception {
        AuthFixture owner = register("body-owner");
        AuthFixture attacker = register("body-attacker");

        mockMvc.perform(post("/api/zodiac/match")
                        .header(HttpHeaders.AUTHORIZATION, bearer(attacker.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "userId", owner.userId(),
                                "targetNickname", "Jamie",
                                "targetBirthDate", "2002-02-14"
                        ))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    private AuthFixture register(String label) throws Exception {
        String username = unique(label);
        JsonNode node = postJson("/api/users/register", Map.of(
                "username", username,
                "password", "pass1234"
        ));
        return AuthFixture.from(node.path("data"), username);
    }

    private JsonNode postJson(String url, Object body) throws Exception {
        String content = mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(content);
    }

    private String unique(String label) {
        return label + "-" + System.nanoTime();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String json(Object body) throws Exception {
        return objectMapper.writeValueAsString(body);
    }

    private String sha256(String raw) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(raw.getBytes(StandardCharsets.UTF_8)));
    }

    private record AuthFixture(
            long userId,
            String username,
            String accessToken,
            String refreshToken
    ) {
        static AuthFixture from(JsonNode data) {
            return from(data, data.path("username").asText());
        }

        static AuthFixture from(JsonNode data, String username) {
            return new AuthFixture(
                    data.path("userId").asLong(),
                    username,
                    data.path("accessToken").asText(),
                    data.path("refreshToken").asText()
            );
        }
    }
}
