@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.insightself.ui.screens.zodiac

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.insightself.R
import com.example.insightself.data.local.LanguageManager
import com.example.insightself.data.model.ZodiacDailyDto
import com.example.insightself.data.model.ZodiacMatchResultDto
import com.example.insightself.data.model.ZodiacNatalDto
import com.example.insightself.util.SampleNameGenerator
import com.example.insightself.util.ZodiacSignUtils
import com.example.insightself.ui.components.AppTextField
import com.example.insightself.ui.components.AppTopBar
import com.example.insightself.ui.components.CollapsibleCard
import com.example.insightself.ui.components.ErrorView
import com.example.insightself.ui.components.InfoRow
import com.example.insightself.ui.components.InsightCard
import com.example.insightself.ui.components.LoadingView
import com.example.insightself.ui.components.PrimaryActionButton
import com.example.insightself.ui.components.ScoreBar
import com.example.insightself.ui.components.ScoreTone
import com.example.insightself.ui.components.ZodiacPlacement
import com.example.insightself.ui.components.ZodiacPlacementRow
import com.example.insightself.ui.components.ZodiacSignHeader
import com.example.insightself.ui.components.ZodiacSignIcon
import com.example.insightself.ui.theme.InsightMuted
import com.example.insightself.ui.theme.InsightPrimary
import com.example.insightself.ui.theme.InsightPrimary2
import com.example.insightself.ui.theme.InsightSpacing
import com.example.insightself.ui.theme.InsightText
import com.example.insightself.viewmodel.UiState
import com.example.insightself.viewmodel.ZodiacViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ZodiacScreen(
    zodiacViewModel: ZodiacViewModel,
    onOpenMatch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dailyState by zodiacViewModel.dailyState.collectAsState()
    val natalState by zodiacViewModel.natalState.collectAsState()
    val natalChart = remember(natalState) { zodiacViewModel.natalChartForDisplay() }
    val context = LocalContext.current

    var currentLanguage by remember { mutableStateOf("en") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var isRefreshing by remember { mutableStateOf(false) }

    val isChinese = runBlocking {
        LanguageManager.getLanguageFlow(context).first() == "zh"
    }

    // 初始加载今日数据
    LaunchedEffect(Unit) {
        zodiacViewModel.loadDaily()
        zodiacViewModel.loadNatal()
    }

    // 当日期改变时，加载对应日期的数据
    LaunchedEffect(selectedDate) {
        if (selectedDate <= LocalDate.now()) {
            zodiacViewModel.loadDailyForDate(selectedDate)
        }
    }

    // 监听语言变化
    LaunchedEffect(Unit) {
        LanguageManager.getLanguageFlow(context).collect { lang ->
            if (currentLanguage != lang) {
                currentLanguage = lang
                if (selectedDate <= LocalDate.now()) {
                    zodiacViewModel.loadDailyForDate(selectedDate)
                }
            }
        }
    }

    // 刷新时显示 Toast
    LaunchedEffect(dailyState) {
        if (isRefreshing && dailyState is UiState.Success) {
            isRefreshing = false
            Toast.makeText(
                context,
                if (isChinese) "已更新当日运势" else "Updated daily horoscope",
                Toast.LENGTH_SHORT
            ).show()
        } else if (isRefreshing && dailyState is UiState.Error) {
            isRefreshing = false
            Toast.makeText(
                context,
                if (isChinese) "网络异常，显示缓存内容" else "Network error, showing cached content",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    when (val state = dailyState) {
        UiState.Idle,
        UiState.Loading -> LoadingView(modifier = modifier.fillMaxSize(), message = if (isChinese) "加载星座中..." else "Loading Zodiac")
        is UiState.Error -> ErrorView(
            message = state.message,
            modifier = modifier.fillMaxSize(),
            onRetry = { zodiacViewModel.loadDailyForDate(selectedDate) }
        )
        is UiState.Success -> ZodiacContent(
            daily = state.data,
            natalChart = natalChart,
            selectedDate = selectedDate,
            onDateChange = { newDate ->
                if (newDate <= LocalDate.now()) {
                    selectedDate = newDate
                } else {
                    Toast.makeText(
                        context,
                        if (isChinese) "只能查看历史日期" else "Only historical dates can be viewed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            onRefresh = {
                isRefreshing = true
                val today = LocalDate.now()
                selectedDate = today
                zodiacViewModel.loadDailyForDate(today, forceRefresh = true)
                zodiacViewModel.loadNatal(forceRefresh = true)
            },
            onOpenMatch = onOpenMatch,
            modifier = modifier,
            isChinese = isChinese
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ZodiacContent(
    daily: ZodiacDailyDto,
    natalChart: ZodiacNatalDto?,
    selectedDate: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    onRefresh: () -> Unit,
    onOpenMatch: () -> Unit,
    modifier: Modifier = Modifier,
    isChinese: Boolean
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val dateFormatter = DateTimeFormatter.ofPattern(if (isChinese) "yyyy年MM月dd日" else "MMM dd, yyyy")
    var showDatePicker by remember { mutableStateOf(false) }

    // 调试日志
    LaunchedEffect(daily, selectedDate) {
        println("=== 数据更新 ===")
        println("当前日期: ${selectedDate.format(dateFormatter)}")
        println("情绪分数: ${daily.emotionScore}")
        println("沟通分数: ${daily.communicationScore}")
        println("行动力分数: ${daily.actionScore}")
        println("建议: ${daily.suggestion}")
        println("星座: ${daily.zodiacSign}")
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        yearRange = 1900..LocalDate.now().year
    )

    // 星座中英文映射
    fun getZodiacSign(sign: String?): String {
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

    val sunSignKey = natalChart?.sunSign ?: daily.zodiacSign
    val moonSignKey = natalChart?.planets?.get("Moon")?.sign
    val risingSignKey = natalChart?.ascendantSign
    val sunSign = ZodiacSignUtils.displaySign(sunSignKey, isChinese)
    val moonSign = ZodiacSignUtils.displaySign(moonSignKey, isChinese)
    val risingSign = ZodiacSignUtils.displaySign(risingSignKey, isChinese)
    val isToday = selectedDate == LocalDate.now()
    val dateLabel = if (isChinese) {
        if (isToday) "今日" else selectedDate.format(dateFormatter)
    } else {
        if (isToday) "Today" else selectedDate.format(dateFormatter)
    }

    val shareText = if (isChinese) {
        "${dateLabel}${sunSign}运势：情绪${daily.emotionScore ?: 0}分，沟通${daily.communicationScore ?: 0}分，行动力${daily.actionScore ?: 0}分。${daily.suggestion?.take(50) ?: ""}"
    } else {
        "$dateLabel $sunSign horoscope: Emotion ${daily.emotionScore ?: 0}, Communication ${daily.communicationScore ?: 0}, Action ${daily.actionScore ?: 0}. ${daily.suggestion?.take(50) ?: ""}"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = InsightSpacing.ScreenHorizontal),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // 顶部栏
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.zodiac_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = InsightText
            )
            Row {
                IconButton(onClick = onRefresh) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_refresh),
                        contentDescription = if (isChinese) "刷新" else "Refresh",
                        tint = InsightPrimary
                    )
                }
                IconButton(onClick = {
                    clipboardManager.setText(AnnotatedString(shareText))
                    Toast.makeText(
                        context,
                        if (isChinese) "已复制到剪贴板" else "Copied to clipboard",
                        Toast.LENGTH_SHORT
                    ).show()
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_share),
                        contentDescription = if (isChinese) "分享" else "Share",
                        tint = InsightPrimary
                    )
                }
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_calendar),
                        contentDescription = if (isChinese) "切换日期" else "Change date",
                        tint = InsightPrimary
                    )
                }
            }
        }

        // 顶部个性化卡片
        HeroZodiacCard(
            sunSign = sunSign,
            moonSign = moonSign,
            risingSign = risingSign,
            sunSignKey = sunSignKey,
            moonSignKey = moonSignKey,
            risingSignKey = risingSignKey,
            date = selectedDate,
            dateFormatter = dateFormatter,
            isChinese = isChinese
        )

        // 动态看板
        DynamicBoardCard(
            daily = daily,
            dateLabel = dateLabel,
            dailySignKey = daily.zodiacSign,
            isChinese = isChinese
        )

        // 核心解读
        CoreInterpretationCard(isChinese = isChinese)

        // 关系匹配入口
        InsightCard(
            title = stringResource(R.string.relationship_matching),
            badge = if (isChinese) "可选" else "Optional"
        ) {
            Text(
                text = if (isChinese)
                    "比较目标出生日期和可选个性标签，进行柔和沟通反思。"
                else
                    "Compare a target birth date and optional personality tag for a soft communication reflection.",
                style = MaterialTheme.typography.bodyMedium,
                color = InsightMuted
            )
            PrimaryActionButton(
                text = stringResource(R.string.open_match),
                onClick = onOpenMatch,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(96.dp))
    }

    // 日期选择器对话框
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val newDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()

                            if (newDate <= LocalDate.now()) {
                                onDateChange(newDate)
                                showDatePicker = false
                            } else {
                                Toast.makeText(
                                    context,
                                    if (isChinese) "不能选择未来日期" else "Cannot select future date",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                ) {
                    Text(if (isChinese) "确认" else "Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(if (isChinese) "取消" else "Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false
            )
        }
    }
}

@Composable
private fun HeroZodiacCard(
    sunSign: String,
    moonSign: String,
    risingSign: String,
    sunSignKey: String?,
    moonSignKey: String?,
    risingSignKey: String?,
    date: LocalDate,
    dateFormatter: DateTimeFormatter,
    isChinese: Boolean
) {
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
                text = if (isChinese) "本命三要素" else "Natal Placements",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = InsightPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            ZodiacPlacementRow(
                placements = listOf(
                    ZodiacPlacement(
                        roleLabel = if (isChinese) "太阳" else "Sun",
                        signLabel = sunSign,
                        signKey = sunSignKey
                    ),
                    ZodiacPlacement(
                        roleLabel = if (isChinese) "月亮" else "Moon",
                        signLabel = moonSign,
                        signKey = moonSignKey
                    ),
                    ZodiacPlacement(
                        roleLabel = if (isChinese) "上升" else "Rising",
                        signLabel = risingSign,
                        signKey = risingSignKey
                    )
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (isChinese)
                    "${date.format(dateFormatter)} · 综合能量解读"
                else
                    "${date.format(dateFormatter)} · Integrated Energy Reading",
                style = MaterialTheme.typography.bodyMedium,
                color = InsightMuted
            )
        }
    }
}

@Composable
private fun DynamicBoardCard(
    daily: ZodiacDailyDto,
    dateLabel: String,
    dailySignKey: String?,
    isChinese: Boolean
) {
    val dailySignLabel = ZodiacSignUtils.displaySign(dailySignKey, isChinese)
    CollapsibleCard(
        title = if (isChinese) "动态看板" else "Dynamic Board",
        badge = if (isChinese) "基于当日相位" else "Based on daily aspects",
        isExpandedByDefault = true
    ) {
        ZodiacSignHeader(
            signKey = dailySignKey,
            signLabel = if (isChinese) "当日太阳星座：$dailySignLabel" else "Today's Sun sign: $dailySignLabel",
            subtitle = if (isChinese) "正在查看：$dateLabel 的运势数据" else "Viewing: $dateLabel horoscope data",
            modifier = Modifier.padding(bottom = 12.dp)
        )

        ScoreBar(
            label = if (isChinese) "情绪" else "Emotion",
            score = daily.emotionScore ?: 0,
            tone = ScoreTone.Success
        )
        ScoreBar(
            label = if (isChinese) "沟通" else "Communication",
            score = daily.communicationScore ?: 0,
            tone = ScoreTone.Primary
        )
        ScoreBar(
            label = if (isChinese) "行动力" else "Action",
            score = daily.actionScore ?: 0,
            tone = ScoreTone.Warning
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (isChinese) "今日建议" else "Today's Suggestion",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = InsightText
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 修改这里：根据语言显示中文或英文的建议
        val suggestionText = if (isChinese) {
            // 中文建议
            when {
                daily.suggestion?.contains("small, observable choices") == true ->
                    "今天适合做一些小而确定的行动。将日常运势视为温和的计划提示，而非预测。"
                daily.suggestion?.contains("balance") == true ->
                    "保持平衡，循序渐进。今天适合在行动前多思考片刻。"
                daily.suggestion?.contains("action") == true ->
                    "行动力旺盛的一天，适合推进重要项目。"
                daily.suggestion?.contains("communication") == true ->
                    "沟通顺畅，适合表达想法和感受。"
                !daily.suggestion.isNullOrBlank() -> daily.suggestion
                else -> "选择一个小的行动，对自己保持温和的节奏。"
            }
        } else {
            // 英文建议
            daily.suggestion?.takeIf { it.isNotBlank() }
                ?: "Choose one small action and keep the pace kind to yourself."
        }

        Text(
            text = suggestionText,
            style = MaterialTheme.typography.bodyMedium,
            color = InsightMuted
        )
    }
}

@Composable
private fun CoreInterpretationCard(isChinese: Boolean) {
    CollapsibleCard(
        title = if (isChinese) "核心解读" else "Core Interpretation",
        badge = if (isChinese) "二级匹配模型" else "Secondary matching model",
        isExpandedByDefault = true
    ) {
        CollapsibleCard(
            title = if (isChinese) "💬 沟通策略" else "💬 Communication Strategy",
            isExpandedByDefault = false
        ) {
            Text(
                text = if (isChinese)
                    "重要对话先书面列出3点事实，再见面补充情绪；避开公众场合即时对质。\n\n" +
                            "建议使用「我感受...」句式而非「你应该...」，减少防御心理。"
                else
                    "List 3 facts in writing before important conversations, then supplement emotions in person; avoid immediate confrontation in public.\n\n" +
                            "Use 'I feel...' instead of 'You should...' to reduce defensiveness.",
                style = MaterialTheme.typography.bodySmall,
                color = InsightMuted,
                lineHeight = 20.sp
            )
        }

        CollapsibleCard(
            title = if (isChinese) "⚠️ 风险提示" else "⚠️ Risk Notes",
            isExpandedByDefault = false
        ) {
            Text(
                text = if (isChinese)
                    "水星逆行前后易误读语气信息；大额承诺建议延迟48小时再签。\n\n" +
                            "避免在情绪波动时做重要决定。"
                else
                    "During Mercury retrograde, communication may be easily misinterpreted; postpone major commitments for 48 hours.\n\n" +
                            "Avoid making important decisions during emotional fluctuations.",
                style = MaterialTheme.typography.bodySmall,
                color = InsightMuted,
                lineHeight = 20.sp
            )
        }

        CollapsibleCard(
            title = if (isChinese) "🤝 合作方式" else "🤝 Collaboration Mode",
            isExpandedByDefault = false
        ) {
            Text(
                text = if (isChinese)
                    "你适合担任「愿景 + 创意」担当；搭档适合出任流程管理与风险控制角色。\n\n" +
                            "建议每周固定时间同步进度，保持信息透明。"
                else
                    "You are suited for the 'vision + creativity' role; partners are suitable for process management and risk control.\n\n" +
                            "Schedule regular weekly syncs to maintain transparency.",
                style = MaterialTheme.typography.bodySmall,
                color = InsightMuted,
                lineHeight = 20.sp
            )
        }
    }
}

// ==================== MatchScreen ====================

@Composable
fun MatchScreen(
    zodiacViewModel: ZodiacViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val matchState by zodiacViewModel.matchState.collectAsState()
    val loading = matchState is UiState.Loading
    var nickname by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var personalityTag by remember { mutableStateOf("") }
    val context = LocalContext.current
    var currentLanguage by remember { mutableStateOf("en") }

    LaunchedEffect(Unit) {
        LanguageManager.getLanguageFlow(context).collect { lang ->
            currentLanguage = lang
        }
    }

    val isChinese = currentLanguage == "zh"
    val nicknamePlaceholder = remember(isChinese) { SampleNameGenerator.nicknamePlaceholder(isChinese) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = InsightSpacing.ScreenHorizontal),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        AppTopBar(
            title = stringResource(R.string.match_title),
            subtitle = stringResource(R.string.match_subtitle),
            onBack = onBack
        )
        InsightCard(title = stringResource(R.string.target_profile), badge = stringResource(R.string.zodiac)) {
            AppTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = stringResource(R.string.nickname),
                placeholder = nicknamePlaceholder
            )
            AppTextField(
                value = birthDate,
                onValueChange = { birthDate = it },
                label = stringResource(R.string.birth_date),
                placeholder = stringResource(R.string.birth_date_placeholder)
            )
            AppTextField(
                value = personalityTag,
                onValueChange = { personalityTag = it.uppercase() },
                label = stringResource(R.string.personality_tag),
                placeholder = if (isChinese) "选填，例如 INFJ" else "Optional, e.g. INFJ"  // 修改这里
            )
            PrimaryActionButton(
                text = stringResource(R.string.calculate_match),
                onClick = {
                    zodiacViewModel.submitMatch(
                        targetNickname = nickname,
                        targetBirthDate = birthDate,
                        targetPersonalityTag = personalityTag
                    )
                },
                loading = loading,
                enabled = !loading
            )
        }
        when (val state = matchState) {
            UiState.Idle -> InsightCard(title = stringResource(R.string.match_result), badge = stringResource(R.string.waiting)) {
                Text(
                    text = if (isChinese) "输入昵称和出生日期，查看以沟通为重点的匹配结果。" else "Enter a nickname and birth date to see a communication-focused match result.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InsightMuted
                )
            }
            UiState.Loading -> LoadingView(modifier = Modifier.height(180.dp), message = stringResource(R.string.calculating))
            is UiState.Error -> InsightCard(title = stringResource(R.string.something_wrong), badge = stringResource(R.string.retry)) {
                Text(text = state.message, style = MaterialTheme.typography.bodyMedium, color = InsightMuted)
            }
            is UiState.Success -> MatchResultCard(result = state.data, isChinese = isChinese)
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun MatchResultCard(result: ZodiacMatchResultDto, isChinese: Boolean) {
    InsightCard(title = stringResource(R.string.match_result), badge = result.level?.takeIf { it.isNotBlank() } ?: stringResource(R.string.reflection)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .background(
                        Brush.linearGradient(listOf(InsightPrimary, InsightPrimary2)),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (result.finalScore ?: 0).coerceIn(0, 100).toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                Text(
                    text = result.targetNickname.orPlaceholder(if (isChinese) "对方" else "Target"),
                    style = MaterialTheme.typography.titleLarge,
                    color = InsightText
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ZodiacSignIcon(
                        sign = result.targetZodiacSign,
                        size = 32.dp,
                        contentDescription = result.targetZodiacSign
                    )
                    Text(
                        text = if (isChinese) {
                            "星座：${ZodiacSignUtils.displaySign(result.targetZodiacSign, true)}"
                        } else {
                            "Sign: ${ZodiacSignUtils.displaySign(result.targetZodiacSign, false)}"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = InsightMuted
                    )
                }
                Text(
                    text = result.collaborationMode.orPlaceholder(if (isChinese) "平衡沟通" else "Balanced communication"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = InsightMuted
                )
            }
        }
        InfoRow(label = if (isChinese) "星座分数" else "Zodiac score", value = (result.zodiacScore ?: 0).toString())
        InfoRow(label = if (isChinese) "个性分数" else "Personality score", value = (result.personalityScore ?: 0).toString())
        MatchReflectionText(title = stringResource(R.string.communication_tips), text = result.communicationTips, isChinese = isChinese)
        MatchReflectionText(title = stringResource(R.string.watch_outs), text = result.riskNotes, isChinese = isChinese)
    }
}

@Composable
private fun MatchReflectionText(title: String, text: String?, isChinese: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = InsightText)
        Text(
            text = text.orPlaceholder(if (isChinese) "保持好奇和具体的沟通方式。" else "Keep the conversation curious and specific."),
            style = MaterialTheme.typography.bodyMedium,
            color = InsightMuted
        )
    }
}

private fun String?.orPlaceholder(value: String): String {
    return if (isNullOrBlank()) value else this
}
