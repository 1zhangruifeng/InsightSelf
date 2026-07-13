package com.example.insightself.data.auth

import android.content.Context
import com.example.insightself.data.api.AuthTokenStore
import com.example.insightself.data.local.UserSessionDataStore
import com.example.insightself.data.local.UserSessionStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import retrofit2.HttpException

/**
 * Keeps the in-memory bearer token aligned with persisted session data.
 * Activity recreate (e.g. language switch) clears [AuthTokenStore] but not DataStore.
 */
object SessionAuth {
    @Volatile
    private var sessionStore: UserSessionStore? = null

    fun init(context: Context) {
        if (sessionStore == null) {
            synchronized(this) {
                if (sessionStore == null) {
                    sessionStore = UserSessionDataStore(context.applicationContext)
                }
            }
        }
    }

    private fun store(): UserSessionStore {
        return sessionStore ?: error("SessionAuth.init(context) must be called before API requests")
    }

    suspend fun hydrateAccessToken(): String? {
        val session = store().observeSession().first()
        val token = session.accessToken?.takeIf { it.isNotBlank() }
        AuthTokenStore.accessToken = token
        return token
    }

    /**
     * Hydrates the bearer token from DataStore, then returns the logged-in user id.
     * Does not clear persisted session or memory token on failure (avoids accidental logouts).
     */
    suspend fun resolveLoggedInUserId(): Long? {
        hydrateAccessToken()
        val session = store().observeSession().first()
        if (!session.isLoggedIn || session.userId == null) return null
        val token = session.accessToken?.takeIf { it.isNotBlank() } ?: return null
        AuthTokenStore.accessToken = token
        return session.userId
    }

    suspend fun requireLoggedInUserId(): Long? = resolveLoggedInUserId()

    suspend fun clearSession() {
        store().clearSession()
        AuthTokenStore.accessToken = null
    }

    /** Synchronous token for OkHttp; hydrates from DataStore when memory cache is empty. */
    fun bearerTokenOrNull(): String? {
        AuthTokenStore.accessToken?.takeIf { it.isNotBlank() }?.let { return it }
        val store = sessionStore ?: return null
        return try {
            runBlocking { hydrateAccessToken() }
        } catch (_: Exception) {
            null
        }
    }

    fun isUnauthorized(error: Throwable): Boolean {
        return error is HttpException && error.code() == 401
    }
}
