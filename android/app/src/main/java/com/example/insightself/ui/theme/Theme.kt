package com.example.insightself.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = InsightPrimary,
    secondary = InsightPrimary2,
    background = InsightBackground,
    surface = InsightCard,
    error = InsightDanger,
    onPrimary = Color.White,
    onBackground = InsightText,
    onSurface = InsightText,
    outline = InsightStroke
)

@Composable
fun InsightSelfTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = InsightTypography,
        content = content
    )
}

object InsightSpacing {
    val ScreenHorizontal = 18.dp
    val ScreenTop = 22.dp
    val Card = 18.dp
    val SectionGap = 16.dp
    val ItemGap = 12.dp
}

object InsightShapes {
    val CardRadius = 20.dp
    val ControlRadius = 18.dp
    val PillRadius = 999.dp
}
