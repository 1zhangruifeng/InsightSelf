package com.insightself.repository;

import com.insightself.domain.AssessmentResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssessmentResultRepository extends JpaRepository<AssessmentResult, Long> {
    List<AssessmentResult> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<AssessmentResult> findFirstByUserIdAndTypeOrderByCreatedAtDesc(Long userId, String type);
}
