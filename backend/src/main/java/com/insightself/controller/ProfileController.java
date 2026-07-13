package com.insightself.controller;

import com.insightself.common.ApiResponse;
import com.insightself.domain.UserProfile;
import com.insightself.dto.ProfileRequest;
import com.insightself.security.CurrentUserService;
import com.insightself.service.ProfileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {
    private final ProfileService profileService;
    private final CurrentUserService currentUserService;

    public ProfileController(ProfileService profileService, CurrentUserService currentUserService) {
        this.profileService = profileService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/{userId}")
    public ApiResponse<UserProfile> create(@PathVariable Long userId, @RequestBody ProfileRequest request) {
        currentUserService.requireUser(userId);
        return ApiResponse.ok(profileService.createOrReplace(userId, request));
    }

    @GetMapping("/{userId}")
    public ApiResponse<UserProfile> get(@PathVariable Long userId) {
        currentUserService.requireUser(userId);
        return ApiResponse.ok(profileService.get(userId));
    }

    @PutMapping("/{userId}")
    public ApiResponse<UserProfile> update(@PathVariable Long userId, @RequestBody ProfileRequest request) {
        currentUserService.requireUser(userId);
        return ApiResponse.ok(profileService.update(userId, request));
    }
}
