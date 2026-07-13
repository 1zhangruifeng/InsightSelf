package com.example.insightself.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight

data class RadarData(
    val label: String,
    val value: Int,
    val color: Color
)

@Composable
fun RadarChart(
    data: List<RadarData>,
    modifier: Modifier = Modifier,
    gridColor: Color = Color(0xFFE0E0E0),
    strokeWidth: Float = 2f
) {
    if (data.isEmpty()) return

    var containerSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
    val density = LocalDensity.current

    // 计算顶点位置（用于放置标签）
    val vertexPositions = remember(data, containerSize) {
        if (containerSize == androidx.compose.ui.geometry.Size.Zero) return@remember emptyList()

        val centerX = containerSize.width / 2
        val centerY = containerSize.height / 2
        val radius = containerSize.minDimension / 2 * 0.7f
        val sides = data.size

        (0 until sides).map { index ->
            val angle = (index * 360.0 / sides) - 90
            val radians = Math.toRadians(angle)
            val x = centerX + radius * cos(radians)
            val y = centerY + radius * sin(radians)
            androidx.compose.ui.geometry.Offset(x.toFloat(), y.toFloat())
        }
    }

    Box(
        modifier = modifier
            .height(300.dp)
            .width(300.dp)
            .onGloballyPositioned { coordinates ->
                containerSize = androidx.compose.ui.geometry.Size(
                    coordinates.size.width.toFloat(),
                    coordinates.size.height.toFloat()
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // 绘制雷达图
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (containerSize == androidx.compose.ui.geometry.Size.Zero) return@Canvas

            val centerX = containerSize.width / 2
            val centerY = containerSize.height / 2
            val radius = containerSize.minDimension / 2 * 0.7f
            val sides = data.size
            val angles = (0 until sides).map { index ->
                val angle = (index * 360.0 / sides) - 90
                Math.toRadians(angle)
            }

            // 背景网格
            val gridLevels = listOf(0.25f, 0.5f, 0.75f, 1.0f)
            gridLevels.forEach { level ->
                val path = Path()
                angles.forEachIndexed { i, angle ->
                    val x = centerX + radius * level * cos(angle).toFloat()
                    val y = centerY + radius * level * sin(angle).toFloat()
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
                drawPath(path = path, color = gridColor, style = Stroke(width = 1f))
            }

            // 放射线
            angles.forEach { angle ->
                val x = centerX + radius * cos(angle).toFloat()
                val y = centerY + radius * sin(angle).toFloat()
                drawLine(color = gridColor, start = Offset(centerX, centerY), end = Offset(x, y), strokeWidth = 1f)
            }

            // 数据区域
            val dataPath = Path()
            data.forEachIndexed { index, item ->
                val valuePercent = (item.value.toFloat().coerceIn(0f, 100f)) / 100f
                val angle = angles[index]
                val x = centerX + radius * valuePercent * cos(angle).toFloat()
                val y = centerY + radius * valuePercent * sin(angle).toFloat()
                if (index == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
            }
            dataPath.close()

            val fillColor = if (data.isNotEmpty()) data.first().color.copy(alpha = 0.3f) else Color(0xFF5B67FF).copy(alpha = 0.3f)
            drawPath(path = dataPath, color = fillColor)
            drawPath(path = dataPath, color = if (data.isNotEmpty()) data.first().color else Color(0xFF5B67FF), style = Stroke(width = strokeWidth, cap = StrokeCap.Round))

            // 数据顶点
            data.forEachIndexed { index, item ->
                val valuePercent = (item.value.toFloat().coerceIn(0f, 100f)) / 100f
                val angle = angles[index]
                val x = centerX + radius * valuePercent * cos(angle).toFloat()
                val y = centerY + radius * valuePercent * sin(angle).toFloat()
                drawCircle(color = item.color, radius = 6f, center = Offset(x, y))
            }
        }

        // 标签 - 放在顶点外侧（更远离中心的位置）
        if (vertexPositions.isNotEmpty() && containerSize != androidx.compose.ui.geometry.Size.Zero) {
            val centerX = containerSize.width / 2
            val centerY = containerSize.height / 2
            val radius = containerSize.minDimension / 2 * 0.7f

            // 计算每个标签的偏移量
            val labelOffsets = remember(data, containerSize, density) {
                val offsets = mutableListOf<Pair<androidx.compose.ui.unit.Dp, androidx.compose.ui.unit.Dp>>()
                val extraDistance = with(density) { 8.dp.toPx() } // 额外延伸距离

                data.indices.forEach { index ->
                    val angle = (index * 360.0 / data.size) - 90
                    val radians = Math.toRadians(angle)

                    // 标签位置：顶点半径 + 额外距离
                    val labelRadius = radius + extraDistance
                    val labelX = centerX + labelRadius * cos(radians).toFloat()
                    val labelY = centerY + labelRadius * sin(radians).toFloat()

                    // 计算偏移量
                    val offsetX = with(density) { (labelX - centerX).toDp() }
                    val offsetY = with(density) { (labelY - centerY).toDp() }

                    offsets.add(Pair(offsetX, offsetY))
                }
                offsets
            }

            data.forEachIndexed { index, item ->
                val (offsetX, offsetY) = labelOffsets[index]
                Text(
                    text = item.label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = item.color,
                    modifier = Modifier.offset(
                        x = offsetX,
                        y = offsetY
                    ).align(Alignment.Center)
                )
            }
        }
    }
}