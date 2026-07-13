package com.insightself.repository;

import com.insightself.domain.AiChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiChatSessionRepository extends JpaRepository<AiChatSession, Long> {
    Optional<AiChatSession> findBySessionIdAndUserId(String sessionId, Long userId);
}
