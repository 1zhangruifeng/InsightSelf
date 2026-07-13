package com.example.insightself.data.api

import com.example.insightself.data.model.AiReportDto
import com.example.insightself.data.model.AssessmentQuestionDto
import com.example.insightself.data.model.AssessmentResultDto
import com.example.insightself.data.model.AssessmentSubmitRequest
import com.example.insightself.data.model.AuthRequest
import com.example.insightself.data.model.BaziDto
import com.example.insightself.data.model.DashboardDto
import com.example.insightself.data.model.DemoSeedDto
import com.example.insightself.data.model.HealthDto
import com.example.insightself.data.model.ProfileDto
import com.example.insightself.data.model.UserDto
import com.example.insightself.data.model.ZodiacDailyDto
import com.example.insightself.data.model.ZodiacNatalDto
import com.example.insightself.data.model.ZodiacMatchRequest
import com.example.insightself.data.model.ZodiacMatchResultDto
import com.example.insightself.data.model.AiChatRequest
import com.example.insightself.data.model.AiChatResponse
import com.example.insightself.data.model.ChangePasswordRequest
import retrofit2.http.*

interface ApiService {
    @GET("api/health")
    suspend fun health(): ApiResponse<HealthDto>

    @POST("api/users/register")
    suspend fun register(@Body request: AuthRequest): ApiResponse<UserDto>

    @POST("api/users/login")
    suspend fun login(@Body request: AuthRequest): ApiResponse<UserDto>

    @POST("api/users/refresh")
    suspend fun refresh(@Body request: RefreshTokenRequest): ApiResponse<UserDto>

    @POST("api/demo/seed")
    suspend fun seedDemo(): ApiResponse<DemoSeedDto>

    @POST("api/users/logout")
    suspend fun logout(): ApiResponse<Boolean>

    @POST("api/profiles/{userId}")
    suspend fun createProfile(
        @Path("userId") userId: Long,
        @Body request: ProfileDto
    ): ApiResponse<ProfileDto>

    @GET("api/profiles/{userId}")
    suspend fun getProfile(@Path("userId") userId: Long): ApiResponse<ProfileDto>

    @PUT("api/profiles/{userId}")
    suspend fun updateProfile(
        @Path("userId") userId: Long,
        @Body request: ProfileDto
    ): ApiResponse<ProfileDto>

    @POST("api/bazi/generate/{userId}")
    suspend fun generateBazi(@Path("userId") userId: Long): ApiResponse<BaziDto>

    @GET("api/bazi/latest/{userId}")
    suspend fun latestBazi(@Path("userId") userId: Long): ApiResponse<BaziDto>

    // 原有方法：获取今日运势（保留兼容）
    @GET("api/zodiac/daily/{userId}")
    suspend fun zodiacDaily(@Path("userId") userId: Long): ApiResponse<ZodiacDailyDto>

    // 新增方法：获取指定日期的运势
    @GET("api/zodiac/daily/{userId}")
    suspend fun zodiacDaily(
        @Path("userId") userId: Long,
        @Query("date") date: String
    ): ApiResponse<ZodiacDailyDto>

    @GET("api/zodiac/natal/{userId}")
    suspend fun zodiacNatal(@Path("userId") userId: Long): ApiResponse<ZodiacNatalDto>

    @POST("api/zodiac/match")
    suspend fun zodiacMatch(@Body request: ZodiacMatchRequest): ApiResponse<ZodiacMatchResultDto>

    @GET("api/assessments/types")
    suspend fun assessmentTypes(): ApiResponse<List<String>>

    @GET("api/assessments/types/{userId}")
    suspend fun assessmentTypesWithLanguage(@Path("userId") userId: Long): ApiResponse<List<AssessmentTypeInfo>>

    @GET("api/assessments/{type}/questions")
    suspend fun assessmentQuestions(@Path("type") type: String): ApiResponse<List<AssessmentQuestionDto>>

    @GET("api/assessments/{type}/questions/{userId}")
    suspend fun assessmentQuestionsWithLanguage(
        @Path("type") type: String,
        @Path("userId") userId: Long
    ): ApiResponse<List<AssessmentQuestionDto>>

    @POST("api/assessments/{type}/submit")
    suspend fun submitAssessment(
        @Path("type") type: String,
        @Body request: AssessmentSubmitRequest
    ): ApiResponse<AssessmentResultDto>

    @GET("api/assessments/results/{userId}")
    suspend fun assessmentResults(@Path("userId") userId: Long): ApiResponse<List<AssessmentResultDto>>

    @GET("api/dashboard/{userId}")
    suspend fun dashboard(@Path("userId") userId: Long): ApiResponse<DashboardDto>

    @POST("api/ai-reports/generate/{userId}")
    suspend fun generateAiReport(@Path("userId") userId: Long): ApiResponse<AiReportDto>

    @GET("api/ai-reports/latest/{userId}")
    suspend fun latestAiReport(@Path("userId") userId: Long): ApiResponse<AiReportDto>

    @POST("api/ai/chat")
    suspend fun aiChat(
        @Body request: AiChatRequest
    ): ApiResponse<AiChatResponse>

    @PUT("api/users/{userId}/password")
    suspend fun changePassword(
        @Path("userId") userId: Long,
        @Body request: ChangePasswordRequest
    ): ApiResponse<Boolean>

}

// 添加 AssessmentTypeInfo 数据类
data class AssessmentTypeInfo(
    val type: String,
    val displayName: String,
    val description: String
)

data class RefreshTokenRequest(
    val refreshToken: String
)
