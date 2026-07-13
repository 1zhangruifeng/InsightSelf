package com.insightself.security;

public record AuthenticatedUser(Long userId, String username, Long sessionId) {
}
