package com.example.insightself.data.repository

import com.example.insightself.data.api.ApiResponse
import com.example.insightself.data.model.ProfileDto

interface ProfileGateway {
    suspend fun getProfile(userId: Long): ApiResponse<ProfileDto>
}
