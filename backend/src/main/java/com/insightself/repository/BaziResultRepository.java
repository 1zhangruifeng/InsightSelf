package com.insightself.repository;

import com.insightself.domain.BaziResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BaziResultRepository extends JpaRepository<BaziResult, Long> {
    Optional<BaziResult> findTopByUserIdOrderByCreatedAtDesc(Long userId);
}
