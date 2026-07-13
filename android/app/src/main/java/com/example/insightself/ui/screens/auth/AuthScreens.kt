package com.example.insightself.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.insightself.R
import com.example.insightself.ui.components.AppPasswordField
import com.example.insightself.ui.components.AppTextField
import com.example.insightself.ui.components.EnglishLocaleContent
import com.example.insightself.ui.components.ErrorView
import com.example.insightself.ui.components.InsightCard
import com.example.insightself.ui.components.LoadingView
import com.example.insightself.ui.components.PrimaryActionButton
import com.example.insightself.ui.components.SecondaryActionButton
import com.example.insightself.ui.components.SectionTitle
import com.example.insightself.ui.theme.InsightBackground
import com.example.insightself.ui.theme.InsightBackground2
import com.example.insightself.ui.theme.InsightMuted
import com.example.insightself.ui.theme.InsightSpacing
import com.example.insightself.util.SampleNameGenerator
import com.example.insightself.viewmodel.AuthDestination
import com.example.insightself.viewmodel.AuthViewModel
import com.example.insightself.viewmodel.UiState

@Composable
fun StartupScreen(
    authViewModel: AuthViewModel,
    onRoute: (AuthDestination) -> Unit
) {
    val startupState by authViewModel.startupState.collectAsState()

    LaunchedEffect(Unit) {
        authViewModel.routeFromSavedSession()
    }
    LaunchedEffect(startupState) {
        val state = startupState
        if (state is UiState.Success) {
            onRoute(state.data)
        }
    }

    ScreenBackground {
        when (val state = startupState) {
            is UiState.Error -> ErrorView(
                message = state.message,
                onRetry = { authViewModel.routeFromSavedSession() }
            )
            else -> LoadingView(message = stringResource(R.string.checking_session))
        }
    }
}

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginComplete: (AuthDestination) -> Unit,
    onRegisterClick: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState by authViewModel.authState.collectAsState()
    val loading = authState is UiState.Loading

    LaunchedEffect(authState) {
        val state = authState
        if (state is UiState.Success) {
            authViewModel.resetAuthState()
            onLoginComplete(state.data)
        }
    }

    AuthScreenFrame {
        SectionTitle(
            title = stringResource(R.string.app_name),
            subtitle = stringResource(R.string.profile_subtitle)
        )
        InsightCard(title = stringResource(R.string.login), subtitle = stringResource(R.string.existing_demo_account)) {
            AppTextField(
                value = username,
                onValueChange = { username = it },
                label = stringResource(R.string.username),
                placeholder = stringResource(R.string.letters_and_numbers)
            )
            AppPasswordField(
                value = password,
                onValueChange = { password = it },
                label = stringResource(R.string.password),
                placeholder = "pass1234"
            )
            if (authState is UiState.Error) {
                Text(
                    text = (authState as UiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            PrimaryActionButton(
                text = stringResource(R.string.login),
                onClick = { authViewModel.login(username, password) },
                enabled = !loading,
                loading = loading
            )
            SecondaryActionButton(
                text = "Use HKICT demo",
                onClick = { authViewModel.seedDemo() }
            )
            SecondaryActionButton(
                text = stringResource(R.string.no_account),
                onClick = onRegisterClick
            )
        }
        Text(
            text = stringResource(R.string.demo_note_text),
            color = InsightMuted,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onRegisterComplete: (AuthDestination) -> Unit,
    onLoginClick: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val usernamePlaceholder = remember { SampleNameGenerator.usernamePlaceholder() }
    val authState by authViewModel.authState.collectAsState()
    val loading = authState is UiState.Loading

    LaunchedEffect(authState) {
        val state = authState
        if (state is UiState.Success) {
            authViewModel.resetAuthState()
            onRegisterComplete(state.data)
        }
    }

    AuthScreenFrame {
        SectionTitle(
            title = stringResource(R.string.create_account),
            subtitle = stringResource(R.string.register_subtitle)
        )
        InsightCard(title = stringResource(R.string.register), subtitle = stringResource(R.string.new_demo_account)) {
            AppTextField(
                value = username,
                onValueChange = { username = it },
                label = stringResource(R.string.username),
                placeholder = usernamePlaceholder
            )
            AppPasswordField(
                value = password,
                onValueChange = { password = it },
                label = stringResource(R.string.password),
                placeholder = "pass1234"
            )
            if (authState is UiState.Error) {
                Text(
                    text = (authState as UiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            PrimaryActionButton(
                text = stringResource(R.string.register_and_fill),
                onClick = { authViewModel.register(username, password) },
                enabled = !loading,
                loading = loading
            )
            SecondaryActionButton(
                text = stringResource(R.string.have_account),
                onClick = onLoginClick
            )
        }
    }
}

@Composable
private fun AuthScreenFrame(content: @Composable ColumnScope.() -> Unit) {
    EnglishLocaleContent {
        ScreenBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = InsightSpacing.ScreenHorizontal, vertical = 34.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                content = content
            )
        }
    }
}

@Composable
fun ScreenBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(InsightBackground, InsightBackground2)))
    ) {
        content()
    }
}
