package com.example.insightself.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.insightself.data.auth.SessionAuth
import com.example.insightself.data.local.UserSessionDataStore
import com.example.insightself.data.model.BaziDto
import com.example.insightself.data.repository.BaziRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class BaziViewModel(
    private val baziRepository: BaziRepository,
    private val sessionDataStore: UserSessionDataStore
) : ViewModel() {
    private val _baziState = MutableStateFlow<UiState<BaziDto?>>(UiState.Idle)
    val baziState: StateFlow<UiState<BaziDto?>> = _baziState

    private val _generating = MutableStateFlow(false)
    val generating: StateFlow<Boolean> = _generating

    fun loadLatest() {
        viewModelScope.launch {
            val userId = SessionAuth.requireLoggedInUserId()
            if (userId == null) {
                _baziState.value = UiState.Error("Session expired. Please log in again.")
                return@launch
            }

            _baziState.value = UiState.Loading
            try {
                val response = baziRepository.latest(userId)
                _baziState.value = if (response.success) {
                    UiState.Success(response.data)
                } else {
                    UiState.Success(null)
                }
            } catch (e: HttpException) {
                _baziState.value = when (e.code()) {
                    404 -> UiState.Success(null)
                    401 -> UiState.Error(messageFor(e))
                    else -> UiState.Error(messageFor(e))
                }
            } catch (e: Exception) {
                _baziState.value = UiState.Error(messageFor(e))
            }
        }
    }

    fun generate() {
        viewModelScope.launch {
            val userId = SessionAuth.requireLoggedInUserId()
            if (userId == null) {
                _baziState.value = UiState.Error("Session expired. Please log in again.")
                return@launch
            }

            _generating.value = true
            android.util.Log.d("BaziViewModel", "Generating Bazi for userId: $userId")
            try {
                val response = baziRepository.generate(userId)
                android.util.Log.d("BaziViewModel", "Generate response: success=${response.success}, message=${response.message}")
                val result = response.data
                _baziState.value = if (response.success && result != null) {
                    UiState.Success(result)
                } else {
                    UiState.Error(response.message.ifBlank { "Bazi insight could not be generated." })
                }
            } catch (e: Exception) {
                android.util.Log.e("BaziViewModel", "Generate error: ${e.message}")
                _baziState.value = UiState.Error(messageFor(e))
            } finally {
                _generating.value = false
            }
        }
    }

    private fun messageFor(error: Exception): String {
        return when (error) {
            is HttpException -> when (error.code()) {
                401 -> "Session expired. Please log in again."
                else -> "Server error ${error.code()}. Please try again."
            }
            is IOException -> "Cannot reach the backend. Start the server and try again."
            else -> error.message ?: "Bazi insight could not be loaded."
        }
    }

    companion object {
        fun factory(sessionDataStore: UserSessionDataStore): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return BaziViewModel(
                        baziRepository = BaziRepository(),
                        sessionDataStore = sessionDataStore
                    ) as T
                }
            }
        }
    }
}