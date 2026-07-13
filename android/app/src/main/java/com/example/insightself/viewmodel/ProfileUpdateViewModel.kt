package com.example.insightself.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.insightself.data.local.UserSessionDataStore
import com.example.insightself.data.model.ProfileDto
import com.example.insightself.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileUpdateViewModel(
    private val profileRepository: ProfileRepository,
    private val sessionDataStore: UserSessionDataStore
) : ViewModel() {

    init {
        println("=== DEBUG: ProfileUpdateViewModel created")
    }

    private val _updateState = MutableStateFlow<UiState<ProfileDto>>(UiState.Idle)
    val updateState: StateFlow<UiState<ProfileDto>> = _updateState.asStateFlow()

    private val _currentProfile = MutableStateFlow<ProfileDto?>(null)
    val currentProfile: StateFlow<ProfileDto?> = _currentProfile.asStateFlow()

    // 密码修改状态 - 独立的状态流
    private val _passwordChangeState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val passwordChangeState: StateFlow<UiState<Boolean>> = _passwordChangeState.asStateFlow()

    fun loadProfile() {
        println("=== DEBUG: loadProfile called")
        viewModelScope.launch {
            try {
                val userId = sessionDataStore.getCurrentUserId()
                println("=== DEBUG: userId = $userId")
                if (userId == null) {
                    println("=== DEBUG: userId is null, setting Idle")
                    _updateState.value = UiState.Idle
                    return@launch
                }
                val response = profileRepository.getProfile(userId)
                println("=== DEBUG: getProfile response success=${response.success}, data=${response.data}")
                if (response.success && response.data != null) {
                    _currentProfile.value = response.data
                    _updateState.value = UiState.Success(response.data)
                } else {
                    _currentProfile.value = null
                    _updateState.value = UiState.Idle
                }
            } catch (e: Exception) {
                println("=== DEBUG: loadProfile error: ${e.message}")
                e.printStackTrace()
                _updateState.value = UiState.Error(e.message ?: "Failed to load profile")
            }
        }
    }

    fun updateLanguage(languageCode: String, onComplete: () -> Unit) {
        println("=== DEBUG: updateLanguage called with $languageCode")
        viewModelScope.launch {
            try {
                val userId = sessionDataStore.getCurrentUserId()
                println("=== DEBUG: updateLanguage userId = $userId")
                if (userId == null) {
                    println("=== DEBUG: userId null, calling onComplete")
                    onComplete()
                    return@launch
                }

                val currentProfile = _currentProfile.value
                if (currentProfile == null) {
                    println("=== DEBUG: currentProfile null, calling onComplete")
                    onComplete()
                    return@launch
                }

                val updatedProfile = currentProfile.copy(language = languageCode)
                println("=== DEBUG: updatedProfile = $updatedProfile")
                val response = profileRepository.updateProfile(userId, updatedProfile)

                if (response.success && response.data != null) {
                    _currentProfile.value = response.data
                    _updateState.value = UiState.Success(response.data)
                }
                onComplete()
            } catch (e: Exception) {
                println("=== DEBUG: updateLanguage error: ${e.message}")
                e.printStackTrace()
                onComplete()
            }
        }
    }

    fun resetState() {
        _updateState.value = UiState.Idle
    }

    fun updateProfile(profile: ProfileDto) {
        viewModelScope.launch {
            _updateState.value = UiState.Loading
            try {
                val userId = sessionDataStore.getCurrentUserId()
                if (userId == null) {
                    _updateState.value = UiState.Error("Session expired")
                    return@launch
                }
                val response = profileRepository.updateProfile(userId, profile)
                if (response.success && response.data != null) {
                    _currentProfile.value = response.data
                    _updateState.value = UiState.Success(response.data)
                } else {
                    _updateState.value = UiState.Error(response.message)
                }
            } catch (e: Exception) {
                _updateState.value = UiState.Error(e.message ?: "Failed to update profile")
            }
        }
    }

    fun changePassword(oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            _passwordChangeState.value = UiState.Loading
            try {
                val userId = sessionDataStore.getCurrentUserId()
                if (userId == null) {
                    _passwordChangeState.value = UiState.Error("会话已过期，请重新登录")
                    return@launch
                }
                val response = profileRepository.changePassword(userId, oldPassword, newPassword)
                if (response.success) {
                    _passwordChangeState.value = UiState.Success(true)
                } else {
                    // 使用后端返回的错误消息（已经在前面的 Repository 中解析为友好消息）
                    val errorMessage = response.message.ifBlank { "密码修改失败，请稍后重试" }
                    _passwordChangeState.value = UiState.Error(errorMessage)
                }
            } catch (e: Exception) {
                _passwordChangeState.value = UiState.Error(e.message ?: "密码修改失败，请稍后重试")
            }
        }
    }

    // 重置密码修改状态
    fun resetPasswordChangeState() {
        _passwordChangeState.value = UiState.Idle
    }
}
