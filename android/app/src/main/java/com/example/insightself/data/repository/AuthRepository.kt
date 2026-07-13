package com.example.insightself.data.repository

import com.example.insightself.data.api.ApiResponse
import com.example.insightself.data.api.ApiService
import com.example.insightself.data.api.RetrofitClient
import com.example.insightself.data.model.AuthRequest
import com.example.insightself.data.model.HealthDto
import com.example.insightself.data.model.UserDto
import com.example.insightself.data.api.RefreshTokenRequest

class AuthRepository(
    private val api: ApiService = RetrofitClient.apiService
) : AuthGateway {
    suspend fun health(): ApiResponse<HealthDto> = api.health()

    override suspend fun register(username: String, password: String): ApiResponse<UserDto> {
        return api.register(AuthRequest(username = username, password = password))
    }

    override suspend fun login(username: String, password: String): ApiResponse<UserDto> {
        return api.login(AuthRequest(username = username, password = password))
    }

    override suspend fun seedDemo(): ApiResponse<UserDto> {
        val response = api.seedDemo()
        return ApiResponse(
            success = response.success,
            message = response.message,
            data = response.data?.user
        )
    }

    suspend fun refresh(refreshToken: String): ApiResponse<UserDto> {
        return api.refresh(RefreshTokenRequest(refreshToken = refreshToken))
    }

    suspend fun logout(): ApiResponse<Boolean> = api.logout()
}
