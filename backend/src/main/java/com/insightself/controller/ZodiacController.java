package com.insightself.controller;

import com.insightself.common.ApiResponse;
import com.insightself.domain.ZodiacResult;
import com.insightself.dto.ZodiacMatchRequest;
import com.insightself.dto.ZodiacMatchResponse;
import com.insightself.security.CurrentUserService;
import com.insightself.service.AstrologyChartCalculator;
import com.insightself.service.ZodiacService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/zodiac")
public class ZodiacController {
    private final ZodiacService zodiacService;
    private final CurrentUserService currentUserService;

    public ZodiacController(ZodiacService zodiacService, CurrentUserService currentUserService) {
        this.zodiacService = zodiacService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/daily/{userId}")
    public ApiResponse<ZodiacResult> daily(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        currentUserService.requireUser(userId);
        return ApiResponse.ok(date == null ? zodiacService.daily(userId) : zodiacService.daily(userId, date));
    }

    @GetMapping("/natal/{userId}")
    public ApiResponse<AstrologyChartCalculator.NatalChart> natal(@PathVariable Long userId) {
        currentUserService.requireUser(userId);
        return ApiResponse.ok(zodiacService.natal(userId));
    }

    @PostMapping("/match")
    public ApiResponse<ZodiacMatchResponse> match(@RequestBody ZodiacMatchRequest request) {
        currentUserService.requireUser(request.userId());
        return ApiResponse.ok(zodiacService.match(request));
    }
}
