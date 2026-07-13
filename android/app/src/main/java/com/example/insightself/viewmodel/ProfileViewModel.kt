package com.example.insightself.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.insightself.data.auth.SessionAuth
import com.example.insightself.data.local.UserSessionDataStore
import com.example.insightself.data.model.ProfileDto
import com.example.insightself.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class ProfileViewModel(
    private val profileRepository: ProfileRepository,
    private val sessionDataStore: UserSessionDataStore
) : ViewModel() {
    private val _profileState = MutableStateFlow<UiState<ProfileDto>>(UiState.Idle)
    val profileState: StateFlow<UiState<ProfileDto>> = _profileState

    fun createProfile(profile: ProfileDto) {
        viewModelScope.launch {
            _profileState.value = UiState.Loading
            try {
                val userId = SessionAuth.requireLoggedInUserId()
                if (userId == null) {
                    _profileState.value = UiState.Error("Session expired. Please log in again.")
                    return@launch
                }
                val response = profileRepository.createProfile(userId, profile)
                val savedProfile = response.data
                if (!response.success || savedProfile == null) {
                    _profileState.value = UiState.Error(response.message.ifBlank { "Profile could not be saved." })
                    return@launch
                }
                _profileState.value = UiState.Success(savedProfile)
            } catch (e: Exception) {
                _profileState.value = UiState.Error(messageFor(e))
            }
        }
    }

    fun resetProfileState() {
        _profileState.value = UiState.Idle
    }

    private fun messageFor(error: Exception): String {
        return when (error) {
            is HttpException -> "Server error ${error.code()}. Please check the profile fields."
            is IOException -> "Cannot reach the backend. Start the server and try again."
            else -> error.message ?: "Profile could not be saved."
        }
    }

    companion object {
        fun factory(sessionDataStore: UserSessionDataStore): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ProfileViewModel(
                        profileRepository = ProfileRepository(),
                        sessionDataStore = sessionDataStore
                    ) as T
                }
            }
        }
    }
}
