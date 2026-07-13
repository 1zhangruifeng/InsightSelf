package com.example.insightself.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.insightself.ui.theme.InsightMuted
import com.example.insightself.ui.theme.InsightPrimary
import com.example.insightself.ui.theme.InsightText
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

data class ZodiacInterpretation(
    val sunSign: String,
    val sunSignKey: String? = null,
    val sunDescription: String,
    val moonSign: String,
    val moonSignKey: String? = null,
    val moonDescription: String,
    val risingSign: String,
    val risingSignKey: String? = null,
    val risingDescription: String
)

@Composable
fun ZodiacInterpretationCard(
    interpretation: ZodiacInterpretation,
    isChinese: Boolean,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(
        if (isChinese) "太阳" else "Sun",
        if (isChinese) "月亮" else "Moon",
        if (isChinese) "上升" else "Rising"
    )

    Column(modifier = modifier) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            indicator = { tabPositions ->
                if (selectedTab < tabPositions.size) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[selectedTab])
                            .padding(horizontal = 16.dp)
                            .height(3.dp)
                            .width(tabPositions[selectedTab].width - 32.dp)
                            .background(InsightPrimary)
                    )
                }
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            color = if (selectedTab == index) InsightText else InsightMuted,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                )
            }
        }

        // 内容区域 - 直接显示完整内容，没有展开/收起按钮
        when (selectedTab) {
            0 -> ZodiacTabContent(
                title = interpretation.sunSign,
                signKey = interpretation.sunSignKey,
                description = interpretation.sunDescription,
                isChinese = isChinese
            )
            1 -> ZodiacTabContent(
                title = interpretation.moonSign,
                signKey = interpretation.moonSignKey,
                description = interpretation.moonDescription,
                isChinese = isChinese
            )
            2 -> ZodiacTabContent(
                title = interpretation.risingSign,
                signKey = interpretation.risingSignKey,
                description = interpretation.risingDescription,
                isChinese = isChinese
            )
        }
    }
}

@Composable
private fun ZodiacTabContent(
    title: String,
    signKey: String?,
    description: String,
    isChinese: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ZodiacSignIcon(
                sign = signKey,
                size = 48.dp,
                contentDescription = title
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = InsightText
            )
        }
        Spacer(modifier = Modifier.height(4.dp))

        // 直接显示完整内容，不截断，没有展开更多按钮
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = InsightMuted,
            lineHeight = 22.sp
        )
    }
}