package com.insightself.controller;

import com.insightself.common.ApiResponse;
import com.insightself.domain.AssessmentQuestion;
import com.insightself.domain.AssessmentResult;
import com.insightself.dto.AssessmentSubmitRequest;
import com.insightself.dto.AssessmentQuestionDto;
import com.insightself.security.CurrentUserService;
import com.insightself.service.AssessmentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/assessments")
public class AssessmentController {
    private final AssessmentService assessmentService;
    private final CurrentUserService currentUserService;

    public AssessmentController(AssessmentService assessmentService, CurrentUserService currentUserService) {
        this.assessmentService = assessmentService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/types")
    public ApiResponse<List<String>> types() {
        return ApiResponse.ok(assessmentService.types());
    }

    @GetMapping("/types/{userId}")
    public ApiResponse<List<AssessmentService.AssessmentTypeInfo>> typesWithLanguage(@PathVariable Long userId) {
        currentUserService.requireUser(userId);
        return ApiResponse.ok(assessmentService.typesWithLanguage(userId));
    }

    @GetMapping("/{type}/questions")
    public ApiResponse<List<AssessmentQuestion>> questions(@PathVariable String type) {
        return ApiResponse.ok(assessmentService.questions(type));
    }
    
    @GetMapping("/{type}/questions/{userId}")
    public ApiResponse<List<AssessmentQuestionDto>> questionsWithLanguage(
            @PathVariable String type, 
            @PathVariable Long userId) {
        currentUserService.requireUser(userId);
        return ApiResponse.ok(assessmentService.questionsWithLanguage(type, userId));
    }

    @PostMapping("/{type}/submit")
    public ApiResponse<AssessmentResult> submit(@PathVariable String type, @RequestBody AssessmentSubmitRequest request) {
        currentUserService.requireUser(request.userId());
        return ApiResponse.ok(assessmentService.submit(type, request));
    }

    @GetMapping("/results/{userId}")
    public ApiResponse<List<AssessmentResult>> results(@PathVariable Long userId) {
        currentUserService.requireUser(userId);
        return ApiResponse.ok(assessmentService.results(userId));
    }
}
