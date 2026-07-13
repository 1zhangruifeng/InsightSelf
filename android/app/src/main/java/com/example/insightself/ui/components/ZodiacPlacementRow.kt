package com.example.insightself.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.insightself.ui.theme.InsightMuted
import com.example.insightself.ui.theme.InsightPrimary
import com.example.insightself.ui.theme.InsightText

data class ZodiacPlacement(
    val roleLabel: String,
    val signLabel: String,
    val signKey: String?
)

@Composable
fun ZodiacPlacementRow(
    placements: List<ZodiacPlacement>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        placements.forEach { placement ->
            ZodiacPlacementChip(placement = placement)
        }
    }
}

@Composable
private fun ZodiacPlacementChip(
    placement: ZodiacPlacement,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ZodiacSignIcon(
            sign = placement.signKey,
            size = 48.dp,
            contentDescription = placement.signLabel
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = placement.roleLabel,
            style = MaterialTheme.typography.labelSmall,
            color = InsightMuted,
            textAlign = TextAlign.Center
        )
        Text(
            text = placement.signLabel,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = InsightText,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ZodiacSignHeader(
    signKey: String?,
    signLabel: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ZodiacSignIcon(sign = signKey, size = 52.dp, tint = InsightPrimary)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = signLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = InsightText
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = InsightMuted
                )
            }
        }
    }
}
