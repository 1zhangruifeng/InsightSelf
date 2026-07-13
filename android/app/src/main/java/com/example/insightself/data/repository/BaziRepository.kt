package com.example.insightself.data.repository

import com.example.insightself.data.api.ApiResponse
import com.example.insightself.data.api.ApiService
import com.example.insightself.data.api.RetrofitClient
import com.example.insightself.data.model.BaziDto

class BaziRepository(
    private val api: ApiService = RetrofitClient.apiService
) {
    suspend fun generate(userId: Long): ApiResponse<BaziDto> {
        return api.generateBazi(userId)
    }

    suspend fun latest(userId: Long): ApiResponse<BaziDto> {
        return api.latestBazi(userId)
    }
}
