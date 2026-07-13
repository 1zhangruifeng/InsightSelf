package com.insightself.dto;

import java.time.LocalDateTime;
import java.time.Instant;

public record UserResponse(
        Long userId,
        String username,
        LocalDateTime createdAt,
        String accessToken,
        String refreshToken,
        Instant accessTokenExpiresAt,
        Instant refreshTokenExpiresAt
) {
}
