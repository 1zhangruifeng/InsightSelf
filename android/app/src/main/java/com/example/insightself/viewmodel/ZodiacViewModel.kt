package com.example.insightself.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.insightself.data.auth.SessionAuth
import com.example.insightself.data.local.UserSessionDataStore
import com.example.insightself.data.model.ZodiacDailyDto
import com.example.insightself.data.model.ZodiacMatchRequest
import com.example.insightself.data.model.ZodiacMatchResultDto
import com.example.insightself.data.model.ZodiacNatalDto
import com.example.insightself.data.repository.ZodiacRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ZodiacViewModel(
    private val zodiacRepository: ZodiacRepository,
    private val sessionDataStore: UserSessionDataStore
) : ViewModel() {
    private val _dailyState = MutableStateFlow<UiState<ZodiacDailyDto>>(UiState.Idle)
    val dailyState: StateFlow<UiState<ZodiacDailyDto>> = _dailyState

    private val _matchState = MutableStateFlow<UiState<ZodiacMatchResultDto>>(UiState.Idle)
    val matchState: StateFlow<UiState<ZodiacMatchResultDto>> = _matchState

    private val _natalState = MutableStateFlow<UiState<ZodiacNatalDto>>(UiState.Idle)
    val natalState: StateFlow<UiState<ZodiacNatalDto>> = _natalState

    /** Last successfully loaded natal chart; kept for tab switches while a refresh is in flight. */
    private var cachedNatal: ZodiacNatalDto? = null

    private val dateCache = mutableMapOf<String, ZodiacDailyDto>()

    private var natalLoadJob: Job? = null
    private var natalLoadGeneration = 0

    private var dailyLoadJob: Job? = null
    private var dailyLoadGeneration = 0

    fun loadDailyForDate(date: LocalDate, forceRefresh: Boolean = false) {
        dailyLoadJob?.cancel()
        dailyLoadJob = viewModelScope.launch {
            val generation = ++dailyLoadGeneration
            val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)

            if (!forceRefresh) {
                dateCache[dateString]?.let {
                    _dailyState.value = UiState.Success(it)
                    return@launch
                }
            }

            val userId = resolveUserIdOrNull()
            if (userId == null) {
                if (_dailyState.value !is UiState.Success) {
                    _dailyState.value = UiState.Error(sessionExpiredMessage())
                }
                return@launch
            }

            if (_dailyState.value !is UiState.Success) {
                _dailyState.value = UiState.Loading
            }

            loadDailyForUser(userId, dateString, generation, retryOnUnauthorized = true)
        }
    }

    private suspend fun loadDailyForUser(
        userId: Long,
        dateString: String,
        generation: Int,
        retryOnUnauthorized: Boolean
    ) {
        try {
            val response = zodiacRepository.daily(userId, dateString)
            if (generation != dailyLoadGeneration) return
            val daily = response.data
            if (response.success && daily != null) {
                dateCache[dateString] = daily
                _dailyState.value = UiState.Success(daily)
            } else {
                _dailyState.value = UiState.Error(
                    response.message.ifBlank { "Daily zodiac could not be loaded for $dateString" }
                )
            }
        } catch (e: Exception) {
            when {
                SessionAuth.isUnauthorized(e) && retryOnUnauthorized -> {
                    SessionAuth.hydrateAccessToken()
                    loadDailyForUser(userId, dateString, generation, retryOnUnauthorized = false)
                }
                generation == dailyLoadGeneration -> {
                    if (_dailyState.value !is UiState.Success) {
                        _dailyState.value = UiState.Error(messageFor(e))
                    }
                }
            }
        }
    }

    fun loadDaily() {
        loadDailyForDate(LocalDate.now())
    }

    /** Natal data for UI: current success state, or last cached chart while reloading. */
    fun natalChartForDisplay(): ZodiacNatalDto? {
        return (_natalState.value as? UiState.Success)?.data ?: cachedNatal
    }

    fun loadNatal(forceRefresh: Boolean = false) {
        restoreNatalFromCacheIfNeeded()

        if (!forceRefresh && _natalState.value is UiState.Success) {
            return
        }

        natalLoadJob?.cancel()
        natalLoadJob = viewModelScope.launch {
            val generation = ++natalLoadGeneration

            val userId = resolveUserIdOrNull()
            if (userId == null) {
                if (cachedNatal == null) {
                    _natalState.value = UiState.Error(sessionExpiredMessage())
                }
                return@launch
            }

            if (cachedNatal == null) {
                _natalState.value = UiState.Loading
            }

            loadNatalForUser(userId, generation, retryOnUnauthorized = true)
        }
    }

    private fun restoreNatalFromCacheIfNeeded() {
        val cached = cachedNatal ?: return
        if (_natalState.value !is UiState.Success) {
            _natalState.value = UiState.Success(cached)
        }
    }

    private suspend fun loadNatalForUser(
        userId: Long,
        generation: Int,
        retryOnUnauthorized: Boolean
    ) {
        try {
            val response = zodiacRepository.natal(userId)
            if (generation != natalLoadGeneration) return
            val natal = response.data
            if (response.success && natal != null) {
                cachedNatal = natal
                _natalState.value = UiState.Success(natal)
            } else if (cachedNatal == null) {
                _natalState.value = UiState.Error(
                    response.message.ifBlank { "Natal chart could not be loaded." }
                )
            }
        } catch (e: Exception) {
            when {
                SessionAuth.isUnauthorized(e) && retryOnUnauthorized -> {
                    SessionAuth.hydrateAccessToken()
                    loadNatalForUser(userId, generation, retryOnUnauthorized = false)
                }
                generation == natalLoadGeneration -> {
                    if (_natalState.value !is UiState.Success) {
                        _natalState.value = UiState.Error(messageFor(e))
                    }
                }
            }
        }
    }

    fun submitMatch(
        targetNickname: String,
        targetBirthDate: String,
        targetPersonalityTag: String?
    ) {
        val nickname = targetNickname.trim()
        val birthDate = targetBirthDate.trim()
        val tag = targetPersonalityTag?.trim().orEmpty()

        if (nickname.isBlank()) {
            _matchState.value = UiState.Error("Target nickname is required.")
            return
        }
        if (!birthDate.matches(Regex("""\d{4}-\d{2}-\d{2}"""))) {
            _matchState.value = UiState.Error("Birth date must use yyyy-MM-dd.")
            return
        }

        viewModelScope.launch {
            val userId = resolveUserIdOrNull()
            if (userId == null) {
                _matchState.value = UiState.Error(sessionExpiredMessage())
                return@launch
            }

            _matchState.value = UiState.Loading
            try {
                val response = zodiacRepository.match(
                    ZodiacMatchRequest(
                        userId = userId,
                        targetNickname = nickname,
                        targetBirthDate = birthDate,
                        targetPersonalityTag = tag.ifBlank { null }
                    )
                )
                val result = response.data
                _matchState.value = if (response.success && result != null) {
                    UiState.Success(result)
                } else {
                    UiState.Error(response.message.ifBlank { "Match could not be calculated." })
                }
            } catch (e: Exception) {
                _matchState.value = UiState.Error(messageFor(e))
            }
        }
    }

    fun resetMatch() {
        _matchState.value = UiState.Idle
    }

    private suspend fun resolveUserIdOrNull(): Long? {
        SessionAuth.resolveLoggedInUserId()?.let { return it }
        delay(80)
        return SessionAuth.resolveLoggedInUserId()
    }

    private fun sessionExpiredMessage(): String {
        return "Session expired. Please log in again."
    }

    private fun messageFor(error: Exception): String {
        return when (error) {
            is HttpException -> when (error.code()) {
                401 -> sessionExpiredMessage()
                else -> "Server error ${error.code()}. Please try again."
            }
            is IOException -> "Cannot reach the backend. Start the server and try again."
            else -> error.message ?: "Zodiac module could not be loaded."
        }
    }

    companion object {
        fun factory(sessionDataStore: UserSessionDataStore): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ZodiacViewModel(
                        zodiacRepository = ZodiacRepository(),
                        sessionDataStore = sessionDataStore
                    ) as T
                }
            }
        }
    }
}
