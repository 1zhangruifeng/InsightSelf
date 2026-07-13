package com.example.insightself.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.insightself.data.auth.SessionAuth
import com.example.insightself.data.local.UserSessionDataStore
import com.example.insightself.data.model.DashboardDto
import com.example.insightself.data.repository.DashboardRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

enum class DashboardDestination {
    Onboarding
}

class DashboardViewModel(
    private val dashboardRepository: DashboardRepository,
    private val sessionDataStore: UserSessionDataStore
) : ViewModel() {
    private val _dashboardState = MutableStateFlow<UiState<DashboardDto>>(UiState.Idle)
    val dashboardState: StateFlow<UiState<DashboardDto>> = _dashboardState

    private val _destination = MutableStateFlow<DashboardDestination?>(null)
    val destination: StateFlow<DashboardDestination?> = _destination

    private var loadJob: Job? = null
    private var loadGeneration = 0

    fun loadDashboard(forceRefresh: Boolean = true) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val generation = ++loadGeneration
            _destination.value = null

            if (!forceRefresh && _dashboardState.value is UiState.Success) {
                return@launch
            }

            val userId = resolveUserIdOrNull()
            if (userId == null) {
                if (_dashboardState.value !is UiState.Success) {
                    _dashboardState.value = UiState.Error(sessionExpiredMessage())
                }
                return@launch
            }

            if (_dashboardState.value !is UiState.Success) {
                _dashboardState.value = UiState.Loading
            }
            loadDashboardForUser(userId, generation, retryOnUnauthorized = true)
        }
    }

    private suspend fun loadDashboardForUser(
        userId: Long,
        generation: Int,
        retryOnUnauthorized: Boolean
    ) {
        try {
            val response = dashboardRepository.loadDashboard(userId)
            if (generation != loadGeneration) return
            val dashboard = response.data
            if (response.success && dashboard != null) {
                _dashboardState.value = UiState.Success(dashboard)
            } else if (response.message.isProfileMissingMessage()) {
                _destination.value = DashboardDestination.Onboarding
            } else {
                _dashboardState.value = UiState.Error(response.message.ifBlank { "Dashboard could not be loaded." })
            }
        } catch (e: Exception) {
            when {
                SessionAuth.isUnauthorized(e) && retryOnUnauthorized -> {
                    SessionAuth.hydrateAccessToken()
                    loadDashboardForUser(userId, generation, retryOnUnauthorized = false)
                }
                generation != loadGeneration -> Unit
                SessionAuth.isUnauthorized(e) -> {
                    if (_dashboardState.value !is UiState.Success) {
                        _dashboardState.value = UiState.Error(sessionExpiredMessage())
                    }
                }
                e is HttpException && e.code() == 404 -> {
                    _destination.value = DashboardDestination.Onboarding
                }
                else -> {
                    if (_dashboardState.value !is UiState.Success) {
                        _dashboardState.value = UiState.Error(messageFor(e))
                    }
                }
            }
        }
    }

    fun clearDestination() {
        _destination.value = null
    }

    private suspend fun resolveUserIdOrNull(): Long? {
        SessionAuth.resolveLoggedInUserId()?.let { return it }
        delay(80)
        return SessionAuth.resolveLoggedInUserId()
    }

    private fun sessionExpiredMessage(): String {
        return "Session expired. Please log in again."
    }

    private fun String.isProfileMissingMessage(): Boolean {
        val normalized = lowercase()
        return "profile" in normalized && ("missing" in normalized || "not found" in normalized)
    }

    private fun messageFor(error: Exception): String {
        return when (error) {
            is HttpException -> when (error.code()) {
                401 -> sessionExpiredMessage()
                else -> "Server error ${error.code()}. Please try refreshing."
            }
            is IOException -> "Cannot reach the backend. Start the server and refresh."
            else -> error.message ?: "Dashboard could not be loaded."
        }
    }

    companion object {
        fun factory(sessionDataStore: UserSessionDataStore): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DashboardViewModel(
                        dashboardRepository = DashboardRepository(),
                        sessionDataStore = sessionDataStore
                    ) as T
                }
            }
        }
    }
}
