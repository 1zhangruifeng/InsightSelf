package com.insightself.repository;

import com.insightself.domain.AssessmentQuestion;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;

public interface AssessmentQuestionRepository extends JpaRepository<AssessmentQuestion, Long> {
    boolean existsByType(String type);
    List<AssessmentQuestion> findByTypeOrderByDisplayOrderAsc(String type);
    long countByType(String type);
    @Modifying
    @Transactional
    void deleteByType(String type);
}
