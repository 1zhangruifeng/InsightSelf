package com.example.insightself.data.repository

import com.example.insightself.data.api.ApiResponse
import com.example.insightself.data.model.UserDto

interface AuthGateway {
    suspend fun register(username: String, password: String): ApiResponse<UserDto>

    suspend fun login(username: String, password: String): ApiResponse<UserDto>

    suspend fun seedDemo(): ApiResponse<UserDto>
}
