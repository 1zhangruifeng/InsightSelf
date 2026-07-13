package com.example.insightself.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.insightself.ui.theme.InsightCardStrong
import com.example.insightself.ui.theme.InsightPrimary
import com.example.insightself.ui.theme.InsightShapes
import com.example.insightself.ui.theme.InsightStroke
import com.example.insightself.ui.theme.InsightText

@Composable
fun LanguageButton(
    title: String,
    isSelected: Boolean,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Text(
        text = title,
        modifier = modifier
            .background(
                color = if (isSelected) InsightPrimary.copy(alpha = 0.12f) else InsightCardStrong,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(InsightShapes.PillRadius)
            )
            .border(
                BorderStroke(
                    1.dp,
                    if (isSelected) InsightPrimary.copy(alpha = 0.42f) else InsightStroke
                ),
                androidx.compose.foundation.shape.RoundedCornerShape(InsightShapes.PillRadius)
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 14.dp),
        color = if (isSelected) InsightPrimary else InsightText,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.ExtraBold,
        textAlign = TextAlign.Center
    )
}
