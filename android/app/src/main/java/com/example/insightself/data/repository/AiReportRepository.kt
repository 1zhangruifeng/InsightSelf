package com.example.insightself.data.repository

import com.example.insightself.data.api.ApiResponse
import com.example.insightself.data.api.ApiService
import com.example.insightself.data.api.RetrofitClient
import com.example.insightself.data.model.AiReportDto

class AiReportRepository(
    private val api: ApiService = RetrofitClient.apiService
) {
    suspend fun generate(userId: Long): ApiResponse<AiReportDto> {
        return api.generateAiReport(userId)
    }

    suspend fun latest(userId: Long): ApiResponse<AiReportDto> {
        return api.latestAiReport(userId)
    }
}
