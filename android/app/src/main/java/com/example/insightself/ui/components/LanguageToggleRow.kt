package com.example.insightself.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.insightself.R

@Composable
fun LanguageToggleRow(
    currentLanguage: String,
    onSelectLanguage: (String) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.language),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LanguageButton(
                title = stringResource(R.string.chinese),
                isSelected = currentLanguage == "zh",
                modifier = Modifier.weight(1f),
                enabled = enabled,
                onClick = { onSelectLanguage("zh") }
            )
            LanguageButton(
                title = stringResource(R.string.english),
                isSelected = currentLanguage == "en",
                modifier = Modifier.weight(1f),
                enabled = enabled,
                onClick = { onSelectLanguage("en") }
            )
        }
    }
}
