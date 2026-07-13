package com.example.insightself.ui.screens.bazi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.insightself.R
import com.example.insightself.data.model.BaziDto
import com.example.insightself.ui.components.AppTopBar
import com.example.insightself.ui.components.CollapsibleCard
import com.example.insightself.ui.components.EmptyView
import com.example.insightself.ui.components.ErrorView
import com.example.insightself.ui.components.InfoRow
import com.example.insightself.ui.components.InsightCard
import com.example.insightself.ui.components.LoadingView
import com.example.insightself.ui.components.PrimaryActionButton
import com.example.insightself.ui.components.ScoreBar
import com.example.insightself.ui.components.ScoreTone
import com.example.insightself.ui.theme.InsightMuted
import com.example.insightself.ui.theme.InsightPrimary
import com.example.insightself.ui.theme.InsightSpacing
import com.example.insightself.ui.theme.InsightText
import com.example.insightself.viewmodel.BaziViewModel
import com.example.insightself.viewmodel.UiState
import com.example.insightself.data.local.LanguageManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxHeight
import com.example.insightself.ui.theme.InsightPrimary2
import androidx.compose.foundation.horizontalScroll
import com.example.insightself.ui.components.RadarChart
import com.example.insightself.ui.components.RadarData
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height

// 五行颜色映射
val elementColors = mapOf(
    "Wood" to Color(0xFF4CAF50),
    "Fire" to Color(0xFFF44336),
    "Earth" to Color(0xFFFF9800),
    "Metal" to Color(0xFF9E9E9E),
    "Water" to Color(0xFF2196F3),
    "木" to Color(0xFF4CAF50),
    "火" to Color(0xFFF44336),
    "土" to Color(0xFFFF9800),
    "金" to Color(0xFF9E9E9E),
    "水" to Color(0xFF2196F3)
)

// 五行相生相克关系
val generateRelations = mapOf(
    "木" to "火", "火" to "土", "土" to "金", "金" to "水", "水" to "木"
)
val restrictRelations = mapOf(
    "木" to "土", "火" to "金", "土" to "水", "金" to "木", "水" to "火"
)

@Composable
fun BaziScreen(
    baziViewModel: BaziViewModel,
    onNavigateToProfile: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val baziState by baziViewModel.baziState.collectAsState()
    val generating by baziViewModel.generating.collectAsState()
    val context = LocalContext.current

    var currentLanguage by remember { mutableStateOf("en") }
    var selectedYear by remember { mutableStateOf(0) }
    var selectedElementForTooltip by remember { mutableStateOf<String?>(null) }
    var tooltipPosition by remember { mutableStateOf(Offset.Zero) }
    var selectedShishen by remember { mutableStateOf<String?>(null) }

    val isChinese = runBlocking {
        LanguageManager.getLanguageFlow(context).first() == "zh"
    }

    LaunchedEffect(Unit) {
        baziViewModel.loadLatest()
    }

    when (val state = baziState) {
        UiState.Idle,
        UiState.Loading -> LoadingView(modifier = modifier.fillMaxSize(), message = if (isChinese) "加载八字中..." else "Loading Bazi")
        is UiState.Error -> ErrorView(
            message = state.message,
            modifier = modifier.fillMaxSize(),
            onRetry = baziViewModel::loadLatest
        )
        is UiState.Success -> {
            val bazi = state.data
            if (bazi == null) {
                EmptyBaziView(
                    generating = generating,
                    onGenerate = baziViewModel::generate,
                    modifier = modifier,
                    isChinese = isChinese
                )
            } else {
                BaziContent(
                    bazi = bazi,
                    generating = generating,
                    onGenerate = baziViewModel::generate,
                    onRefresh = baziViewModel::loadLatest,
                    onNavigateToProfile = onNavigateToProfile,
                    selectedYear = selectedYear,
                    onYearChange = { selectedYear = it },
                    selectedElementForTooltip = selectedElementForTooltip,
                    onElementTooltip = { element, position ->
                        selectedElementForTooltip = element
                        tooltipPosition = position
                    },
                    selectedShishen = selectedShishen,
                    onShishenClick = { selectedShishen = if (selectedShishen == it) null else it },
                    modifier = modifier,
                    isChinese = isChinese
                )
            }
        }
    }
}

@Composable
private fun EmptyBaziView(
    generating: Boolean,
    onGenerate: () -> Unit,
    modifier: Modifier = Modifier,
    isChinese: Boolean
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = InsightSpacing.ScreenHorizontal),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        AppTopBar(
            title = if (isChinese) "生辰八字" else "Bazi",
            subtitle = if (isChinese) "五行 · 大运 · 建议" else "Five Elements · Fortune · Advice"
        )
        EmptyView(
            message = if (isChinese) "尚未生成八字洞察。准备好后可从您的统一档案创建。" else "No Bazi insight has been generated yet. Create one from your unified profile when you are ready.",
            actionLabel = if (generating) null else if (isChinese) "生成八字洞察" else "Generate Bazi",
            onAction = if (generating) null else onGenerate,
            modifier = Modifier.weight(1f)
        )
        if (generating) {
            LoadingView(modifier = Modifier.height(180.dp), message = if (isChinese) "生成中..." else "Generating")
        }
    }
}

@Composable
private fun BaziContent(
    bazi: BaziDto,
    generating: Boolean,
    onGenerate: () -> Unit,
    onRefresh: () -> Unit,
    onNavigateToProfile: () -> Unit,
    selectedYear: Int,
    onYearChange: (Int) -> Unit,
    selectedElementForTooltip: String?,
    onElementTooltip: (String?, Offset) -> Unit,
    selectedShishen: String?,
    onShishenClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    isChinese: Boolean
) {
    val config = LocalConfiguration.current
    val screenWidth = config.screenWidthDp.dp

    // 模拟流年数据（实际应从后端获取）
    val years = (2024..2035).toList()
    val fortuneYears = listOf(2024, 2025, 2026, 2027, 2028)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = InsightSpacing.ScreenHorizontal),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // 顶部栏保留页面标题，避免首屏只出现孤立刷新图标。
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isChinese) "生辰八字" else "Bazi",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = InsightText
            )
            IconButton(onClick = onRefresh) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_refresh),
                    contentDescription = if (isChinese) "刷新" else "Refresh",
                    tint = InsightPrimary
                )
            }
        }

        // 顶部一句评价
        HeroQuoteCard(bazi = bazi, isChinese = isChinese)

        // 缺少时辰提示
        if (bazi.hourPillar.isNullOrBlank()) {
            MissingHourBanner(onNavigateToProfile = onNavigateToProfile, isChinese = isChinese)
        }

        // 五行雷达图
        RadarChartCard(
            bazi = bazi,
            selectedElement = selectedElementForTooltip,
            onElementClick = { element, position -> onElementTooltip(element, position) },
            isChinese = isChinese
        )

        // 相生相克
        GenerateRestrictCard(
            isChinese = isChinese,
            onElementClick = { element ->
            }
        )

        // 解读区：喜用神、幸运颜色/方位/数字
        InterpretationCard(bazi = bazi, isChinese = isChinese)

        // 建议卡片（可折叠）
        SuggestionCard(bazi = bazi, isChinese = isChinese)

        // 十神柱形图
        ShishenBarChart(
            bazi = bazi,
            selectedShishen = selectedShishen,
            onShishenClick = onShishenClick,
            isChinese = isChinese
        )

        // 流年切换
        if (years.isNotEmpty()) {
            FortuneYearSelector(
                years = years,
                selectedYear = selectedYear,
                onYearChange = onYearChange,
                isChinese = isChinese
            )
        }

        // 重新生成按钮
        PrimaryActionButton(
            text = if (isChinese) "重新生成八字洞察" else "Regenerate Bazi Insight",
            onClick = onGenerate,
            loading = generating,
            enabled = !generating
        )

        Spacer(modifier = Modifier.height(96.dp))
    }
}

@Composable
private fun HeroQuoteCard(bazi: BaziDto, isChinese: Boolean) {
    val strongest = bazi.elementScoreList().maxByOrNull { it.second }
    val strongestName = if (isChinese) {
        when (strongest?.first?.lowercase()) {
            "wood" -> "木"
            "fire" -> "火"
            "earth" -> "土"
            "metal" -> "金"
            "water" -> "水"
            else -> strongest?.first ?: ""
        }
    } else {
        strongest?.first ?: ""
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = InsightPrimary.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isChinese) "天生的「$strongestName」能量主导者" else "Natural \"$strongestName\" Energy Leader",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = InsightPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isChinese)
                    "你的八字格局以${strongestName}能量为主导，建议在生活中平衡五行，发挥优势"
                else
                    "Your Bazi pattern is dominated by $strongestName energy. Balance the five elements in daily life.",
                style = MaterialTheme.typography.bodyMedium,
                color = InsightMuted,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun MissingHourBanner(onNavigateToProfile: () -> Unit, isChinese: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToProfile() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF3E0)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚠️",
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isChinese)
                    "未填写出生时辰，八字排盘仅供参考。点击前往补充"
                else
                    "Birth time not set. Bazi calculation is for reference only. Tap to complete",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFE65100),
                modifier = Modifier.weight(1f)
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_forward),
                contentDescription = null,
                tint = Color(0xFFE65100)
            )
        }
    }
}

@Composable
private fun RadarChartCard(
    bazi: BaziDto,
    selectedElement: String?,
    onElementClick: (String, Offset) -> Unit,
    isChinese: Boolean
) {
    CollapsibleCard(
        title = if (isChinese) "五行能量雷达" else "Five Elements Radar",
        isExpandedByDefault = true
    ) {
        val elements = bazi.elementScoreList()

        // 确保五行顺序正确（木、火、土、金、水）
        val orderedElements = listOf("Wood", "Fire", "Earth", "Metal", "Water")

        // 构建雷达图数据（按固定顺序）
        val radarData = orderedElements.mapNotNull { element ->
            val score = elements.find { it.first == element }?.second ?: return@mapNotNull null
            val displayName = if (isChinese) {
                when (element.lowercase()) {
                    "wood" -> "木"
                    "fire" -> "火"
                    "earth" -> "土"
                    "metal" -> "金"
                    "water" -> "水"
                    else -> element
                }
            } else {
                element
            }
            val color = elementColors[displayName] ?: elementColors[element] ?: Color.Gray
            RadarData(
                label = displayName,
                value = score,
                color = color
            )
        }

        if (radarData.isNotEmpty()) {
            // 雷达图 - 居中显示
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                RadarChart(
                    data = radarData,
                    modifier = Modifier
                )
            }
        } else {
            Text(
                text = if (isChinese) "暂无五行数据" else "No element data",
                style = MaterialTheme.typography.bodyMedium,
                color = InsightMuted,
                modifier = Modifier.padding(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 图例
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            radarData.forEach { data ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable {
                            onElementClick(data.label, Offset.Zero)
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(data.color)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = data.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = InsightText
                    )
                    Text(
                        text = "${data.value}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = data.color
                    )
                }
            }
        }

        // Tooltip
        selectedElement?.let { element ->
            Card(
                modifier = Modifier
                    .padding(8.dp)
                    .width(140.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.8f))
            ) {
                val score = radarData.find { it.label == element }?.value ?: 0
                Text(
                    text = if (isChinese) "${element}能量：$score" else "$element: $score",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Composable
private fun GenerateRestrictCard(
    isChinese: Boolean,
    onElementClick: (String) -> Unit = {}
) {
    CollapsibleCard(
        title = if (isChinese) "相生相克" else "Generate & Restrict",
        isExpandedByDefault = false
    ) {
        // 五行说明映射
        val elementExplanations = mapOf(
            "木" to if (isChinese) "木元素代表生长、发展、创造力。木旺的人积极进取，有领导才能。" else "Wood represents growth, development, creativity. Strong wood indicates进取心 and leadership.",
            "火" to if (isChinese) "火元素代表热情、活力、行动力。火旺的人充满激情，善于表达。" else "Fire represents passion, vitality, action. Strong fire indicates enthusiasm and expression.",
            "土" to if (isChinese) "土元素代表稳定、包容、承载。土旺的人踏实可靠，善于协调。" else "Earth represents stability,包容,承载. Strong earth indicates reliability and coordination.",
            "金" to if (isChinese) "金元素代表决断、原则、变革。金旺的人果断刚毅，有魄力。" else "Metal represents decision, principle, change. Strong metal indicates decisiveness and courage.",
            "水" to if (isChinese) "水元素代表智慧、流动、适应。水旺的人聪明灵活，善于变通。" else "Water represents wisdom, flow, adaptation. Strong water indicates intelligence and flexibility."
        )

        var selectedElement by remember { mutableStateOf<String?>(null) }

        // 相生
        Text(
            text = if (isChinese) "相生" else "Generate",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = InsightPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 相生行 - 使用 Row 固定宽度对齐
        val generateElements = listOf("木", "火", "土", "金", "水")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            generateElements.forEachIndexed { index, element ->
                // 五行圆形按钮
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(elementColors[element] ?: Color.Gray)
                        .clickable {
                            selectedElement = if (selectedElement == element) null else element
                            onElementClick(element)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = element,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                // 箭头容器（固定宽度，确保对齐）
                if (index < generateElements.size - 1) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier.width(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "→",
                            fontSize = 16.sp,
                            color = InsightMuted
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }

        // 显示选中五行的说明
        if (selectedElement != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = InsightPrimary.copy(alpha = 0.1f))
            ) {
                Text(
                    text = elementExplanations[selectedElement] ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = InsightMuted,
                    modifier = Modifier.padding(12.dp),
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 相克
        Text(
            text = if (isChinese) "相克" else "Restrict",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFF44336)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 相克行 - 使用 Row 固定宽度对齐
        val restrictElements = listOf("木", "土", "水", "火", "金")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            restrictElements.forEachIndexed { index, element ->
                // 五行圆形按钮
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(elementColors[element] ?: Color.Gray)
                        .clickable {
                            selectedElement = if (selectedElement == element) null else element
                            onElementClick(element)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = element,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                // 叉号容器（固定宽度，确保对齐）
                if (index < restrictElements.size - 1) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier.width(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✗",
                            fontSize = 16.sp,
                            color = Color(0xFFF44336),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }

        // 提示文字
        Text(
            text = if (isChinese) "← 滑动查看 →  点击五行图标查看详细说明" else "← Swipe →  Tap on element icons for details",
            style = MaterialTheme.typography.bodySmall,
            color = InsightMuted,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
private fun InterpretationCard(bazi: BaziDto, isChinese: Boolean) {
    val strongest = bazi.elementScoreList().maxByOrNull { it.second }
    val strongestName = if (isChinese) {
        when (strongest?.first?.lowercase()) {
            "wood" -> "木"
            "fire" -> "火"
            "earth" -> "土"
            "metal" -> "金"
            "water" -> "水"
            else -> strongest?.first ?: ""
        }
    } else {
        strongest?.first ?: ""
    }

    CollapsibleCard(
        title = if (isChinese) "解读区" else "Interpretation",
        isExpandedByDefault = true
    ) {
        // 喜用神
        InfoRow(
            label = if (isChinese) "喜用神" else "Favorite Element",
            value = if (isChinese) "金、水" else "Metal, Water"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 幸运信息
        Text(
            text = if (isChinese) "幸运信息" else "Lucky Information",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = InsightText
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LuckyInfoCard(
                title = if (isChinese) "幸运颜色" else "Lucky Color",
                value = if (isChinese) "白色、金色" else "White, Gold",
                modifier = Modifier.weight(1f)
            )
            LuckyInfoCard(
                title = if (isChinese) "幸运方位" else "Lucky Direction",
                value = if (isChinese) "西北" else "Northwest",
                modifier = Modifier.weight(1f)
            )
            LuckyInfoCard(
                title = if (isChinese) "幸运数字" else "Lucky Number",
                value = "6, 7, 8",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun LuckyInfoCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = InsightPrimary.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = InsightMuted
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = InsightPrimary
            )
        }
    }
}

@Composable
private fun SuggestionCard(bazi: BaziDto, isChinese: Boolean) {
    val strongest = bazi.elementScoreList().maxByOrNull { it.second }
    val weakest = bazi.elementScoreList().minByOrNull { it.second }

    val strongestName = if (isChinese) {
        when (strongest?.first?.lowercase()) {
            "wood" -> "木"
            "fire" -> "火"
            "earth" -> "土"
            "metal" -> "金"
            "water" -> "水"
            else -> strongest?.first ?: ""
        }
    } else {
        strongest?.first ?: ""
    }
    val weakestName = if (isChinese) {
        when (weakest?.first?.lowercase()) {
            "wood" -> "木"
            "fire" -> "火"
            "earth" -> "土"
            "metal" -> "金"
            "water" -> "水"
            else -> weakest?.first ?: ""
        }
    } else {
        weakest?.first ?: ""
    }

    CollapsibleCard(
        title = if (isChinese) "建议" else "Suggestions",
        isExpandedByDefault = true
    ) {
        // 行业建议
        SuggestionItem(
            title = if (isChinese) "💼 行业建议" else "💼 Career Advice",
            content = if (isChinese)
                "适合从事创意、咨询、教育培训、文化艺术等需要表达和沟通的行业。避免过于机械和重复的工作。"
            else
                "Suitable for creative, consulting, education, and cultural industries that require expression and communication. Avoid overly mechanical and repetitive work."
        )

        // 伴侣契合
        SuggestionItem(
            title = if (isChinese) "❤️ 伴侣契合" else "❤️ Partner Compatibility",
            content = if (isChinese)
                "与${if (weakestName == "木") "水" else if (weakestName == "火") "木" else if (weakestName == "土") "火" else if (weakestName == "金") "土" else "金"}元素旺盛的人较为契合，能形成互补。"
            else
                "More compatible with people who have strong ${if (weakestName == "Wood") "Water" else if (weakestName == "Fire") "Wood" else if (weakestName == "Earth") "Fire" else if (weakestName == "Metal") "Earth" else "Metal"} element."
        )

        // 大运（可折叠）
        CollapsibleCard(
            title = if (isChinese) "📈 大运走向" else "📈 Fortune Trend",
            isExpandedByDefault = false
        ) {
            Text(
                text = if (isChinese)
                    "当前大运（2020-2029）：偏重资源整合和人际关系，适合建立合作网络。\n\n下一大运（2030-2039）：事业上升期，宜把握机会，大胆开拓。"
                else
                    "Current fortune (2020-2029): Focus on resource integration and relationships, suitable for building networks.\n\nNext fortune (2030-2039): Career rising period, seize opportunities.",
                style = MaterialTheme.typography.bodySmall,
                color = InsightMuted,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun SuggestionItem(title: String, content: String) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = InsightText
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodySmall,
            color = InsightMuted,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun ShishenBarChart(
    bazi: BaziDto,
    selectedShishen: String?,
    onShishenClick: (String) -> Unit,
    isChinese: Boolean
) {
    // 十神数据
    val shishenData = listOf(
        "比肩" to 28,
        "食神" to 22,
        "偏财" to 18,
        "七杀" to 16,
        "正印" to 16
    )

    val shishenExplanations = mapOf(
        "比肩" to if (isChinese) "代表同辈、朋友、竞争者。比肩旺的人独立自主，有领导力，但需注意合作。" else "Represents peers, friends, competitors. Strong indicates independence and leadership.",
        "食神" to if (isChinese) "代表才华、表达、享受。食神旺的人有艺术天赋，善于沟通。" else "Represents talent, expression, enjoyment. Strong indicates artistic talent.",
        "偏财" to if (isChinese) "代表投资、意外之财、人际关系。偏财旺的人有商业头脑。" else "Represents investment, unexpected wealth, relationships. Strong indicates business acumen.",
        "七杀" to if (isChinese) "代表权威、压力、挑战。七杀旺的人有魄力，能承受压力。" else "Represents authority, pressure, challenges. Strong indicates courage.",
        "正印" to if (isChinese) "代表学识、贵人、支持。正印旺的人学习能力强，易得帮助。" else "Represents knowledge, mentors, support. Strong indicates learning ability."
    )

    val maxScore = shishenData.maxOfOrNull { it.second } ?: 100

    CollapsibleCard(
        title = if (isChinese) "十神柱形图" else "Ten Gods Chart",
        isExpandedByDefault = false
    ) {
        shishenData.forEach { (name, score) ->
            val isSelected = selectedShishen == name
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onShishenClick(name) }
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) InsightPrimary else InsightText,
                        modifier = Modifier.width(50.dp)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) InsightPrimary.copy(alpha = 0.2f) else Color(0xFFE8ECF1))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth((score.toFloat() / maxScore).coerceIn(0f, 1f))
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    brush = if (isSelected)
                                        Brush.horizontalGradient(listOf(InsightPrimary, InsightPrimary2))
                                    else
                                        Brush.horizontalGradient(listOf(Color(0xFF9E9E9E), Color(0xFFBDBDBD)))
                                )
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$score%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) InsightPrimary else InsightMuted,
                        modifier = Modifier.width(40.dp)
                    )
                }

                // 选中时显示解释
                if (isSelected) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = InsightPrimary.copy(alpha = 0.08f))
                    ) {
                        Text(
                            text = shishenExplanations[name] ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = InsightMuted,
                            modifier = Modifier.padding(12.dp),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FortuneYearSelector(
    years: List<Int>,
    selectedYear: Int,
    onYearChange: (Int) -> Unit,
    isChinese: Boolean
) {
    CollapsibleCard(
        title = if (isChinese) "流年运势" else "Yearly Fortune",
        isExpandedByDefault = false
    ) {
        Text(
            text = if (isChinese) "选择年份查看详细流年分析" else "Select year for detailed analysis",
            style = MaterialTheme.typography.bodySmall,
            color = InsightMuted,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(years) { year ->
                val isSelected = selectedYear == year
                Card(
                    modifier = Modifier
                        .clickable { onYearChange(year) }
                        .width(70.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) InsightPrimary else Color.White
                    )
                ) {
                    Text(
                        text = year.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else InsightText,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        if (selectedYear != 0) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = InsightPrimary.copy(alpha = 0.08f))
            ) {
                Text(
                    text = if (isChinese)
                        "$selectedYear 年运势：事业稳步上升，感情方面需多加沟通，健康注意作息规律。"
                    else
                        "$selectedYear fortune: Career stable with upward trend, need more communication in relationships, pay attention to regular作息.",
                    style = MaterialTheme.typography.bodySmall,
                    color = InsightMuted,
                    modifier = Modifier.padding(12.dp),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// 扩展函数
private fun BaziDto.elementScoreList(): List<Pair<String, Int>> {
    val fromMap = elementScores?.mapNotNull { (key, value) -> key.takeIf { it.isNotBlank() }?.let { it to value } }
    if (!fromMap.isNullOrEmpty()) return fromMap
    return listOf(
        "Wood" to (woodScore ?: 0),
        "Fire" to (fireScore ?: 0),
        "Earth" to (earthScore ?: 0),
        "Metal" to (metalScore ?: 0),
        "Water" to (waterScore ?: 0)
    )
}
