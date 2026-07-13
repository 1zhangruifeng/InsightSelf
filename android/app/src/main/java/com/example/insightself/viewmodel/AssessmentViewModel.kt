package com.example.insightself.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.insightself.data.api.ApiService
import com.example.insightself.data.api.RetrofitClient
import com.example.insightself.data.auth.SessionAuth
import com.example.insightself.data.local.UserSessionDataStore
import com.example.insightself.data.model.AssessmentQuestionDto
import com.example.insightself.data.model.AssessmentResultDto
import com.example.insightself.data.model.AssessmentSubmitRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AssessmentHubData(
    val types: List<String>,
    val results: List<AssessmentResultDto>
)

class AssessmentViewModel(
    private val sessionDataStore: UserSessionDataStore
) : ViewModel() {

    private val apiService: ApiService = RetrofitClient.apiService

    private val _hubState = MutableStateFlow<UiState<AssessmentHubData>>(UiState.Idle)
    val hubState: StateFlow<UiState<AssessmentHubData>> = _hubState.asStateFlow()

    private val _questionsState = MutableStateFlow<UiState<List<AssessmentQuestionDto>>>(UiState.Idle)
    val questionsState: StateFlow<UiState<List<AssessmentQuestionDto>>> = _questionsState.asStateFlow()

    private val _answers = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val answers: StateFlow<Map<Long, Int>> = _answers.asStateFlow()

    private val _submitState = MutableStateFlow<UiState<AssessmentResultDto>>(UiState.Idle)
    val submitState: StateFlow<UiState<AssessmentResultDto>> = _submitState.asStateFlow()

    private val _selectedResult = MutableStateFlow<AssessmentResultDto?>(null)
    val selectedResult: StateFlow<AssessmentResultDto?> = _selectedResult.asStateFlow()

    // 保存答题进度的内存存储
    private val _savedProgressMap = MutableStateFlow<MutableMap<String, Map<Long, Int>>>(mutableMapOf())
    val savedProgressMap: StateFlow<MutableMap<String, Map<Long, Int>>> = _savedProgressMap.asStateFlow()

    fun loadHub() {
        viewModelScope.launch {
            _hubState.value = UiState.Loading
            try {
                val userId = SessionAuth.requireLoggedInUserId()
                if (userId == null) {
                    _hubState.value = UiState.Error("Session expired")
                    return@launch
                }
                val typesResponse = apiService.assessmentTypesWithLanguage(userId)
                val resultsResponse = apiService.assessmentResults(userId)

                if (typesResponse.success && resultsResponse.success) {
                    val types = typesResponse.data?.map { it.type } ?: emptyList()
                    val results = resultsResponse.data ?: emptyList()
                    _hubState.value = UiState.Success(AssessmentHubData(types, results))
                } else {
                    val error = typesResponse.message.takeIf { it.isNotBlank() }
                        ?: resultsResponse.message.takeIf { it.isNotBlank() }
                        ?: "Failed to load assessment data"
                    _hubState.value = UiState.Error(error)
                }
            } catch (e: Exception) {
                _hubState.value = UiState.Error(e.message ?: "Network error")
            }
        }
    }

    fun loadQuestions(type: String) {
        viewModelScope.launch {
            _questionsState.value = UiState.Loading
            try {
                val userId = SessionAuth.requireLoggedInUserId()
                if (userId == null) {
                    _questionsState.value = UiState.Error("Session expired")
                    return@launch
                }
                val response = apiService.assessmentQuestionsWithLanguage(type, userId)
                if (response.success && response.data != null) {
                    _questionsState.value = UiState.Success(response.data)

                    // 如果有保存的进度，加载已保存的答案
                    val savedProgress = loadSavedProgress(type)
                    if (savedProgress != null && savedProgress.isNotEmpty()) {
                        _answers.value = savedProgress
                    } else {
                        _answers.value = emptyMap()
                    }
                } else {
                    _questionsState.value = UiState.Error(response.message)
                }
            } catch (e: Exception) {
                _questionsState.value = UiState.Error(e.message ?: "Failed to load questions")
            }
        }
    }

    fun selectAnswer(questionId: Long, score: Int) {
        val currentAnswers = _answers.value.toMutableMap()
        currentAnswers[questionId] = score
        _answers.value = currentAnswers
    }

    fun submit(type: String) {
        viewModelScope.launch {
            _submitState.value = UiState.Loading
            try {
                val userId = SessionAuth.requireLoggedInUserId()
                if (userId == null) {
                    _submitState.value = UiState.Error("Session expired")
                    return@launch
                }
                val answersList: List<AssessmentSubmitRequest.Answer> = _answers.value.map { (questionId, score) ->
                    AssessmentSubmitRequest.Answer(questionId, score)
                }
                val request = AssessmentSubmitRequest(userId, answersList)
                val response = apiService.submitAssessment(type, request)
                if (response.success && response.data != null) {
                    // 提交成功后清除保存的进度
                    clearSavedProgress(type)
                    _submitState.value = UiState.Success(response.data)
                } else {
                    _submitState.value = UiState.Error(response.message)
                }
            } catch (e: Exception) {
                _submitState.value = UiState.Error(e.message ?: "Failed to submit")
            }
        }
    }

    fun resetSubmitState() {
        _submitState.value = UiState.Idle
    }

    fun selectResult(result: AssessmentResultDto) {
        _selectedResult.value = result
    }

    fun findResult(resultId: Long): AssessmentResultDto? {
        val hubData = (_hubState.value as? UiState.Success)?.data
        return hubData?.results?.find { it.id == resultId }
    }

    // 保存进度
    fun saveProgress(type: String, answers: Map<Long, Int>) {
        val currentMap = _savedProgressMap.value.toMutableMap()
        currentMap[type] = answers
        _savedProgressMap.value = currentMap
    }

    // 加载进度
    fun loadSavedProgress(type: String): Map<Long, Int>? {
        return _savedProgressMap.value[type]
    }

    // 清除进度
    fun clearSavedProgress(type: String) {
        val currentMap = _savedProgressMap.value.toMutableMap()
        currentMap.remove(type)
        _savedProgressMap.value = currentMap
    }

    // 检查是否有保存的进度
    fun hasSavedProgress(type: String): Boolean {
        return _savedProgressMap.value[type]?.isNotEmpty() == true
    }

    companion object {
        fun factory(sessionDataStore: UserSessionDataStore): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AssessmentViewModel(sessionDataStore) as T
                }
            }
        }
    }
}