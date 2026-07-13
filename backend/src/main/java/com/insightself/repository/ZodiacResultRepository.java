package com.insightself.repository;

import com.insightself.domain.ZodiacResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ZodiacResultRepository extends JpaRepository<ZodiacResult, Long> {
    Optional<ZodiacResult> findByUserIdAndInsightDate(Long userId, LocalDate insightDate);
}
