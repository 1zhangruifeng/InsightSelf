package com.example.insightself.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.insightself.ui.theme.InsightPrimary
import com.example.insightself.ui.theme.InsightPrimary2
import com.example.insightself.ui.theme.InsightSuccess
import com.example.insightself.ui.theme.InsightSuccess2
import com.example.insightself.ui.theme.InsightTrack
import com.example.insightself.ui.theme.InsightWarning
import com.example.insightself.ui.theme.InsightWarning2

enum class ScoreTone {
    Primary,
    Success,
    Warning
}

@Composable
fun ScoreBar(
    label: String,
    score: Int,
    modifier: Modifier = Modifier,
    tone: ScoreTone = ScoreTone.Primary,
    showValue: Boolean = true
) {
    val clamped = score.coerceIn(0, 100)
    val brush = when (tone) {
        ScoreTone.Primary -> Brush.linearGradient(listOf(InsightPrimary, InsightPrimary2))
        ScoreTone.Success -> Brush.linearGradient(listOf(InsightSuccess, InsightSuccess2))
        ScoreTone.Warning -> Brush.linearGradient(listOf(InsightWarning, InsightWarning2))
    }
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xDD12131A)
            )
            if (showValue) {
                Text(
                    text = clamped.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF7C808A),
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(InsightTrack)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(clamped / 100f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(brush)
            )
        }
    }
}
