package com.example.insightself.data.local

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.util.Locale

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

class LanguageManager(private val context: Context) {

    companion object {
        private val LANGUAGE_KEY = stringPreferencesKey("selected_language")
        private const val DEFAULT_LANGUAGE = "en"

        val SUPPORTED_LANGUAGES = listOf(
            LanguageOption("en", "English"),
            LanguageOption("zh", "简体中文")
        )

        fun getLanguageFlow(context: Context): Flow<String> {
            return context.dataStore.data.map { preferences ->
                preferences[LANGUAGE_KEY] ?: DEFAULT_LANGUAGE
            }
        }

        suspend fun saveLanguage(context: Context, languageCode: String) {
            context.dataStore.edit { preferences ->
                preferences[LANGUAGE_KEY] = languageCode
            }
        }

        fun applyLanguage(context: Context, languageCode: String): Context {
            val normalized = normalizeLanguage(languageCode)
            val locale = Locale(normalized)
            Locale.setDefault(locale)

            val config = Configuration(context.resources.configuration)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                config.setLocale(locale)
            } else {
                @Suppress("Deprecation")
                config.locale = locale
            }

            return context.createConfigurationContext(config)
        }

        fun normalizeLanguage(languageCode: String?): String {
            return if (languageCode == "zh") "zh" else "en"
        }

        fun currentLanguageBlocking(context: Context): String {
            return runBlocking {
                getLanguageFlow(context).first()
            }
        }
    }
}

data class LanguageOption(
    val code: String,
    val displayName: String
)