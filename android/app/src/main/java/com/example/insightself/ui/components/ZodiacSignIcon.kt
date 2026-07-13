package com.example.insightself.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.insightself.ui.theme.InsightPrimary
import com.example.insightself.util.ZodiacSignIcons

@Composable
fun ZodiacSignIcon(
    sign: String?,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    tint: Color = InsightPrimary,
    showBackground: Boolean = true,
    contentDescription: String? = null
) {
    val iconModifier = modifier
        .size(size)
        .then(
            if (showBackground) {
                Modifier
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.12f))
            } else {
                Modifier
            }
        )

    Box(
        modifier = iconModifier,
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(ZodiacSignIcons.drawableRes(sign)),
            contentDescription = contentDescription,
            modifier = Modifier.size(size * 0.58f),
            tint = tint
        )
    }
}

@Composable
fun ZodiacSignGlyph(
    sign: String?,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    tint: Color = InsightPrimary
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = ZodiacSignIcons.symbol(sign),
            fontSize = (size.value * 0.48f).sp,
            color = tint,
            style = MaterialTheme.typography.titleLarge
        )
    }
}
