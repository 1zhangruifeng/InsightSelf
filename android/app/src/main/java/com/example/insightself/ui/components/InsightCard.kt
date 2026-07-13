package com.example.insightself.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import com.example.insightself.ui.theme.InsightCard
import com.example.insightself.ui.theme.InsightPrimary
import com.example.insightself.ui.theme.InsightPrimary2
import com.example.insightself.ui.theme.InsightShapes
import com.example.insightself.ui.theme.InsightSpacing
import com.example.insightself.ui.theme.InsightStroke

@Composable
fun InsightCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    icon: ImageVector? = null,
    badge: String? = null,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(InsightShapes.CardRadius),
                ambientColor = Color.Black.copy(alpha = 0.035f),
                spotColor = Color.Black.copy(alpha = 0.05f)
            )
            .background(
                color = InsightCard,
                shape = RoundedCornerShape(InsightShapes.CardRadius)
            )
            .border(
                BorderStroke(1.dp, InsightStroke),
                RoundedCornerShape(InsightShapes.CardRadius)
            )
            .padding(InsightSpacing.Card),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        if (title != null || subtitle != null || icon != null || badge != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (icon != null) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    listOf(
                                        InsightPrimary.copy(alpha = 0.14f),
                                        InsightPrimary2.copy(alpha = 0.10f)
                                    )
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = InsightPrimary
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    if (title != null) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                if (badge != null) {
                    InsightBadge(text = badge)
                }
            }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
fun InsightBadge(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier
            .background(
                color = InsightPrimary.copy(alpha = 0.10f),
                shape = RoundedCornerShape(InsightShapes.PillRadius)
            )
            .border(
                BorderStroke(1.dp, InsightPrimary.copy(alpha = 0.28f)),
                shape = RoundedCornerShape(InsightShapes.PillRadius)
            )
            .padding(horizontal = 11.dp, vertical = 6.dp),
        color = InsightPrimary,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.ExtraBold
    )
}
