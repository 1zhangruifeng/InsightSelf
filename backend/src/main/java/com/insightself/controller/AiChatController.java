package com.insightself.controller;

import com.insightself.common.ApiResponse;
import com.insightself.dto.AiChatRequest;
import com.insightself.dto.AiChatResponse;
import com.insightself.security.CurrentUserService;
import com.insightself.service.AiChatService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiChatController {
    private final AiChatService aiChatService;
    private final CurrentUserService currentUserService;

    public AiChatController(AiChatService aiChatService, CurrentUserService currentUserService) {
        this.aiChatService = aiChatService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/chat")
    public ApiResponse<AiChatResponse> chat(@RequestBody AiChatRequest request) {
        currentUserService.requireUser(request.getUserId());
        return ApiResponse.ok(aiChatService.chat(request));
    }
}
