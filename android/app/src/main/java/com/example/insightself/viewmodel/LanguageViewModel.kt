package com.example.insightself.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LanguageViewModel(
    languageFlow: Flow<String>,
    private val saveLanguagePreference: suspend (String) -> Unit
) : ViewModel() {

    private val _currentLanguage = MutableStateFlow("en")
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    init {
        viewModelScope.launch {
            languageFlow.collect { language ->
                _currentLanguage.value = language
            }
        }
    }

    fun updateLanguage(languageCode: String) {
        viewModelScope.launch {
            saveLanguagePreference(languageCode)
            _currentLanguage.value = languageCode
        }
    }
}
