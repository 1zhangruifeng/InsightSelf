package com.example.insightself.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.insightself.data.api.AuthTokenStore
import com.example.insightself.data.auth.SessionAuth
import com.example.insightself.data.local.LanguageManager
import com.example.insightself.data.local.UserSessionDataStore
import com.example.insightself.data.local.UserSessionStore
import com.example.insightself.data.model.UserDto
import com.example.insightself.data.repository.AuthGateway
import com.example.insightself.data.repository.AuthRepository
import com.example.insightself.data.repository.ProfileGateway
import com.example.insightself.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

enum class AuthDestination {
    Login,
    Onboarding,
    Main
}

class AuthViewModel(
    private val authRepository: AuthGateway,
    private val profileRepository: ProfileGateway,
    private val sessionDataStore: UserSessionStore,
    private val syncLanguagePreference: suspend (String) -> Unit
) : ViewModel() {
    private val _startupState = MutableStateFlow<UiState<AuthDestination>>(UiState.Idle)
    val startupState: StateFlow<UiState<AuthDestination>> = _startupState

    private val _authState = MutableStateFlow<UiState<AuthDestination>>(UiState.Idle)
    val authState: StateFlow<UiState<AuthDestination>> = _authState

    fun routeFromSavedSession() {
        viewModelScope.launch {
            _startupState.value = UiState.Loading
            try {
                val session = sessionDataStore.observeSession().first()
                val userId = session.userId
                val accessToken = session.accessToken
                if (!session.isLoggedIn || userId == null || accessToken.isNullOrBlank()) {
                    sessionDataStore.clearSession()
                    _startupState.value = UiState.Success(AuthDestination.Login)
                    return@launch
                }
                AuthTokenStore.accessToken = accessToken
                _startupState.value = UiState.Success(destinationForProfile(userId))
            } catch (e: Exception) {
                _startupState.value = UiState.Error(messageFor(e))
            }
        }
    }

    fun login(username: String, password: String) {
        val trimmedUsername = username.trim()
        if (trimmedUsername.isBlank() || password.isBlank()) {
            _authState.value = UiState.Error("Username and password are required.")
            return
        }
        if (password.length < MIN_PASSWORD_LENGTH) {
            _authState.value = UiState.Error("Password must be at least 8 characters.")
            return
        }
        viewModelScope.launch {
            _authState.value = UiState.Loading
            try {
                val response = authRepository.login(trimmedUsername, password)
                val user = response.data
                if (!response.success || user == null) {
                    _authState.value = UiState.Error(response.message.ifBlank { "Login failed." })
                    return@launch
                }
                saveAuthenticatedSession(user)
                _authState.value = UiState.Success(destinationForProfile(user.userId))
            } catch (e: Exception) {
                _authState.value = UiState.Error(messageFor(e))
            }
        }
    }

    fun register(username: String, password: String) {
        val trimmedUsername = username.trim()
        if (trimmedUsername.isBlank() || password.isBlank()) {
            _authState.value = UiState.Error("Username and password are required.")
            return
        }
        if (password.length < MIN_PASSWORD_LENGTH) {
            _authState.value = UiState.Error("Password must be at least 8 characters.")
            return
        }
        viewModelScope.launch {
            _authState.value = UiState.Loading
            try {
                val response = authRepository.register(trimmedUsername, password)
                val user = response.data
                if (!response.success || user == null) {
                    _authState.value = UiState.Error(response.message.ifBlank { "Registration failed." })
                    return@launch
                }
                saveAuthenticatedSession(user)
                _authState.value = UiState.Success(AuthDestination.Onboarding)
            } catch (e: Exception) {
                _authState.value = UiState.Error(messageFor(e))
            }
        }
    }

    fun seedDemo() {
        viewModelScope.launch {
            _authState.value = UiState.Loading
            try {
                val response = authRepository.seedDemo()
                val user = response.data
                if (!response.success || user == null) {
                    _authState.value = UiState.Error(response.message.ifBlank { "Demo account is not available." })
                    return@launch
                }
                saveAuthenticatedSession(user)
                _authState.value = UiState.Success(AuthDestination.Main)
            } catch (e: Exception) {
                _authState.value = UiState.Error(messageFor(e))
            }
        }
    }

    fun resetAuthState() {
        _authState.value = UiState.Idle
    }

    private suspend fun saveAuthenticatedSession(user: UserDto) {
        val accessToken = user.accessToken ?: error("Backend did not return an access token.")
        val refreshToken = user.refreshToken ?: error("Backend did not return a refresh token.")
        sessionDataStore.saveSession(
            userId = user.userId,
            username = user.username,
            accessToken = accessToken,
            refreshToken = refreshToken,
            accessTokenExpiresAt = user.accessTokenExpiresAt,
            refreshTokenExpiresAt = user.refreshTokenExpiresAt
        )
        AuthTokenStore.accessToken = accessToken
    }

    private suspend fun destinationForProfile(userId: Long): AuthDestination {
        SessionAuth.hydrateAccessToken()
        return try {
            val response = profileRepository.getProfile(userId)
            if (response.success && response.data != null) {
                syncLanguagePreference(LanguageManager.normalizeLanguage(response.data.language))
                AuthDestination.Main
            } else {
                AuthDestination.Onboarding
            }
        } catch (e: HttpException) {
            when (e.code()) {
                401 -> {
                    sessionDataStore.clearSession()
                    AuthTokenStore.accessToken = null
                    throw e
                }
                404 -> AuthDestination.Onboarding
                else -> throw e
            }
        }
    }

    private fun messageFor(error: Exception): String {
        return when (error) {
            is HttpException -> when (error.code()) {
                400 -> "Password must be at least 8 characters."
                401 -> "Session expired or password incorrect."
                404 -> "Account or profile not found."
                409 -> "That username is already registered."
                else -> "Server error ${error.code()}. Please try again."
            }
            is IOException -> "Cannot reach the backend. Start the server and try again."
            else -> error.message ?: "Something went wrong. Please try again."
        }
    }

    companion object {
        private const val MIN_PASSWORD_LENGTH = 8

        fun factory(
            sessionDataStore: UserSessionDataStore,
            syncLanguagePreference: suspend (String) -> Unit = {}
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AuthViewModel(
                        authRepository = AuthRepository(),
                        profileRepository = ProfileRepository(),
                        sessionDataStore = sessionDataStore,
                        syncLanguagePreference = syncLanguagePreference
                    ) as T
                }
            }
        }
    }
}
