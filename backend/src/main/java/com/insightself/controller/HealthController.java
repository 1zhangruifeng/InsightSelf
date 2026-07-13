package com.insightself.controller;

import com.insightself.common.ApiResponse;
import com.insightself.common.WellnessSafetyNotice;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {
    private final String qwenApiKey;
    private final String qwenModel;

    public HealthController(
            @Value("${QWEN_API_KEY:${qwen.api-key:}}") String qwenApiKey,
            @Value("${QWEN_MODEL:${qwen.model:qwen-plus}}") String qwenModel
    ) {
        this.qwenApiKey = qwenApiKey;
        this.qwenModel = qwenModel;
    }

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        return ApiResponse.ok(Map.of(
                "status", "UP",
                "service", "InsightSelf Backend",
                "time", LocalDateTime.now(),
                "aiProviderConfigured", qwenApiKey != null && !qwenApiKey.isBlank(),
                "aiModel", qwenModel,
                "fallbackMode", "TEMPLATE",
                "safetyNotice", WellnessSafetyNotice.english()
        ));
    }
}
