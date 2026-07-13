package com.example.insightself.data.repository

import com.example.insightself.data.api.ApiResponse
import com.example.insightself.data.api.ApiService
import com.example.insightself.data.api.RetrofitClient
import com.example.insightself.data.model.DashboardDto

class DashboardRepository(
    private val api: ApiService = RetrofitClient.apiService
) {
    suspend fun loadDashboard(userId: Long): ApiResponse<DashboardDto> {
        return api.dashboard(userId)
    }
}
