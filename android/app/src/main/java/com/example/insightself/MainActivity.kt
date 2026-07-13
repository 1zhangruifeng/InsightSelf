package com.example.insightself

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import com.example.insightself.data.auth.SessionAuth
import com.example.insightself.data.local.LanguageManager
import com.example.insightself.ui.navigation.AppNavGraph
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.insightself.ui.theme.InsightSelfTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        SessionAuth.init(this)
        applySavedLanguage()
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            SessionAuth.hydrateAccessToken()
        }
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        setContent {
            InsightSelfTheme {
                AppNavGraph()
            }
        }
    }

    private fun applySavedLanguage() {
        val language = runBlocking {
            LanguageManager.getLanguageFlow(this@MainActivity).first()
        }
        LanguageManager.applyLanguage(this, language)
    }

    override fun attachBaseContext(newBase: Context) {
        val language = runBlocking {
            LanguageManager.getLanguageFlow(newBase).first()
        }
        super.attachBaseContext(LanguageManager.applyLanguage(newBase, language))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        applySavedLanguage()
    }

    fun restartForLanguageChange() {
        recreate()
    }
}
