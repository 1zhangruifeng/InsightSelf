package com.example.insightself.data.repository

import com.example.insightself.data.api.ApiResponse
import com.example.insightself.data.api.ApiService
import com.example.insightself.data.api.RetrofitClient
import com.example.insightself.data.model.ZodiacDailyDto
import com.example.insightself.data.model.ZodiacMatchRequest
import com.example.insightself.data.model.ZodiacMatchResultDto
import com.example.insightself.data.model.ZodiacNatalDto

class ZodiacRepository(
    private val api: ApiService = RetrofitClient.apiService
) {
    // 原方法：获取今日运势（保持兼容）
    suspend fun daily(userId: Long): ApiResponse<ZodiacDailyDto> {
        return api.zodiacDaily(userId)
    }

    // 新增方法：获取指定日期的运势
    suspend fun daily(userId: Long, date: String): ApiResponse<ZodiacDailyDto> {
        return api.zodiacDaily(userId, date)
    }

    suspend fun natal(userId: Long): ApiResponse<ZodiacNatalDto> {
        return api.zodiacNatal(userId)
    }

    suspend fun match(request: ZodiacMatchRequest): ApiResponse<ZodiacMatchResultDto> {
        return api.zodiacMatch(request)
    }
}