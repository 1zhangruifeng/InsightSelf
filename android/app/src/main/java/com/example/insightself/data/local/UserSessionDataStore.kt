package com.example.insightself.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.insightself.data.api.AuthTokenStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.userSessionDataStore by preferencesDataStore(name = "user_session")

data class UserSession(
    val userId: Long?,
    val username: String?,
    val accessToken: String?,
    val refreshToken: String?,
    val accessTokenExpiresAt: String?,
    val refreshTokenExpiresAt: String?,
    val isLoggedIn: Boolean
)

class UserSessionDataStore(
    private val context: Context
) : UserSessionStore {
    private object Keys {
        val USER_ID = longPreferencesKey("user_id")
        val USERNAME = stringPreferencesKey("username")
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val ACCESS_TOKEN_EXPIRES_AT = stringPreferencesKey("access_token_expires_at")
        val REFRESH_TOKEN_EXPIRES_AT = stringPreferencesKey("refresh_token_expires_at")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    }

    override suspend fun saveSession(
        userId: Long,
        username: String,
        accessToken: String,
        refreshToken: String,
        accessTokenExpiresAt: String?,
        refreshTokenExpiresAt: String?
    ) {
        context.userSessionDataStore.edit { preferences ->
            preferences[Keys.USER_ID] = userId
            preferences[Keys.USERNAME] = username
            preferences[Keys.ACCESS_TOKEN] = accessToken
            preferences[Keys.REFRESH_TOKEN] = refreshToken
            accessTokenExpiresAt?.let { preferences[Keys.ACCESS_TOKEN_EXPIRES_AT] = it }
            refreshTokenExpiresAt?.let { preferences[Keys.REFRESH_TOKEN_EXPIRES_AT] = it }
            preferences[Keys.IS_LOGGED_IN] = true
        }
        AuthTokenStore.accessToken = accessToken
    }

    override suspend fun clearSession() {
        context.userSessionDataStore.edit { preferences ->
            preferences.clear()
        }
        AuthTokenStore.accessToken = null
    }

    override fun observeSession(): Flow<UserSession> {
        return context.userSessionDataStore.data.map { preferences ->
            UserSession(
                userId = preferences[Keys.USER_ID],
                username = preferences[Keys.USERNAME],
                accessToken = preferences[Keys.ACCESS_TOKEN],
                refreshToken = preferences[Keys.REFRESH_TOKEN],
                accessTokenExpiresAt = preferences[Keys.ACCESS_TOKEN_EXPIRES_AT],
                refreshTokenExpiresAt = preferences[Keys.REFRESH_TOKEN_EXPIRES_AT],
                isLoggedIn = preferences[Keys.IS_LOGGED_IN] ?: false
            )
        }
    }

    override suspend fun getCurrentUserId(): Long? {
        return observeSession().first().userId
    }
}
