package com.example.insightself.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.example.insightself.data.local.LanguageManager

/**
 * Applies a specific app language for the subtree without requiring Activity recreate.
 */
@Composable
fun AppLocaleContent(
    languageCode: String,
    content: @Composable () -> Unit
) {
    val baseContext = LocalContext.current
    val localizedContext = remember(baseContext, languageCode) {
        LanguageManager.applyLanguage(baseContext, languageCode)
    }
    val localizedConfiguration = remember(localizedContext) {
        localizedContext.resources.configuration
    }
    androidx.compose.runtime.CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedConfiguration
    ) {
        content()
    }
}

/**
 * Forces English for auth entry screens (login/register/startup).
 * Does not change the user's saved language preference in DataStore.
 */
@Composable
fun EnglishLocaleContent(content: @Composable () -> Unit) {
    AppLocaleContent(languageCode = "en", content = content)
}
