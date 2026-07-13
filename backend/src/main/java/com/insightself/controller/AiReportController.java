package com.insightself.controller;

import com.insightself.common.ApiResponse;
import com.insightself.dto.AiReportResponse;
import com.insightself.security.CurrentUserService;
import com.insightself.service.AiReportService;
import com.insightself.service.ProfileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai-reports")
public class AiReportController {
    private final AiReportService aiReportService;
    private final CurrentUserService currentUserService;
    private final ProfileService profileService;

    public AiReportController(
            AiReportService aiReportService,
            CurrentUserService currentUserService,
            ProfileService profileService
    ) {
        this.aiReportService = aiReportService;
        this.currentUserService = currentUserService;
        this.profileService = profileService;
    }

    @PostMapping("/generate/{userId}")
    public ApiResponse<AiReportResponse> generate(@PathVariable Long userId) {
        currentUserService.requireUser(userId);
        String language = profileService.findOptional(userId).map(profile -> profile.getLanguage()).orElse("en");
        return ApiResponse.ok(AiReportResponse.from(aiReportService.generate(userId), language));
    }

    @GetMapping("/latest/{userId}")
    public ApiResponse<AiReportResponse> latest(@PathVariable Long userId) {
        currentUserService.requireUser(userId);
        String language = profileService.findOptional(userId).map(profile -> profile.getLanguage()).orElse("en");
        return ApiResponse.ok(AiReportResponse.from(aiReportService.latest(userId), language));
    }
}
