package com.example.insightself.ui.screens.assessment

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.insightself.R
import com.example.insightself.data.local.LanguageManager
import com.example.insightself.data.model.AssessmentQuestionDto
import com.example.insightself.data.model.AssessmentResultDto
import com.example.insightself.ui.components.AppTopBar
import com.example.insightself.ui.components.ErrorView
import com.example.insightself.ui.components.InsightCard
import com.example.insightself.ui.components.LoadingView
import com.example.insightself.ui.components.PrimaryActionButton
import com.example.insightself.ui.components.SecondaryActionButton
import com.example.insightself.ui.theme.InsightCardStrong
import com.example.insightself.ui.theme.InsightMuted
import com.example.insightself.ui.theme.InsightPrimary
import com.example.insightself.ui.theme.InsightPrimary2
import com.example.insightself.ui.theme.InsightSpacing
import com.example.insightself.ui.theme.InsightText
import com.example.insightself.viewmodel.AssessmentHubData
import com.example.insightself.viewmodel.AssessmentViewModel
import com.example.insightself.viewmodel.UiState
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.RadioButtonDefaults
import com.example.insightself.ui.components.InfoRow
import com.example.insightself.ui.components.ScoreBar
import com.example.insightself.ui.components.ScoreTone

// ==================== 测评类型配置 ====================

data class AssessmentTypeConfig(
    val type: String,
    val displayNameEn: String,
    val displayNameZh: String,
    val descriptionEn: String,
    val descriptionZh: String,
    val estimatedMinutes: Int,
    val icon: String
)

val assessmentConfigs = listOf(
    AssessmentTypeConfig(
        type = "BFI10",
        displayNameEn = "IPIP Big Five-20",
        displayNameZh = "IPIP 大五人格 20 题",
        descriptionEn = "A 20-item Big Five reflection based on public-domain IPIP-style item families.",
        descriptionZh = "基于 IPIP 公共领域题项家族的 20 题大五人格反思工具。",
        estimatedMinutes = 4,
        icon = "📊"
    ),
    AssessmentTypeConfig(
        type = "MBTI",
        displayNameEn = "MBTI-style Preferences",
        displayNameZh = "MBTI 风格偏好",
        descriptionEn = "A familiar preference-style reflection module; not the official proprietary MBTI instrument.",
        descriptionZh = "常见偏好风格反思模块；不是官方专有 MBTI 量表。",
        estimatedMinutes = 5,
        icon = "🧠"
    ),
    AssessmentTypeConfig(
        type = "ATTACHMENT",
        displayNameEn = "ECR-RS-style Attachment",
        displayNameZh = "ECR-RS 风格依恋问卷",
        descriptionEn = "A 9-item dimensional reflection on connection patterns and support needs.",
        descriptionZh = "关于关系连接模式和支持需求的 9 题维度反思。",
        estimatedMinutes = 5,
        icon = "💕"
    ),
    AssessmentTypeConfig(
        type = "CAREER",
        displayNameEn = "O*NET Mini Interest Profiler",
        displayNameZh = "O*NET 迷你职业兴趣测评",
        descriptionEn = "The official 30-item O*NET Mini Interest Profiler for RIASEC career interests.",
        descriptionZh = "官方 O*NET 30 题迷你职业兴趣测评，输出 RIASEC 六维兴趣。",
        estimatedMinutes = 8,
        icon = "🎯"
    ),
    AssessmentTypeConfig(
        type = "WHO5",
        displayNameEn = "WHO-5 Well-being",
        displayNameZh = "WHO-5 幸福感指数",
        descriptionEn = "A 5-item well-being index using the original 0-5 response scale.",
        descriptionZh = "5 题幸福感指数，使用原始 0-5 频率量尺。",
        estimatedMinutes = 2,
        icon = "🌤️"
    ),
    AssessmentTypeConfig(
        type = "RSES",
        displayNameEn = "Rosenberg Self-Esteem",
        displayNameZh = "Rosenberg 自尊量表",
        descriptionEn = "A 10-item self-esteem scale using the original 4-point agreement format.",
        descriptionZh = "10 题自尊量表，使用原始 4 点同意量尺。",
        estimatedMinutes = 3,
        icon = "🪞"
    )
)

fun getDisplayName(config: AssessmentTypeConfig, isChinese: Boolean): String {
    return if (isChinese) config.displayNameZh else config.displayNameEn
}

fun getDescription(config: AssessmentTypeConfig, isChinese: Boolean): String {
    return if (isChinese) config.descriptionZh else config.descriptionEn
}

// ==================== 主页面 ====================

@Composable
fun AssessmentHubScreen(
    assessmentViewModel: AssessmentViewModel,
    onOpenQuestions: (String) -> Unit,
    onOpenResult: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val hubState by assessmentViewModel.hubState.collectAsState()
    val context = LocalContext.current

    var currentLanguage by remember { mutableStateOf("en") }

    LaunchedEffect(Unit) {
        LanguageManager.getLanguageFlow(context).collect { lang ->
            if (currentLanguage != lang) {
                currentLanguage = lang
                assessmentViewModel.loadHub()
            }
        }
    }

    LaunchedEffect(Unit) {
        assessmentViewModel.loadHub()
    }

    when (val state = hubState) {
        UiState.Idle,
        UiState.Loading -> LoadingView(modifier = modifier.fillMaxSize(), message = if (currentLanguage == "zh") "加载测评列表中..." else "Loading assessments")
        is UiState.Error -> ErrorView(
            message = state.message,
            modifier = modifier.fillMaxSize(),
            onRetry = assessmentViewModel::loadHub
        )
        is UiState.Success -> AssessmentHubContent(
            data = state.data,
            assessmentViewModel = assessmentViewModel,
            onOpenQuestions = onOpenQuestions,
            onOpenResult = { result ->
                assessmentViewModel.selectResult(result)
                result.id?.let(onOpenResult)
            },
            onRefresh = assessmentViewModel::loadHub,
            modifier = modifier,
            isChinese = currentLanguage == "zh"
        )
    }
}

@Composable
private fun AssessmentHubContent(
    data: AssessmentHubData,
    assessmentViewModel: AssessmentViewModel,
    onOpenQuestions: (String) -> Unit,
    onOpenResult: (AssessmentResultDto) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    isChinese: Boolean
) {
    val context = LocalContext.current

    // 获取每个测评的保存进度状态
    val savedProgressMap by assessmentViewModel.savedProgressMap.collectAsState()

    fun getLastCompletedTime(type: String): String? {
        val result = data.results.firstOrNull { it.type == type }
        return result?.createdAt?.takeIf { it.isNotBlank() }
    }

    fun getStatus(type: String): Pair<String, Color> {
        val hasResult = data.results.any { it.type == type }
        return if (hasResult) {
            Pair(if (isChinese) "已完成" else "Completed", Color(0xFF4CAF50))
        } else {
            Pair(if (isChinese) "未开始" else "Not started", Color(0xFF9E9E9E))
        }
    }

    // 检查是否有保存的进度
    fun hasSavedProgress(type: String): Boolean {
        return savedProgressMap[type]?.isNotEmpty() == true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = InsightSpacing.ScreenHorizontal),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isChinese) "心理测评" else "Assessments",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = InsightText
            )
            OutlinedButton(onClick = onRefresh) {
                Text(if (isChinese) "刷新" else "Refresh")
            }
        }

        Text(
            text = if (isChinese) "轻量反思工具，帮助您更好地了解自己" else "Light reflection tools to help you understand yourself better",
            style = MaterialTheme.typography.bodyMedium,
            color = InsightMuted
        )

        Spacer(modifier = Modifier.height(8.dp))

        assessmentConfigs.forEach { config ->
            val lastTime = getLastCompletedTime(config.type)
            val (status, statusColor) = getStatus(config.type)
            val displayName = getDisplayName(config, isChinese)
            val description = getDescription(config, isChinese)
            val hasProgress = hasSavedProgress(config.type)

            AssessmentTypeCard(
                config = config,
                displayName = displayName,
                description = description,
                status = status,
                statusColor = statusColor,
                lastCompletedTime = lastTime,
                hasSavedProgress = hasProgress,
                onStart = { onOpenQuestions(config.type) },
                onContinue = {
                    onOpenQuestions(config.type)
                },
                onViewResult = {
                    data.results.firstOrNull { it.type == config.type }?.let { onOpenResult(it) }
                },
                isChinese = isChinese
            )
        }

        if (data.results.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isChinese) "历史记录" else "History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = InsightText
            )

            data.results.take(5).forEach { result ->
                ResultHistoryItem(
                    result = result,
                    onClick = { onOpenResult(result) },
                    isChinese = isChinese
                )
            }
        }

        Spacer(modifier = Modifier.height(96.dp))
    }
}

@Composable
private fun AssessmentTypeCard(
    config: AssessmentTypeConfig,
    displayName: String,
    description: String,
    status: String,
    statusColor: Color,
    lastCompletedTime: String?,
    hasSavedProgress: Boolean,
    onStart: () -> Unit,
    onContinue: () -> Unit,
    onViewResult: () -> Unit,
    isChinese: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = InsightCardStrong),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            val displayStatus = when {
                hasSavedProgress -> if (isChinese) "未完成" else "In Progress"
                else -> status
            }
            val displayStatusColor = if (hasSavedProgress) Color(0xFFFF9800) else statusColor

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(InsightPrimary.copy(alpha = 0.10f), RoundedCornerShape(17.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = config.icon,
                        fontSize = 26.sp
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = InsightText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isChinese) "约${config.estimatedMinutes}分钟" else "About ${config.estimatedMinutes} min",
                        style = MaterialTheme.typography.labelMedium,
                        color = InsightMuted
                    )
                }
                Text(
                    text = displayStatus,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = displayStatusColor,
                    modifier = Modifier
                        .background(displayStatusColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = InsightMuted,
                lineHeight = 22.sp
            )

            if (lastCompletedTime != null) {
                Text(
                    text = if (isChinese) "上次完成：${formatDate(lastCompletedTime, isChinese)}" else "Last completed: ${formatDate(lastCompletedTime, isChinese)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = InsightMuted
                )
            }

            // 按钮行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 如果有保存的进度，显示"继续作答"按钮
                if (hasSavedProgress) {
                    Button(
                        onClick = onContinue,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF9800)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isChinese) "继续作答" else "Continue",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                } else {
                    // 没有保存进度时，显示"开始测评"或"重新测评"
                    Button(
                        onClick = onStart,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = InsightPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (status == if (isChinese) "已完成" else "Completed")
                                (if (isChinese) "重新测评" else "Retake")
                            else
                                (if (isChinese) "开始测评" else "Start"),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // 查看报告按钮（仅当已完成时显示）
                if (status == if (isChinese) "已完成" else "Completed") {
                    OutlinedButton(
                        onClick = onViewResult,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isChinese) "查看报告" else "View Report",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = InsightPrimary
                        )
                    }
                } else if (!hasSavedProgress) {
                    // 占位，保持布局对齐
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ResultHistoryItem(
    result: AssessmentResultDto,
    onClick: () -> Unit,
    isChinese: Boolean
) {
    val config = assessmentConfigs.find { it.type == result.type }
    val typeName = if (isChinese) config?.displayNameZh ?: result.type ?: "测评" else config?.displayNameEn ?: result.type ?: "Assessment"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = InsightCardStrong)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = config?.icon ?: "📋",
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = typeName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = InsightText
                )
                Text(
                    text = result.resultLabel ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = InsightMuted
                )
            }
            Text(
                text = formatDate(result.createdAt, isChinese),
                style = MaterialTheme.typography.bodySmall,
                color = InsightMuted
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_forward),
                contentDescription = null,
                tint = InsightMuted
            )
        }
    }
}

// ==================== 答题页面 ====================

@Composable
fun AssessmentQuestionScreen(
    type: String,
    assessmentViewModel: AssessmentViewModel,
    onBack: () -> Unit,
    onSubmitted: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val questionsState by assessmentViewModel.questionsState.collectAsState()
    val answers by assessmentViewModel.answers.collectAsState()
    val submitState by assessmentViewModel.submitState.collectAsState()
    val context = LocalContext.current
    val config = assessmentConfigs.find { it.type == type }

    var currentQuestionIndex by remember { mutableStateOf(0) }
    var showExitDialog by remember { mutableStateOf(false) }
    var currentLanguage by remember { mutableStateOf("en") }
    var hasLoadedProgress by remember { mutableStateOf(false) }

    val questions = (questionsState as? UiState.Success)?.data ?: emptyList()
    val currentQuestion = questions.getOrNull(currentQuestionIndex)
    val answeredCount = answers.size

    val isChinese = currentLanguage == "zh"
    val displayName = if (isChinese) config?.displayNameZh ?: type else config?.displayNameEn ?: type

    // 获取当前语言
    LaunchedEffect(Unit) {
        LanguageManager.getLanguageFlow(context).collect { lang ->
            currentLanguage = lang
        }
    }

    // 加载题目
    LaunchedEffect(type) {
        assessmentViewModel.loadQuestions(type)
    }

    // 题目加载完成后，加载保存的进度并跳转到最后位置
    LaunchedEffect(questionsState) {
        if (!hasLoadedProgress && questionsState is UiState.Success && questions.isNotEmpty()) {
            val savedAnswers = assessmentViewModel.loadSavedProgress(type)
            if (savedAnswers != null && savedAnswers.isNotEmpty()) {
                // 恢复保存的答案
                savedAnswers.forEach { (qId, score) ->
                    if (assessmentViewModel.answers.value[qId] == null) {
                        assessmentViewModel.selectAnswer(qId, score)
                    }
                }
                // 跳转到最后作答的题目
                val lastAnsweredId = savedAnswers.keys.lastOrNull()
                val lastIndex = questions.indexOfFirst { it.id == lastAnsweredId }
                if (lastIndex >= 0) {
                    currentQuestionIndex = lastIndex
                }
            }
            hasLoadedProgress = true
        }
    }

    // 提交成功处理
    LaunchedEffect(submitState) {
        val result = (submitState as? UiState.Success)?.data
        val id = result?.id
        if (id != null) {
            assessmentViewModel.clearSavedProgress(type)
            assessmentViewModel.selectResult(result)
            assessmentViewModel.resetSubmitState()
            onSubmitted(id)
        }
    }

    // 退出确认对话框
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text(if (isChinese) "退出作答？" else "Exit Assessment?") },
            text = {
                Text(
                    if (isChinese)
                        "您已完成 ${answeredCount}/${questions.size} 题。是否保存进度以便下次继续？"
                    else
                        "You have completed ${answeredCount}/${questions.size} questions. Save progress to continue later?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        // 点击"放弃" - 清除保存的进度
                        assessmentViewModel.clearSavedProgress(type)
                        Toast.makeText(
                            context,
                            if (isChinese) "进度已放弃，下次将重新开始" else "Progress abandoned, will start over next time",
                            Toast.LENGTH_SHORT
                        ).show()
                        onBack()
                    }
                ) {
                    Text(if (isChinese) "放弃" else "Abandon")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        // 点击"保存进度" - 保存当前进度
                        assessmentViewModel.saveProgress(type, answers)
                        Toast.makeText(
                            context,
                            if (isChinese) "进度已保存" else "Progress saved",
                            Toast.LENGTH_SHORT
                        ).show()
                        onBack()
                    }
                ) {
                    Text(if (isChinese) "保存进度" else "Save Progress")
                }
            }
        )
    }

    when (val state = questionsState) {
        UiState.Idle,
        UiState.Loading -> LoadingView(modifier = modifier.fillMaxSize(), message = if (isChinese) "加载题目中..." else "Loading questions")
        is UiState.Error -> ErrorView(
            message = state.message,
            modifier = modifier.fillMaxSize(),
            onRetry = { assessmentViewModel.loadQuestions(type) }
        )
        is UiState.Success -> {
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
                    IconButton(onClick = { showExitDialog = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = if (isChinese) "返回" else "Back",
                            tint = InsightPrimary
                        )
                    }
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = InsightText
                    )
                    Box(modifier = Modifier.size(48.dp))
                }

                // 进度条
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isChinese) "第 ${currentQuestionIndex + 1} / ${questions.size} 题" else "Question ${currentQuestionIndex + 1}/${questions.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = InsightMuted
                        )
                        Text(
                            text = if (isChinese) "已答 ${answeredCount}/${questions.size}" else "Answered ${answeredCount}/${questions.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = InsightPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = answeredCount.toFloat() / questions.size,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = InsightPrimary,
                        trackColor = InsightMuted.copy(alpha = 0.2f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 当前题目
                if (currentQuestion != null) {
                        QuestionCard(
                            type = type,
                            question = currentQuestion,
                            selectedScore = answers[currentQuestion.id],
                            onSelect = { score ->
                            assessmentViewModel.selectAnswer(currentQuestion.id, score)
                        },
                        isChinese = isChinese
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 导航按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (currentQuestionIndex > 0) {
                        OutlinedButton(
                            onClick = { currentQuestionIndex-- },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (isChinese) "上一题" else "Previous",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = InsightPrimary
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    // 下一题或提交按钮
                    val isCurrentQuestionAnswered = currentQuestion != null && answers.containsKey(currentQuestion.id)

                    if (currentQuestionIndex + 1 < questions.size) {
                        Button(
                            onClick = {
                                if (isCurrentQuestionAnswered) {
                                    currentQuestionIndex++
                                } else {
                                    Toast.makeText(
                                        context,
                                        if (isChinese) "请先选择答案" else "Please select an answer first",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            enabled = isCurrentQuestionAnswered,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = InsightPrimary,
                                disabledContainerColor = InsightMuted.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (isChinese) "下一题" else "Next",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    } else {
                        val allAnswered = answeredCount == questions.size
                        Button(
                            onClick = { assessmentViewModel.submit(type) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            enabled = allAnswered && submitState !is UiState.Loading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = InsightPrimary,
                                disabledContainerColor = InsightMuted.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (submitState is UiState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = if (isChinese) "提交测评" else "Submit",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun QuestionCard(
    type: String,
    question: AssessmentQuestionDto,
    selectedScore: Int?,
    onSelect: (Int) -> Unit,
    isChinese: Boolean
) {
    val options = responseOptions(type, isChinese)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = InsightCardStrong)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = question.questionText,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = InsightText,
                lineHeight = 28.sp
            )

            Text(
                text = if (isChinese) "维度：${getDimensionChinese(question.dimension, isChinese)}" else "Dimension: ${question.dimension}",
                style = MaterialTheme.typography.bodySmall,
                color = InsightMuted
            )

            Spacer(modifier = Modifier.height(8.dp))

            options.forEach { (score, label) ->
                val isSelected = selectedScore == score
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(score) }
                        .background(
                            if (isSelected) InsightPrimary.copy(alpha = 0.1f) else Color.Transparent,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onSelect(score) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = InsightPrimary
                        )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isSelected) InsightPrimary else InsightText,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

// ==================== 结果页面 ====================

@Composable
fun AssessmentResultScreen(
    resultId: Long,
    assessmentViewModel: AssessmentViewModel,
    onBackToAssessments: () -> Unit,
    onBackHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentLanguage by remember { mutableStateOf("en") }

    LaunchedEffect(Unit) {
        LanguageManager.getLanguageFlow(context).collect { lang ->
            currentLanguage = lang
        }
    }

    val isChinese = currentLanguage == "zh"

    val selected by assessmentViewModel.selectedResult.collectAsState()
    val hubState by assessmentViewModel.hubState.collectAsState()
    val submitState by assessmentViewModel.submitState.collectAsState()
    val result = selected?.takeIf { it.id == resultId }
        ?: (submitState as? UiState.Success)?.data?.takeIf { it.id == resultId }
        ?: (hubState as? UiState.Success)?.data?.results?.firstOrNull { it.id == resultId }
        ?: assessmentViewModel.findResult(resultId)

    if (result == null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = InsightSpacing.ScreenHorizontal),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isChinese) "请返回测评列表查看报告" else "Please return to assessment list to view report",
                style = MaterialTheme.typography.titleMedium,
                color = InsightMuted
            )
            Spacer(modifier = Modifier.height(16.dp))
            PrimaryActionButton(
                text = if (isChinese) "返回测评列表" else "Back to Assessments",
                onClick = onBackToAssessments
            )
        }
        return
    }

    // 获取测评类型配置
    val config = assessmentConfigs.find { it.type == result.type }
    val displayType = if (isChinese) {
        config?.displayNameZh ?: when (result.type?.uppercase()) {
            "BFI10" -> "IPIP 大五人格 20 题"
            "MBTI" -> "MBTI 风格偏好"
            "ATTACHMENT" -> "依恋风格"
            "CAREER" -> "O*NET 职业兴趣"
            "WHO5" -> "WHO-5 幸福感"
            "RSES" -> "Rosenberg 自尊"
            else -> result.type ?: "测评"
        }
    } else {
        config?.displayNameEn ?: when (result.type?.uppercase()) {
            "BFI10" -> "IPIP Big Five-20"
            "MBTI" -> "MBTI-style Preferences"
            "CAREER" -> "O*NET Career Interests"
            "WHO5" -> "WHO-5 Well-being"
            "RSES" -> "Rosenberg Self-Esteem"
            else -> result.type ?: "Assessment"
        }
    }

    // 根据当前语言动态生成显示内容
    val displayLabel = if (isChinese) {
        careerAnchorLabelZh(result.resultLabel) ?: when (result.resultLabel) {
            "Secure" -> "安全型"
            "Anxious-leaning" -> "焦虑型倾向"
            "Avoidant-leaning" -> "回避型倾向"
            "ISTJ" -> "物流师型"
            "ISFJ" -> "守护者型"
            "INFJ" -> "提倡者型"
            "INTJ" -> "建筑师型"
            "ISTP" -> "鉴赏家型"
            "ISFP" -> "探险家型"
            "INFP" -> "调停者型"
            "INTP" -> "逻辑学家型"
            "ESTP" -> "企业家型"
            "ESFP" -> "表演者型"
            "ENFP" -> "竞选者型"
            "ENTP" -> "辩论家型"
            "ESTJ" -> "总经理型"
            "ESFJ" -> "执政官型"
            "ENFJ" -> "主人公型"
            "ENTJ" -> "指挥官型"
            else -> result.resultLabel ?: "结果"
        }
    } else {
        careerAnchorLabelEn(result.resultLabel) ?: (result.resultLabel ?: "Result")
    }

    val displaySummary = if (isChinese) {
        when (result.type) {
            "MBTI" -> "您的 MBTI 风格偏好结果为" + displayLabel + "。这是用于沟通偏好的轻量自我反思模式，不是官方 MBTI 量表或临床标签。"
            "ATTACHMENT" -> "您的依恋风格简版结果为" + displayLabel + "。请将其视为觉察关系习惯的提示，保持灵活与关怀。"
            "CAREER" -> "您的主 O*NET 职业兴趣是" + displayLabel + "。请结合下方 RIASEC 六个维度的相对高低一起阅读，而非只看单一标签。"
            "WHO5" -> "您的 WHO-5 幸福感结果为" + displayLabel + "。请把它作为近期状态反思，不作为临床判断。"
            "RSES" -> "您的 Rosenberg 自尊结果为" + displayLabel + "。请把它作为自我接纳状态的反思提示。"
            else -> "您的 IPIP 大五人格 20 题最高维度是" + displayLabel + "。此分数模式可支持对习惯、优势和发展领域的反思，不使用人口常模诊断。"
        }
    } else {
        when (result.type) {
            "MBTI" -> "Your MBTI-style preference result suggests " + displayLabel + ". This is a light self-reflection pattern for communication preferences, not the official MBTI instrument or a clinical label."
            "ATTACHMENT" -> "Your attachment short-form result is " + displayLabel + ". Use it as a prompt for noticing relationship habits with care and flexibility."
            "CAREER" -> "Your primary O*NET career interest area is " + displayLabel + ". Read the relative highs and lows across all six RIASEC dimensions rather than relying on one label alone."
            "WHO5" -> "Your WHO-5 well-being result is " + displayLabel + ". Treat it as recent-state reflection, not a clinical judgement."
            "RSES" -> "Your Rosenberg self-esteem result is " + displayLabel + ". Use it as a reflection prompt about self-acceptance."
            else -> "Your IPIP Big Five-20 highest current dimension is " + displayLabel + ". The score pattern can support reflection on habits, strengths, and growth areas without applying population norms."
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = InsightSpacing.ScreenHorizontal),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        AppTopBar(
            title = stringResource(R.string.result_title, displayType),
            subtitle = formatDate(result.createdAt, isChinese),
            onBack = onBackToAssessments
        )

        // 结果卡片
        ResultHero(result = result, isChinese = isChinese)

        // 摘要卡片
        InsightCard(
            title = if (isChinese) "结果摘要" else "Summary",
            badge = if (isChinese) "反思" else "Reflection"
        ) {
            Text(
                text = displaySummary,
                style = MaterialTheme.typography.bodyLarge,
                color = InsightMuted
            )
            Text(
                text = if (isChinese) "此结果仅供轻量自我反思和比赛演示使用。" else "This result is for lightweight self-reflection and competition demonstration.",
                style = MaterialTheme.typography.bodyMedium,
                color = InsightMuted,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        // 分数卡片
        InsightCard(
            title = if (isChinese) "分数详情" else "Scores",
            badge = if (isChinese) "详情" else "Details"
        ) {
            val scores = result.scores.orEmpty()
            if (scores.isEmpty()) {
                Text(
                    text = if (isChinese) "此结果未返回分数详情。" else "No score details were returned for this result.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InsightMuted
                )
            } else {
                scores.entries.forEachIndexed { index, entry ->
                    val dimensionLabel = getDimensionLabel(entry.key, isChinese)
                    ScoreBar(
                        label = dimensionLabel,
                        score = entry.value.toDisplayScore(),
                        tone = when (index % 3) {
                            0 -> ScoreTone.Primary
                            1 -> ScoreTone.Success
                            else -> ScoreTone.Warning
                        }
                    )
                }
            }
        }

        if (!result.createdAt.isNullOrBlank()) {
            InsightCard(title = if (isChinese) "已保存" else "Saved") {
                InfoRow(label = if (isChinese) "创建时间" else "Created", value = formatDate(result.createdAt, isChinese))
            }
        }

        SecondaryActionButton(
            text = if (isChinese) "返回测评列表" else "Back to Assessments",
            onClick = onBackToAssessments
        )
        PrimaryActionButton(
            text = if (isChinese) "返回首页" else "Back to Home",
            onClick = onBackHome
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun ResultHero(result: AssessmentResultDto, isChinese: Boolean) {
    val config = assessmentConfigs.find { it.type == result.type }
    val displayType = if (isChinese) {
        config?.displayNameZh ?: result.type ?: "测评"
    } else {
        config?.displayNameEn ?: when (result.type?.uppercase()) {
            "BFI10" -> "IPIP Big Five-20"
            "MBTI" -> "MBTI-style Preferences"
            "WHO5" -> "WHO-5 Well-being"
            "RSES" -> "Rosenberg Self-Esteem"
            else -> result.type ?: "Assessment"
        }
    }
    val displayLabel = if (isChinese) {
        careerAnchorLabelZh(result.resultLabel) ?: when (result.resultLabel) {
            "Secure" -> "安全型"
            "Anxious-leaning" -> "焦虑型倾向"
            "Avoidant-leaning" -> "回避型倾向"
            else -> result.resultLabel ?: "结果"
        }
    } else {
        careerAnchorLabelEn(result.resultLabel) ?: (result.resultLabel ?: "Result")
    }

    InsightCard(title = displayLabel, badge = displayType) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(74.dp)
                    .background(Brush.linearGradient(listOf(InsightPrimary, InsightPrimary2)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayLabel.take(3),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = displayType, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = if (isChinese) "来自您统一档案的已保存反思结果。" else "A saved reflection result from your unified profile.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InsightMuted
                )
            }
        }
    }
}

@Composable
private fun ScoreDetailItem(
    label: String,
    score: Double,
    isLast: Boolean,
    isChinese: Boolean
) {
    val intScore = (score * 20).toInt().coerceIn(0, 100)
    val color = when {
        intScore >= 70 -> Color(0xFF4CAF50)
        intScore >= 40 -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = InsightText
            )
            Text(
                text = "$intScore",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(intScore / 100f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
        if (!isLast) {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

// ==================== 辅助函数 ====================

private fun getDimensionChinese(dimension: String, isChinese: Boolean): String {
    if (!isChinese) return dimension
    return when (dimension) {
        "Extraversion" -> "外向性"
        "Agreeableness" -> "宜人性"
        "Conscientiousness" -> "尽责性"
        "Neuroticism" -> "神经质"
        "Openness" -> "开放性"
        "Security" -> "安全稳定"
        "Anxiety" -> "焦虑型"
        "Well-being" -> "幸福感"
        "Self-esteem" -> "自尊"
        "Realistic" -> "现实型"
        "Investigative" -> "研究型"
        "Artistic" -> "艺术型"
        "Social" -> "社会型"
        "Enterprising" -> "企业型"
        "Conventional" -> "事务型"
        "Avoidance" -> "回避型"
        else -> dimension
    }
}

private fun responseOptions(type: String, isChinese: Boolean): List<Pair<Int, String>> {
    return when (type.uppercase()) {
        "WHO5" -> listOf(
            0 to (if (isChinese) "从未" else "At no time"),
            1 to (if (isChinese) "偶尔" else "Some of the time"),
            2 to (if (isChinese) "少于一半时间" else "Less than half the time"),
            3 to (if (isChinese) "超过一半时间" else "More than half the time"),
            4 to (if (isChinese) "大部分时间" else "Most of the time"),
            5 to (if (isChinese) "一直" else "All of the time")
        )
        "RSES" -> listOf(
            1 to (if (isChinese) "非常不同意" else "Strongly disagree"),
            2 to (if (isChinese) "不同意" else "Disagree"),
            3 to (if (isChinese) "同意" else "Agree"),
            4 to (if (isChinese) "非常同意" else "Strongly agree")
        )
        else -> listOf(
            1 to (if (isChinese) "非常不同意" else "Strongly Disagree"),
            2 to (if (isChinese) "不同意" else "Disagree"),
            3 to (if (isChinese) "一般" else "Neutral"),
            4 to (if (isChinese) "同意" else "Agree"),
            5 to (if (isChinese) "非常同意" else "Strongly Agree")
        )
    }
}

private fun getDimensionLabel(dimension: String, isChinese: Boolean): String {
    if (!isChinese) return dimension
    return when (dimension) {
        "Extraversion" -> "外向性"
        "Agreeableness" -> "宜人性"
        "Conscientiousness" -> "尽责性"
        "Neuroticism" -> "神经质"
        "Openness" -> "开放性"
        "Openness to Experience" -> "开放性"
        "Security" -> "安全稳定"
        "Anxiety" -> "焦虑型"
        "Avoidance" -> "回避型"
        "Well-being" -> "幸福感"
        "Self-esteem" -> "自尊"
        "Neuroticism/Emotional Stability" -> "神经质/情绪稳定性"
        "Introversion" -> "内向性"
        "Intuition" -> "直觉"
        "Thinking" -> "思考"
        "Feeling" -> "情感"
        "Judging" -> "判断"
        "Perceiving" -> "感知"
        "Realistic" -> "现实型"
        "Investigative" -> "研究型"
        "Artistic" -> "艺术型"
        "Social" -> "社会型"
        "Enterprising" -> "企业型"
        "Conventional" -> "事务型"
        else -> dimension
    }
}

private fun careerAnchorLabelZh(label: String?): String? = when (label) {
    "Realistic" -> "现实型"
    "Investigative" -> "研究型"
    "Artistic" -> "艺术型"
    "Social" -> "社会型"
    "Enterprising" -> "企业型"
    "Conventional" -> "事务型"
    else -> null
}

private fun careerAnchorLabelEn(label: String?): String? = when (label) {
    "现实型" -> "Realistic"
    "研究型" -> "Investigative"
    "艺术型" -> "Artistic"
    "社会型" -> "Social"
    "企业型" -> "Enterprising"
    "事务型" -> "Conventional"
    "Realistic" -> "Realistic"
    "Investigative" -> "Investigative"
    "Artistic" -> "Artistic"
    "Social" -> "Social"
    "Enterprising" -> "Enterprising"
    "Conventional" -> "Conventional"
    else -> null
}

private fun Double.toDisplayScore(): Int {
    return (this * 20).toInt().coerceIn(0, 100)
}

private fun formatDate(dateString: String?, isChinese: Boolean): String {
    if (dateString.isNullOrBlank()) return ""
    return try {
        val date = java.time.LocalDateTime.parse(dateString)
        val formatter = java.time.format.DateTimeFormatter.ofPattern(
            if (isChinese) "yyyy-MM-dd HH:mm" else "MMM dd, yyyy HH:mm"
        )
        date.format(formatter)
    } catch (e: Exception) {
        dateString.take(16)
    }
}
