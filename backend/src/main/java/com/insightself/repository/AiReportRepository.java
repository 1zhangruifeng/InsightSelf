package com.insightself.repository;

import com.insightself.domain.AiReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiReportRepository extends JpaRepository<AiReport, Long> {
    Optional<AiReport> findTopByUserIdOrderByCreatedAtDesc(Long userId);
}
