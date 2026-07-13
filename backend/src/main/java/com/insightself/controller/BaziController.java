package com.insightself.controller;

import com.insightself.common.ApiResponse;
import com.insightself.domain.BaziResult;
import com.insightself.security.CurrentUserService;
import com.insightself.service.BaziService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bazi")
public class BaziController {
    private final BaziService baziService;
    private final CurrentUserService currentUserService;

    public BaziController(BaziService baziService, CurrentUserService currentUserService) {
        this.baziService = baziService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/generate/{userId}")
    public ApiResponse<BaziResult> generate(@PathVariable Long userId) {
        currentUserService.requireUser(userId);
        return ApiResponse.ok(baziService.generate(userId));
    }

    @GetMapping("/latest/{userId}")
    public ApiResponse<BaziResult> latest(@PathVariable Long userId) {
        currentUserService.requireUser(userId);
        return ApiResponse.ok(baziService.latest(userId));
    }
}
