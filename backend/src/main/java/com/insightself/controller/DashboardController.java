package com.insightself.controller;

import com.insightself.common.ApiResponse;
import com.insightself.dto.DashboardResponse;
import com.insightself.security.CurrentUserService;
import com.insightself.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;
    private final CurrentUserService currentUserService;

    public DashboardController(DashboardService dashboardService, CurrentUserService currentUserService) {
        this.dashboardService = dashboardService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/{userId}")
    public ApiResponse<DashboardResponse> dashboard(@PathVariable Long userId) {
        currentUserService.requireUser(userId);
        return ApiResponse.ok(dashboardService.load(userId));
    }
}
