package com.example.insightself.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.insightself.R
import com.example.insightself.data.local.LanguageManager
import com.example.insightself.data.local.UserSessionDataStore
import com.example.insightself.data.model.AssessmentResultDto
import com.example.insightself.data.model.BaziDto
import com.example.insightself.data.model.DashboardDto
import com.example.insightself.data.model.ProfileDto
import com.example.insightself.data.model.ZodiacDailyDto
import com.example.insightself.ui.components.AiChatBottomSheet
import com.example.insightself.ui.components.CollapsibleCard
import com.example.insightself.ui.components.ErrorView
import com.example.insightself.ui.components.FortuneData
import com.example.insightself.ui.components.InfoRow
import com.example.insightself.ui.components.InsightCard
import com.example.insightself.ui.components.LoadingView
import com.example.insightself.ui.components.PrimaryActionButton
import com.example.insightself.ui.components.ScoreBar
import com.example.insightself.ui.components.ScoreTone
import com.example.insightself.ui.components.SecondaryActionButton
import com.example.insightself.ui.components.SectionTitle
import com.example.insightself.ui.components.TodayFortuneCard
import com.example.insightself.ui.components.ZodiacInterpretation
import com.example.insightself.ui.components.ZodiacInterpretationCard
import com.example.insightself.ui.screens.assessment.assessmentConfigs
import com.example.insightself.ui.theme.InsightMuted
import com.example.insightself.ui.theme.InsightCardStrong
import com.example.insightself.ui.theme.InsightPrimary
import com.example.insightself.ui.theme.InsightPrimary2
import com.example.insightself.ui.theme.InsightShapes
import com.example.insightself.ui.theme.InsightSpacing
import com.example.insightself.ui.theme.InsightStroke
import com.example.insightself.ui.theme.InsightText
import com.example.insightself.util.AssessmentTypeSnapshot
import com.example.insightself.util.ZodiacSignUtils
import com.example.insightself.util.displayAssessmentResultLabel
import com.example.insightself.util.latestAssessmentSnapshots
import com.example.insightself.viewmodel.AiChatViewModel
import com.example.insightself.viewmodel.DashboardDestination
import com.example.insightself.viewmodel.DashboardViewModel
import com.example.insightself.viewmodel.UiState
import com.example.insightself.viewmodel.ZodiacViewModel
import java.util.Calendar
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.width
import com.example.insightself.ui.components.PieChart
import com.example.insightself.ui.components.PieSlice
import androidx.compose.foundation.background
import androidx.compose.ui.text.style.TextOverflow

// ==================== 问候语工具函数 ====================

/**
 * 根据当前时间和语言获取问候语
 */
private fun getTimeBasedGreeting(isChinese: Boolean): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour in 5..11 -> if (isChinese) "早上好" else "Good morning"
        hour in 12..17 -> if (isChinese) "下午好" else "Good afternoon"
        else -> if (isChinese) "晚上好" else "Good evening"
    }
}

/**
 * 获取显示用的昵称
 */
private fun getDisplayNickname(nickname: String?, isChinese: Boolean): String {
    if (!nickname.isNullOrBlank()) return nickname
    return if (isChinese) "用户" else "User"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDashboardScreen(
    dashboardViewModel: DashboardViewModel,
    zodiacViewModel: ZodiacViewModel,
    onMissingSession: () -> Unit,
    onProfileMissing: () -> Unit,
    onOpenReport: () -> Unit,
    onNavigateToAssessments: () -> Unit,
    onNavigateToAssessmentResult: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val dashboardState by dashboardViewModel.dashboardState.collectAsState()
    val destination by dashboardViewModel.destination.collectAsState()
    val context = LocalContext.current
    val appContext = context.applicationContext
    val sessionDataStore = remember { UserSessionDataStore(appContext) }

    var currentLanguage by remember { mutableStateOf(LanguageManager.currentLanguageBlocking(appContext)) }
    var showAiChat by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    LaunchedEffect(Unit) {
        com.example.insightself.data.auth.SessionAuth.init(appContext)
        com.example.insightself.data.auth.SessionAuth.hydrateAccessToken()
    }

    LaunchedEffect(Unit) {
        LanguageManager.getLanguageFlow(appContext).collect { lang ->
            val languageChanged = currentLanguage != lang
            currentLanguage = lang
            dashboardViewModel.loadDashboard(forceRefresh = true)
            zodiacViewModel.loadNatal(forceRefresh = languageChanged)
        }
    }

    LaunchedEffect(destination) {
        if (destination == DashboardDestination.Onboarding) {
            dashboardViewModel.clearDestination()
            onProfileMissing()
        }
    }

    when (val state = dashboardState) {
        UiState.Idle,
        UiState.Loading -> LoadingView(modifier = modifier.fillMaxSize())
        is UiState.Error -> {
            val showLoginAgain = state.message.contains("log in", ignoreCase = true)
            ErrorView(
                message = state.message,
                modifier = modifier.fillMaxSize(),
                onRetry = dashboardViewModel::loadDashboard,
                secondaryActionLabel = if (showLoginAgain) {
                    stringResource(R.string.log_in_again)
                } else {
                    null
                },
                onSecondaryAction = if (showLoginAgain) onMissingSession else null
            )
        }
        is UiState.Success -> Box(modifier = modifier.fillMaxSize()) {
            val natalState by zodiacViewModel.natalState.collectAsState()
            val natalChart = remember(natalState) { zodiacViewModel.natalChartForDisplay() }
            DashboardContent(
                dashboard = state.data,
                natalState = natalState,
                natalChart = natalChart,
                onRefresh = {
                    dashboardViewModel.loadDashboard(forceRefresh = true)
                    zodiacViewModel.loadNatal(forceRefresh = true)
                },
                onOpenChat = { showAiChat = true },
                onOpenReport = onOpenReport,
                onNavigateToAssessments = onNavigateToAssessments,
                onNavigateToAssessmentResult = onNavigateToAssessmentResult,
                modifier = Modifier.fillMaxSize(),
                isChinese = currentLanguage == "zh"
            )

            if (showAiChat) {
                val aiChatViewModel: AiChatViewModel = viewModel(
                    factory = AiChatViewModel.factory(sessionDataStore)
                )
                val messages by aiChatViewModel.messages.collectAsState()
                val isLoading by aiChatViewModel.isLoading.collectAsState()

                LaunchedEffect(showAiChat) {
                    if (showAiChat) {
                        aiChatViewModel.loadInitialMessage(currentLanguage == "zh")
                    }
                }

                ModalBottomSheet(
                    onDismissRequest = { showAiChat = false },
                    sheetState = sheetState,
                    containerColor = Color(0xFFF3FBFF),
                    modifier = Modifier.fillMaxSize()
                ) {
                    AiChatBottomSheet(
                        messages = messages,
                        isLoading = isLoading,
                        onSendMessage = { aiChatViewModel.sendMessage(it, currentLanguage == "zh") },
                        onDismiss = { showAiChat = false },
                        isChinese = currentLanguage == "zh"
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardContent(
    dashboard: DashboardDto,
    natalState: UiState<com.example.insightself.data.model.ZodiacNatalDto>,
    natalChart: com.example.insightself.data.model.ZodiacNatalDto?,
    onRefresh: () -> Unit,
    onOpenChat: () -> Unit,
    onOpenReport: () -> Unit,
    onNavigateToAssessments: () -> Unit,
    onNavigateToAssessmentResult: (Long) -> Unit,
    modifier: Modifier = Modifier,
    isChinese: Boolean
) {
    val profile = dashboard.profile
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = InsightSpacing.ScreenHorizontal),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        DashboardHeader(
            profile = profile,
            onRefresh = onRefresh,
            onOpenChat = onOpenChat,
            isChinese = isChinese
        )

        // 卡片一：今日运势
        CollapsibleCard(
            title = if (isChinese) "今日运势" else "Today's Fortune",
            isExpandedByDefault = true
        ) {
            val fortuneData = FortuneData(
                score = 78,
                luckyColor = if (isChinese) "雾蓝" else "Mist Blue",
                luckyDirection = if (isChinese) "东南" else "Southeast",
                luckyNumber = "3 · 8",
                dos = if (isChinese) listOf("与朋友深度对话", "整理桌面与日程") else listOf("Deep conversation with friends", "Organize desk and schedule"),
                donts = if (isChinese) listOf("冲动签约", "熬夜赶工") else listOf("Impulsive signing", "Stay up late")
            )
            TodayFortuneCard(fortune = fortuneData, isChinese = isChinese)
        }

        // 卡片二：星象解读
        CollapsibleCard(
            title = if (isChinese) "星象解读" else "Zodiac Interpretation",
            badge = if (isChinese) "日 · 月 · 升" else "Sun · Moon · Rising",
            isExpandedByDefault = true
        ) {
            when {
                natalChart != null -> {
                    val interpretation = ZodiacSignUtils.interpretationFromNatal(natalChart, isChinese)
                    ZodiacInterpretationCard(interpretation = interpretation, isChinese = isChinese)
                }
                natalState is UiState.Loading || natalState is UiState.Idle -> {
                    LoadingView(message = if (isChinese) "加载星盘中..." else "Loading chart...")
                }
                natalState is UiState.Error -> {
                    Text(
                        text = natalState.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                else -> Unit
            }
        }

        // 卡片三：八字亮点
        BaziHighlightCard(bazi = dashboard.bazi, isChinese = isChinese)

        // 卡片四：最新心理测评结论
        AssessmentSnapshotCard(
            results = dashboard.assessmentResults.orEmpty(),
            isChinese = isChinese,
            onNavigateToAssessments = onNavigateToAssessments,
            onNavigateToAssessmentResult = onNavigateToAssessmentResult
        )

        // 卡片五：综合洞察
        IntegratedInsightCard(dashboard = dashboard, isChinese = isChinese)

        // 卡片六：报告生成
        LatestReportCard(
            reportText = dashboard.latestAiReport?.reportText,
            generatedBy = dashboard.latestAiReport?.generatedBy,
            onOpenReport = onOpenReport,
            isChinese = isChinese
        )
        Spacer(modifier = Modifier.height(96.dp))
    }
}

@Composable
private fun DashboardHeader(
    profile: ProfileDto?,
    onRefresh: () -> Unit,
    onOpenChat: () -> Unit,
    isChinese: Boolean
) {
    val nickname = getDisplayNickname(profile?.nickname, isChinese)
    val greeting = getTimeBasedGreeting(isChinese)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    listOf(InsightPrimary.copy(alpha = 0.13f), InsightPrimary2.copy(alpha = 0.10f), Color.White)
                ),
                shape = RoundedCornerShape(28.dp)
            )
            .border(
                BorderStroke(1.dp, InsightPrimary.copy(alpha = 0.12f)),
                RoundedCornerShape(28.dp)
            )
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (isChinese) "$nickname，$greeting" else "$nickname, $greeting",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = InsightText
                )
                Text(
                    text = if (isChinese) "把八字、星象和测评整理成今天可执行的自我洞察"
                    else "Bazi, zodiac, and assessments organized into one daily reflection",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InsightMuted
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(
                    onClick = onOpenChat,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(InsightPrimary, InsightPrimary2)))
                ) {
                    Text(
                        text = "AI",
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(InsightShapes.ControlRadius))
                        .background(InsightCardStrong)
                        .border(
                            BorderStroke(1.dp, InsightStroke),
                            RoundedCornerShape(InsightShapes.ControlRadius)
                        )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_refresh),
                        contentDescription = if (isChinese) "刷新" else "Refresh",
                        tint = InsightPrimary
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HomeHeaderMetric(
                label = if (isChinese) "今日状态" else "Today",
                value = "78",
                modifier = Modifier.weight(1f)
            )
            HomeHeaderMetric(
                label = if (isChinese) "已完成测评" else "Tests",
                value = "Ready",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun HomeHeaderMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.72f), RoundedCornerShape(16.dp))
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.9f)), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = InsightMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = InsightText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ==================== 映射函数 ====================

private fun getZodiacSignName(sign: String?, isChinese: Boolean): String {
    if (sign.isNullOrBlank()) return if (isChinese) "未知" else "Unknown"
    return when (sign) {
        "Aries" -> if (isChinese) "白羊座" else "Aries"
        "Taurus" -> if (isChinese) "金牛座" else "Taurus"
        "Gemini" -> if (isChinese) "双子座" else "Gemini"
        "Cancer" -> if (isChinese) "巨蟹座" else "Cancer"
        "Leo" -> if (isChinese) "狮子座" else "Leo"
        "Virgo" -> if (isChinese) "处女座" else "Virgo"
        "Libra" -> if (isChinese) "天秤座" else "Libra"
        "Scorpio" -> if (isChinese) "天蝎座" else "Scorpio"
        "Sagittarius" -> if (isChinese) "射手座" else "Sagittarius"
        "Capricorn" -> if (isChinese) "摩羯座" else "Capricorn"
        "Aquarius" -> if (isChinese) "水瓶座" else "Aquarius"
        "Pisces" -> if (isChinese) "双鱼座" else "Pisces"
        else -> sign
    }
}

private fun getZodiacSuggestion(suggestion: String?, isChinese: Boolean): String {
    if (!isChinese) {
        return suggestion?.takeIf { it.isNotBlank() } ?: "Choose one small action and keep the pace kind to yourself."
    }
    if (suggestion.isNullOrBlank() ||
        suggestion.contains("Today may support") ||
        suggestion.contains("small, observable choices")) {
        return "今天适合做一些小而确定的行动。将日常运势视为温和的计划提示，而非预测。"
    }
    return suggestion
}

private fun getElementChinese(element: String): String {
    return when (element.lowercase()) {
        "wood" -> "木"
        "fire" -> "火"
        "earth" -> "土"
        "metal" -> "金"
        "water" -> "水"
        else -> element
    }
}

private fun getPillarChinese(pillar: String): String {
    if (pillar.isBlank()) return pillar
    val stemMap = mapOf(
        "Jia Wood" to "甲木", "Yi Wood" to "乙木",
        "Bing Fire" to "丙火", "Ding Fire" to "丁火",
        "Wu Earth" to "戊土", "Ji Earth" to "己土",
        "Geng Metal" to "庚金", "Xin Metal" to "辛金",
        "Ren Water" to "壬水", "Gui Water" to "癸水"
    )
    val branchMap = mapOf(
        "Zi Rat" to "子鼠", "Chou Ox" to "丑牛",
        "Yin Tiger" to "寅虎", "Mao Rabbit" to "卯兔",
        "Chen Dragon" to "辰龙", "Si Snake" to "巳蛇",
        "Wu Horse" to "午马", "Wei Goat" to "未羊",
        "Shen Monkey" to "申猴", "You Rooster" to "酉鸡",
        "Xu Dog" to "戌狗", "Hai Pig" to "亥猪"
    )
    var result = pillar
    stemMap.forEach { (eng, chn) -> result = result.replace(eng, chn) }
    branchMap.forEach { (eng, chn) -> result = result.replace(eng, chn) }
    return result
}

private fun getDimensionLabel(dimension: String, isChinese: Boolean): String {
    if (!isChinese) return dimension
    return when (dimension) {
        "Extraversion" -> "外向性"
        "Agreeableness" -> "宜人性"
        "Conscientiousness" -> "尽责性"
        "Neuroticism" -> "神经质"
        "Openness" -> "开放性"
        "Security" -> "安全型"
        "Anxiety" -> "焦虑型"
        "Avoidance" -> "回避型"
        else -> dimension
    }
}

// ==================== UI 组件 ====================

@Composable
private fun BaziHighlightCard(bazi: BaziDto?, isChinese: Boolean) {
    CollapsibleCard(
        title = if (isChinese) "八字亮点" else "Bazi Highlights",
        isExpandedByDefault = true
    ) {
        if (bazi == null) {
            Text(
                text = if (isChinese) "八字亮点将在后端读取档案后显示于此。" else "Bazi highlights will show here once the backend has a profile to read.",
                style = MaterialTheme.typography.bodyMedium,
                color = InsightMuted,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            return@CollapsibleCard
        }

        // 获取五行分数数据
        val elementScores = bazi.elementScoreList()

        // 五行颜色映射
        val elementColors = mapOf(
            "Wood" to Color(0xFF4CAF50),   // 木 - 绿色
            "Fire" to Color(0xFFF44336),    // 火 - 红色
            "Earth" to Color(0xFFFF9800),   // 土 - 橙色
            "Metal" to Color(0xFF9E9E9E),   // 金 - 灰色
            "Water" to Color(0xFF2196F3)    // 水 - 蓝色
        )

        val chineseNames = mapOf(
            "Wood" to "木",
            "Fire" to "火",
            "Earth" to "土",
            "Metal" to "金",
            "Water" to "水"
        )

        // 构建饼图数据
        val pieSlices = elementScores.map { (element, score) ->
            PieSlice(
                label = if (isChinese) chineseNames[element] ?: element else element,
                value = score,
                color = elementColors[element] ?: Color.Gray
            )
        }

        // 找出最强和最弱元素
        val strongest = elementScores.maxByOrNull { it.second }
        val weakest = elementScores.minByOrNull { it.second }

        val strongestName = if (isChinese) {
            chineseNames[strongest?.first ?: ""] ?: (strongest?.first ?: "")
        } else {
            strongest?.first ?: ""
        }
        val weakestName = if (isChinese) {
            chineseNames[weakest?.first ?: ""] ?: (weakest?.first ?: "")
        } else {
            weakest?.first ?: ""
        }

        // 饼状图
        PieChart(
            slices = pieSlices,
            isChinese = isChinese
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 最强和辅助标记
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InsightCard(
                modifier = Modifier.weight(1f),
                title = if (isChinese) "最强" else "Strongest",
                badge = null
            ) {
                Text(
                    text = strongestName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
                Text(
                    text = if (isChinese) "主导元素" else "Dominant element",
                    style = MaterialTheme.typography.bodySmall,
                    color = InsightMuted
                )
            }

            InsightCard(
                modifier = Modifier.weight(1f),
                title = if (isChinese) "辅助" else "Gentle Support",
                badge = null
            ) {
                Text(
                    text = weakestName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF9800)
                )
                Text(
                    text = if (isChinese) "需要平衡的元素" else "Element to balance",
                    style = MaterialTheme.typography.bodySmall,
                    color = InsightMuted
                )
            }
        }
    }
}

@Composable
private fun AssessmentSnapshotCard(
    results: List<AssessmentResultDto>,
    isChinese: Boolean,
    onNavigateToAssessments: () -> Unit,
    onNavigateToAssessmentResult: (Long) -> Unit
) {
    val snapshots = remember(results) { latestAssessmentSnapshots(results) }
    val completedCount = snapshots.count { it.latestResult != null }

    InsightCard(
        title = stringResource(R.string.latest_assessments),
        badge = "${completedCount}/${snapshots.size}"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            snapshots.forEach { snapshot ->
                AssessmentSnapshotRow(
                    snapshot = snapshot,
                    isChinese = isChinese,
                    onViewResult = { resultId -> onNavigateToAssessmentResult(resultId) }
                )
            }
            if (completedCount < snapshots.size) {
                PrimaryActionButton(
                    text = stringResource(R.string.take_assessment),
                    onClick = onNavigateToAssessments,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun AssessmentSnapshotRow(
    snapshot: AssessmentTypeSnapshot,
    isChinese: Boolean,
    onViewResult: (Long) -> Unit
) {
    val result = snapshot.latestResult
    val typeName = if (isChinese) snapshot.config.displayNameZh else snapshot.config.displayNameEn
    val statusText = if (result != null) {
        displayAssessmentResultLabel(result.resultLabel, isChinese)
    } else {
        stringResource(R.string.assessment_not_taken)
    }
    val summaryText = result?.summary?.takeIf { it.isNotBlank() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (result?.id != null) {
                    Modifier.clickable { onViewResult(result.id!!) }
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = InsightCardStrong)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = snapshot.config.icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = typeName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = InsightText
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (result != null) InsightPrimary else InsightMuted,
                    fontWeight = if (result != null) FontWeight.SemiBold else FontWeight.Normal
                )
                if (!summaryText.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = summaryText,
                        style = MaterialTheme.typography.bodySmall,
                        color = InsightMuted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (result?.id != null) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_forward),
                    contentDescription = stringResource(R.string.view_details),
                    tint = InsightMuted
                )
            }
        }
    }
}

@Composable
private fun IntegratedInsightCard(dashboard: DashboardDto, isChinese: Boolean) {
    InsightCard(
        title = if (isChinese) "综合洞察" else "Integrated Insight",
        badge = if (isChinese) "摘要" else "Summary"
    ) {
        val insight = localizedIntegratedInsight(dashboard, isChinese)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = insight.summary,
                style = MaterialTheme.typography.bodyLarge,
                color = InsightText,
                fontWeight = FontWeight.Medium
            )
            insight.points.forEachIndexed { index, point ->
                IntegratedInsightFactRow(
                    label = point.first,
                    value = point.second,
                    tone = when (index % 3) {
                        0 -> InsightPrimary
                        1 -> Color(0xFF16A34A)
                        else -> Color(0xFFFF9800)
                    }
                )
            }
        }
    }
}

@Composable
private fun IntegratedInsightFactRow(label: String, value: String, tone: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(tone.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .background(tone, CircleShape)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = tone,
            modifier = Modifier.width(70.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = InsightText,
            modifier = Modifier.weight(1f)
        )
    }
}

private data class IntegratedInsightView(
    val summary: String,
    val points: List<Pair<String, String>>
)

private fun localizedIntegratedInsight(dashboard: DashboardDto, isChinese: Boolean): IntegratedInsightView {
    val latestAssessment = dashboard.assessmentResults.orEmpty().firstOrNull()
    val signEn = getZodiacSignName(dashboard.zodiacDaily?.zodiacSign, false)
    val signZh = getZodiacSignName(dashboard.zodiacDaily?.zodiacSign, true)
    val strongestEn = dashboard.bazi?.strongestElement()?.takeIf { it.isNotBlank() } ?: "balanced elements"
    val strongestZh = getElementChinese(strongestEn)

    if (isChinese) {
        return IntegratedInsightView(
            summary = buildChineseIntegratedSummary(dashboard, signZh, strongestZh, latestAssessment),
            points = listOf(
                "星象" to signZh,
                "五行" to "当前偏强：$strongestZh",
                "测评" to latestAssessmentText(latestAssessment, true)
            )
        )
    }

    val nickname = dashboard.profile?.nickname?.takeIf { it.isNotBlank() } ?: "User"
    return IntegratedInsightView(
        summary = buildEnglishIntegratedSummary(nickname, signEn, strongestEn, latestAssessment),
        points = listOf(
            "Zodiac" to signEn,
            "Bazi" to "Leading element: $strongestEn",
            "Tests" to latestAssessmentText(latestAssessment, false)
        )
    )
}

private fun buildChineseIntegratedSummary(
    dashboard: DashboardDto,
    sign: String,
    strongest: String,
    latestAssessment: AssessmentResultDto?
): String {
    val nickname = dashboard.profile?.nickname?.takeIf { it.isNotBlank() } ?: "用户"
    val assessmentPart = if (latestAssessment == null) {
        "暂无完成的测评，因此此部分会保持开放，方便后续补充。"
    } else {
        "最新测评显示 ${latestAssessmentText(latestAssessment, true)}。"
    }
    return "对于$nickname，今日自我反思结合了$sign 的日常星象提示，以及以$strongest 为当前偏强元素的八字五行结构。$assessmentPart 请把它作为温和的计划摘要，而不是诊断、预测或固定标签。"
}

private fun buildEnglishIntegratedSummary(
    nickname: String,
    sign: String,
    strongest: String,
    latestAssessment: AssessmentResultDto?
): String {
    val assessmentPart = if (latestAssessment == null) {
        "No assessment has been completed yet, so this section stays open for later reflection."
    } else {
        "The latest assessment shows ${latestAssessmentText(latestAssessment, false)}."
    }
    return "For $nickname, today's self-reflection combines $sign daily indicators with a calendar-based Bazi element balance led by $strongest. $assessmentPart Treat this as a gentle planning summary, not a diagnosis or prediction."
}

private fun latestAssessmentText(result: AssessmentResultDto?, isChinese: Boolean): String {
    if (result == null) return if (isChinese) "尚未完成" else "Not completed yet"
    val typeName = assessmentConfigs.find { it.type == result.type }?.let {
        if (isChinese) it.displayNameZh else it.displayNameEn
    } ?: (result.type ?: if (isChinese) "测评" else "Assessment")
    val resultLabel = displayAssessmentResultLabel(result.resultLabel, isChinese)
    return if (isChinese) "$typeName：$resultLabel" else "$typeName: $resultLabel"
}

@Composable
private fun LatestReportCard(
    reportText: String?,
    generatedBy: String?,
    onOpenReport: () -> Unit,
    isChinese: Boolean
) {
    // 不使用 CollapsibleCard，直接使用 InsightCard（不可折叠）
    InsightCard(
        title = if (isChinese) "报告生成" else "Report Generator",
        badge = if (isChinese) "报告" else (generatedBy ?: "Report")
    ) {
        if (reportText.isNullOrBlank()) {
            Text(
                text = if (isChinese) "尚未生成综合报告，点击下方按钮生成。" else "No integrated report has been generated yet. Click the button below to generate.",
                style = MaterialTheme.typography.bodyMedium,
                color = InsightMuted,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            PrimaryActionButton(
                text = if (isChinese) "生成报告" else "Generate Report",
                onClick = onOpenReport,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            PrimaryActionButton(
                text = if (isChinese) "打开报告" else "Open Report",
                onClick = onOpenReport,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ==================== 扩展函数 ====================

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

private fun BaziDto.strongestElement(): String {
    return elementScoreList().maxByOrNull { it.second }?.first ?: "Pending"
}

private fun BaziDto.lowestElement(): String {
    return elementScoreList().minByOrNull { it.second }?.first ?: "Pending"
}

private fun Double.toDisplayScore(): Int {
    return if (this <= 5.0) (this * 20).toInt().coerceIn(0, 100) else toInt().coerceIn(0, 100)
}

private fun String.toPlainSummary(): String {
    return lineSequence()
        .map { it.trim().trimStart('#').trim() }
        .firstOrNull { it.isNotBlank() }
        ?.take(220)
        ?: "Your latest report is ready."
}
