package com.example.insightself.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun AiFloatingButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 眨眼动画（每3秒眨一次眼）
    val blinkAnimation = rememberInfiniteTransition()
    val blink by blinkAnimation.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000),
            repeatMode = RepeatMode.Restart
        )
    )

    // 眼睛位置移动动画（看向不同方向，幅度更大）
    val lookX = remember { Animatable(0f) }
    val lookY = remember { Animatable(0f) }

    // 随机改变眼睛方向，幅度更大更明显
    LaunchedEffect(Unit) {
        while (true) {
            delay(2500)  // 缩短间隔，更快变化
            lookX.animateTo(
                targetValue = (kotlin.random.Random.nextFloat() - 0.5f) * 0.8f,  // 幅度从0.35增大到0.8
                animationSpec = tween(400)  // 更快移动
            )
            lookY.animateTo(
                targetValue = (kotlin.random.Random.nextFloat() - 0.5f) * 0.8f,
                animationSpec = tween(400)
            )
        }
    }

    // 呼吸动画（轻微缩放）
    val scaleAnimation = rememberInfiniteTransition()
    val scale by scaleAnimation.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier
            .size(64.dp * scale)
            .clip(androidx.compose.foundation.shape.CircleShape)
            .shadow(8.dp, androidx.compose.foundation.shape.CircleShape)
            .background(
                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF5B67FF),
                        Color(0xFF3D4AFF)
                    ),
                    radius = 60f
                )
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(56.dp)) {
            val isBlinking = blink < 0.05f

            val lookXValue = lookX.value
            val lookYValue = lookY.value

            // 眼睛尺寸和位置 - 增大眼球移动范围
            val eyeSize = size.width * 0.15f
            // 眼睛移动范围增大（从0.1倍增加到0.25倍）
            val eyeMoveRangeX = size.width * 0.25f
            val eyeMoveRangeY = size.height * 0.2f

            val leftEyeX = size.width * 0.34f + eyeMoveRangeX * lookXValue
            val leftEyeY = size.height * 0.38f + eyeMoveRangeY * lookYValue
            val rightEyeX = size.width * 0.66f + eyeMoveRangeX * lookXValue
            val rightEyeY = size.height * 0.38f + eyeMoveRangeY * lookYValue

            // 眨眼效果
            if (isBlinking) {
                // 眨眼 - 眼睛眯成一条线
                drawLine(
                    color = Color.White,
                    start = Offset(leftEyeX - eyeSize, leftEyeY),
                    end = Offset(leftEyeX + eyeSize, leftEyeY),
                    strokeWidth = 4f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color.White,
                    start = Offset(rightEyeX - eyeSize, rightEyeY),
                    end = Offset(rightEyeX + eyeSize, rightEyeY),
                    strokeWidth = 4f,
                    cap = StrokeCap.Round
                )
            } else {
                // 睁眼 - 白色眼球
                drawCircle(
                    color = Color.White,
                    radius = eyeSize,
                    center = Offset(leftEyeX, leftEyeY)
                )
                drawCircle(
                    color = Color.White,
                    radius = eyeSize,
                    center = Offset(rightEyeX, rightEyeY)
                )

                // 黑色瞳孔（随视线移动，移动范围更大）
                val pupilSize = eyeSize * 0.55f
                val pupilMoveX = eyeSize * 0.45f * lookXValue
                val pupilMoveY = eyeSize * 0.45f * lookYValue

                drawCircle(
                    color = Color(0xFF1A1A2E),
                    radius = pupilSize,
                    center = Offset(
                        leftEyeX + pupilMoveX,
                        leftEyeY + pupilMoveY
                    )
                )
                drawCircle(
                    color = Color(0xFF1A1A2E),
                    radius = pupilSize,
                    center = Offset(
                        rightEyeX + pupilMoveX,
                        rightEyeY + pupilMoveY
                    )
                )

                // 眼睛高光
                val highlightSize = eyeSize * 0.22f
                drawCircle(
                    color = Color.White,
                    radius = highlightSize,
                    center = Offset(
                        leftEyeX - eyeSize * 0.25f + pupilMoveX * 0.5f,
                        leftEyeY - eyeSize * 0.25f + pupilMoveY * 0.5f
                    )
                )
                drawCircle(
                    color = Color.White,
                    radius = highlightSize,
                    center = Offset(
                        rightEyeX - eyeSize * 0.25f + pupilMoveX * 0.5f,
                        rightEyeY - eyeSize * 0.25f + pupilMoveY * 0.5f
                    )
                )
            }

            // 微笑（向上弯曲的弧线）
            val mouthY = size.height * 0.70f
            drawArc(
                color = Color.White,
                startAngle = 20f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(size.width * 0.32f, mouthY - 6f),
                size = Size(size.width * 0.36f, size.height * 0.18f),
                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
            )

            // 脸颊红晕
            val blushSize = 7.dp.toPx()
            drawCircle(
                color = Color(0x33FF8C8C),
                radius = blushSize,
                center = Offset(
                    size.width * 0.20f,
                    size.height * 0.58f
                )
            )
            drawCircle(
                color = Color(0x33FF8C8C),
                radius = blushSize,
                center = Offset(
                    size.width * 0.80f,
                    size.height * 0.58f
                )
            )
        }
    }
}