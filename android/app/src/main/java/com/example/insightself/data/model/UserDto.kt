package com.example.insightself.data.model

data class AuthRequest(
    val username: String,
    val password: String
)

data class UserDto(
    val userId: Long,
    val username: String,
    val createdAt: String?,
    val accessToken: String?,
    val refreshToken: String?,
    val accessTokenExpiresAt: String?,
    val refreshTokenExpiresAt: String?
)

data class DemoSeedDto(
    val user: UserDto,
    val demoNote: String?,
    val seededProfile: String?,
    val seededAssessments: String?,
    val seededReport: String?
)

data class HealthDto(
    val status: String?,
    val service: String?,
    val time: String?
)
