package com.example.insightself.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.insightself.ui.theme.InsightMuted

private val RequiredRed = Color(0xFFE53935)

@Composable
fun RequiredFieldLabel(
    text: String,
    requirement: ProfileCalcRequirement = ProfileCalcRequirement.NONE,
    modifier: Modifier = Modifier,
    isChinese: Boolean = true,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleMedium
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = InsightMuted,
            style = style,
            fontWeight = FontWeight.Medium
        )
        if (requirement.showRequiredMark) {
            Text(
                text = " *",
                color = RequiredRed,
                style = style,
                fontWeight = FontWeight.Bold
            )
            if (requirement == ProfileCalcRequirement.BAZI) {
                Text(
                    text = if (isChinese) "（八字）" else " (Bazi)",
                    color = InsightMuted,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(start = 2.dp)
                )
            }
        }
    }
}

@Composable
fun ProfileRequiredFieldsLegend(
    isChinese: Boolean,
    modifier: Modifier = Modifier
) {
    Text(
        text = if (isChinese) {
            "* 为必填项；出生信息用于八字或星座；带（八字）为仅八字所需。"
        } else {
            "* Required fields; birth data powers Bazi/Zodiac; (Bazi) marks Bazi-only fields."
        },
        color = InsightMuted,
        style = MaterialTheme.typography.bodySmall,
        modifier = modifier
    )
}
