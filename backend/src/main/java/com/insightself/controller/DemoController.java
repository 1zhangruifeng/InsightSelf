package com.insightself.controller;

import com.insightself.common.ApiResponse;
import com.insightself.dto.DemoSeedResponse;
import com.insightself.service.DemoSeedService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo")
public class DemoController {
    private final DemoSeedService demoSeedService;

    public DemoController(DemoSeedService demoSeedService) {
        this.demoSeedService = demoSeedService;
    }

    @PostMapping("/seed")
    public ApiResponse<DemoSeedResponse> seed() {
        return ApiResponse.ok("demo ready", demoSeedService.seed());
    }
}
