package com.insightself.controller;

import com.insightself.common.ApiResponse;
import com.insightself.dto.AuthRequest;
import com.insightself.dto.ChangePasswordRequest;
import com.insightself.dto.RefreshTokenRequest;
import com.insightself.dto.UserResponse;
import com.insightself.security.AuthenticatedUser;
import com.insightself.security.CurrentUserService;
import com.insightself.service.UserService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final CurrentUserService currentUserService;

    public UserController(UserService userService, CurrentUserService currentUserService) {
        this.userService = userService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@RequestBody AuthRequest request) {
        return ApiResponse.ok("registered", userService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<UserResponse> login(@RequestBody AuthRequest request) {
        return ApiResponse.ok("login ok", userService.login(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<UserResponse> refresh(@RequestBody RefreshTokenRequest request) {
        return ApiResponse.ok("refreshed", userService.refresh(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Boolean> logout() {
        AuthenticatedUser user = currentUserService.requireAuthenticatedUser();
        userService.logout(user.sessionId());
        return ApiResponse.ok(true);
    }

    @PutMapping("/{userId}/password")
    public ApiResponse<Boolean> changePassword(
            @PathVariable Long userId,
            @RequestBody ChangePasswordRequest request
    ) {
        AuthenticatedUser user = currentUserService.requireAuthenticatedUser();
        currentUserService.requireUser(userId);
        return ApiResponse.ok(userService.changePassword(userId, request, user.sessionId()));
    }
}
