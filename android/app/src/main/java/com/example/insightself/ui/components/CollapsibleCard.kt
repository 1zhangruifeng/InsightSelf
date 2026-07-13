package com.example.insightself.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.insightself.R
import com.example.insightself.ui.theme.InsightMuted
import com.example.insightself.ui.theme.InsightPrimary
import com.example.insightself.ui.theme.InsightText

@Composable
fun CollapsibleCard(
    title: String,
    badge: String? = null,
    isExpandedByDefault: Boolean = false,
    onExpandToggle: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var isExpanded by remember { mutableStateOf(isExpandedByDefault) }

    val actualExpanded = isExpanded
    val onToggle = {
        val newValue = !actualExpanded
        isExpanded = newValue
        onExpandToggle?.invoke(newValue)
    }

    InsightCard(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = InsightText
                )
                if (badge != null) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.bodySmall,
                        color = InsightMuted
                    )
                }
            }
            Icon(
                painter = painterResource(
                    id = if (actualExpanded) R.drawable.ic_expand_less else R.drawable.ic_expand_more
                ),
                contentDescription = if (actualExpanded) "收起" else "展开",
                tint = InsightPrimary
            )
        }

        AnimatedVisibility(
            visible = actualExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier.padding(top = 12.dp)
            ) {
                content()
            }
        }
    }
}