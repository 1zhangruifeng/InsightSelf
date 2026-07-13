package com.example.insightself.data.local

import kotlinx.coroutines.flow.Flow

interface UserSessionStore {
    suspend fun saveSession(
        userId: Long,
        username: String,
        accessToken: String,
        refreshToken: String,
        accessTokenExpiresAt: String?,
        refreshTokenExpiresAt: String?
    )

    suspend fun clearSession()

    fun observeSession(): Flow<UserSession>

    suspend fun getCurrentUserId(): Long?
}
