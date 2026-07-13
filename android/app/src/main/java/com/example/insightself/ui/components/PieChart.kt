package com.example.insightself.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.insightself.ui.theme.InsightText
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class PieSlice(
    val label: String,
    val value: Int,
    val color: Color
)

@Composable
fun PieChart(
    slices: List<PieSlice>,
    modifier: Modifier = Modifier,
    isChinese: Boolean = true
) {
    val total = slices.sumOf { it.value }
    if (total == 0) return

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 饼图
        Box(
            modifier = Modifier
                .size(140.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(140.dp)) {
                var startAngle = -90f
                val radius = size.minDimension / 2
                val centerX = size.width / 2
                val centerY = size.height / 2

                slices.forEach { slice ->
                    val sweepAngle = (slice.value.toFloat() / total) * 360f
                    val startRad = Math.toRadians(startAngle.toDouble())
                    val sweepRad = Math.toRadians(sweepAngle.toDouble())

                    // 计算弧的端点
                    val x1 = centerX + radius * cos(startRad).toFloat()
                    val y1 = centerY + radius * sin(startRad).toFloat()
                    val x2 = centerX + radius * cos(startRad + sweepRad).toFloat()
                    val y2 = centerY + radius * sin(startRad + sweepRad).toFloat()

                    // 创建扇形路径
                    val path = Path().apply {
                        moveTo(centerX, centerY)
                        lineTo(x1, y1)
                        arcTo(
                            rect = androidx.compose.ui.geometry.Rect(
                                centerX - radius,
                                centerY - radius,
                                centerX + radius,
                                centerY + radius
                            ),
                            startAngleDegrees = startAngle,
                            sweepAngleDegrees = sweepAngle,
                            forceMoveTo = false
                        )
                        close()
                    }

                    drawPath(
                        path = path,
                        color = slice.color
                    )

                    startAngle += sweepAngle
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // 图例
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            slices.forEach { slice ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(slice.color, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = slice.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = InsightText,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${slice.value}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = InsightText
                    )
                }
            }
        }
    }
}