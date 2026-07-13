package com.insightself.repository;

import com.insightself.domain.AiChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiChatMessageRepository extends JpaRepository<AiChatMessage, Long> {
    List<AiChatMessage> findTop12BySessionIdOrderByIdDesc(String sessionId);
    long countBySessionId(String sessionId);
}
