package com.example.insightself.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.insightself.ui.theme.InsightDanger

@Composable
fun ErrorView(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
    secondaryActionLabel: String? = null,
    onSecondaryAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        InsightCard(title = "Something needs attention", subtitle = message) {
            Text(
                text = "You can retry when the connection or input looks right.",
                color = InsightDanger,
                style = MaterialTheme.typography.bodyMedium
            )
            if (onRetry != null) {
                PrimaryActionButton(text = "Retry", onClick = onRetry)
            }
            if (onSecondaryAction != null && !secondaryActionLabel.isNullOrBlank()) {
                SecondaryActionButton(text = secondaryActionLabel, onClick = onSecondaryAction)
            }
        }
    }
}
