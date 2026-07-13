package com.example.insightself.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.insightself.ui.theme.InsightMuted
import com.example.insightself.ui.theme.InsightText

data class FortuneData(
    val score: Int,
    val luckyColor: String,
    val luckyDirection: String,
    val luckyNumber: String,
    val dos: List<String>,
    val donts: List<String>
)

@Composable
fun TodayFortuneCard(
    fortune: FortuneData,
    isChinese: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 综合分数 - 环形进度条效果
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(72.dp)
                    .height(72.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = Color(0xFF5B67FF),
                        startAngle = -90f,
                        sweepAngle = 360f * fortune.score / 100f,
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawCircle(
                        color = Color(0xFFE8ECF1),
                        radius = size.minDimension / 2 - 4.dp.toPx(),
                        style = Stroke(width = 8.dp.toPx())
                    )
                }
                Text(
                    text = "${fortune.score}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5B67FF)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FortuneItem(
                    label = if (isChinese) "幸运色" else "Lucky Color",
                    value = fortune.luckyColor
                )
                FortuneItem(
                    label = if (isChinese) "幸运方位" else "Lucky Direction",
                    value = fortune.luckyDirection
                )
                FortuneItem(
                    label = if (isChinese) "幸运数字" else "Lucky Number",
                    value = fortune.luckyNumber
                )
            }
        }

        // 宜忌列表
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isChinese) "宜" else "Do",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1FBF73)
                )
                fortune.dos.forEach { doItem ->
                    Text(
                        text = "• $doItem",
                        style = MaterialTheme.typography.bodySmall,
                        color = InsightMuted,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isChinese) "忌" else "Don't",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE53935)
                )
                fortune.donts.forEach { dontItem ->
                    Text(
                        text = "• $dontItem",
                        style = MaterialTheme.typography.bodySmall,
                        color = InsightMuted,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FortuneItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = InsightMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = InsightText
        )
    }
}