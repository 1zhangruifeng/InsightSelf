package com.example.insightself.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.insightself.data.auth.SessionAuth
import com.example.insightself.data.local.UserSessionDataStore
import com.example.insightself.data.model.AiReportDto
import com.example.insightself.data.repository.AiReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

class AiReportViewModel(
    private val aiReportRepository: AiReportRepository,
    private val sessionDataStore: UserSessionDataStore
) : ViewModel() {
    private val _reportState = MutableStateFlow<UiState<AiReportDto?>>(UiState.Idle)
    val reportState: StateFlow<UiState<AiReportDto?>> = _reportState

    private val _generating = MutableStateFlow(false)
    val generating: StateFlow<Boolean> = _generating

    fun loadLatest() {
        viewModelScope.launch {
            val userId = SessionAuth.resolveLoggedInUserId()
            if (userId == null) {
                _reportState.value = UiState.Error(sessionExpiredMessage())
                return@launch
            }

            _reportState.value = UiState.Loading
            try {
                val response = aiReportRepository.latest(userId)
                _reportState.value = if (response.success) {
                    UiState.Success(response.data)
                } else {
                    UiState.Success(null)
                }
            } catch (e: HttpException) {
                _reportState.value = if (e.code() == 404) UiState.Success(null) else UiState.Error(messageFor(e))
            } catch (e: Exception) {
                _reportState.value = UiState.Error(messageFor(e))
            }
        }
    }

    fun generate() {
        viewModelScope.launch {
            val userId = SessionAuth.resolveLoggedInUserId()
            if (userId == null) {
                _reportState.value = UiState.Error(sessionExpiredMessage())
                return@launch
            }

            _generating.value = true
            try {
                val response = aiReportRepository.generate(userId)
                val report = response.data
                _reportState.value = if (response.success && report != null) {
                    UiState.Success(report)
                } else {
                    UiState.Error(response.message.ifBlank { "Report could not be generated." })
                }
            } catch (e: Exception) {
                _reportState.value = UiState.Error(messageFor(e))
            } finally {
                _generating.value = false
            }
        }
    }

    private fun sessionExpiredMessage(): String {
        return "Session expired. Please log in again."
    }

    private fun messageFor(error: Exception): String {
        return when (error) {
            is SocketTimeoutException -> "The backend took too long to respond. Please try again."
            is HttpException -> when (error.code()) {
                401 -> sessionExpiredMessage()
                else -> "Server error ${error.code()}. Please try again."
            }
            is IOException -> "Cannot reach the backend. Start the server and try again."
            else -> error.message ?: "Report could not be loaded."
        }
    }

    companion object {
        fun factory(sessionDataStore: UserSessionDataStore): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AiReportViewModel(
                        aiReportRepository = AiReportRepository(),
                        sessionDataStore = sessionDataStore
                    ) as T
                }
            }
        }
    }
}
