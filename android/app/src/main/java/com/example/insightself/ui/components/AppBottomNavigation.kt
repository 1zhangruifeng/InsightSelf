package com.example.insightself.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.insightself.ui.theme.InsightMutedLight
import com.example.insightself.ui.theme.InsightPrimary
import com.example.insightself.ui.theme.InsightPrimary2
import com.example.insightself.ui.theme.InsightStroke
import com.example.insightself.ui.theme.InsightText

data class AppBottomNavItem(
    val route: String,
    val label: String,
    val iconText: String
)

@Composable
fun AppBottomNavigation(
    items: List<AppBottomNavItem>,
    selectedRoute: String?,
    onItemClick: (AppBottomNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.10f), Color.White.copy(alpha = 0.98f))
                )
            )
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val selected = item.route == selectedRoute
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        color = if (selected) InsightPrimary.copy(alpha = 0.08f) else Color.Transparent,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .clickable { onItemClick(item) }
                    .padding(vertical = 7.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = item.iconText,
                    color = if (selected) InsightPrimary else InsightMutedLight,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 1.dp)
                )
                Text(
                    text = item.label,
                    color = if (selected) InsightText else InsightMutedLight,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Box(
                    modifier = Modifier
                        .padding(top = 1.dp)
                        .size(if (selected) 6.dp else 0.dp)
                        .background(
                            Brush.linearGradient(listOf(InsightPrimary, InsightPrimary2)),
                            CircleShape
                        )
                )
            }
        }
    }
}
