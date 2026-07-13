package com.example.insightself.ui.screens.report

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.insightself.R
import com.example.insightself.data.local.LanguageManager
import com.example.insightself.data.model.AiReportDto
import com.example.insightself.ui.components.AppTopBar
import com.example.insightself.ui.components.EmptyView
import com.example.insightself.ui.components.ErrorView
import com.example.insightself.ui.components.InsightCard
import com.example.insightself.ui.components.LoadingView
import com.example.insightself.ui.components.PrimaryActionButton
import com.example.insightself.ui.components.SecondaryActionButton
import com.example.insightself.ui.theme.InsightMuted
import com.example.insightself.ui.theme.InsightSpacing
import com.example.insightself.ui.theme.InsightText
import com.example.insightself.utils.ReportExporter
import com.example.insightself.utils.ReportMarkdownParser
import com.example.insightself.viewmodel.AiReportViewModel
import com.example.insightself.viewmodel.UiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun AiReportScreen(
    aiReportViewModel: AiReportViewModel,
    onSessionExpired: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val reportState by aiReportViewModel.reportState.collectAsState()
    val generating by aiReportViewModel.generating.collectAsState()
    val context = LocalContext.current
    val exporter = remember { ReportExporter(context) }
    var showExportDialog by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }

    var currentLanguage by remember { mutableStateOf("en") }

    LaunchedEffect(Unit) {
        LanguageManager.getLanguageFlow(context).collect { lang ->
            if (currentLanguage != lang) {
                currentLanguage = lang
                aiReportViewModel.loadLatest()
            }
        }
    }

    LaunchedEffect(Unit) {
        aiReportViewModel.loadLatest()
    }

    val isChinese = currentLanguage == "zh"

    val fallbackReportContent = fallbackReportText(isChinese)
    val exportReportContent = when (val state = reportState) {
        is UiState.Success -> state.data?.reportText?.takeIf { it.isNotBlank() } ?: fallbackReportContent
        else -> fallbackReportContent
    }
    val reportTitle = if (isChinese) "InsightSelf 综合报告" else "InsightSelf Integrated Report"

    // 导出对话框
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text(if (isChinese) "导出报告" else "Export Report") },
            text = { Text(if (isChinese) "请选择导出格式：" else "Please select export format:") },
            confirmButton = {
                Column {
                    Button(
                        onClick = {
                            showExportDialog = false
                            isExporting = true
                            CoroutineScope(Dispatchers.IO).launch {
                                val file = exporter.exportAsPdf(exportReportContent, reportTitle)
                                isExporting = false
                                file?.let { exporter.shareFile(it) }
                            }
                        }
                    ) {
                        Text(if (isChinese) "PDF 格式" else "PDF Format")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            showExportDialog = false
                            isExporting = true
                            CoroutineScope(Dispatchers.IO).launch {
                                val file = exporter.exportAsImage(exportReportContent, reportTitle)
                                isExporting = false
                                file?.let { exporter.shareFile(it) }
                            }
                        }
                    ) {
                        Text(if (isChinese) "图片格式 (PNG)" else "Image Format (PNG)")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text(if (isChinese) "取消" else "Cancel")
                }
            }
        )
    }

    when (val state = reportState) {
        UiState.Idle,
        UiState.Loading -> LoadingView(modifier = modifier.fillMaxSize(), message = if (isChinese) "加载报告中..." else stringResource(R.string.loading_report))
        is UiState.Error -> {
            val showLoginAgain = state.message.contains("log in", ignoreCase = true)
            ErrorView(
                message = state.message,
                modifier = modifier.fillMaxSize(),
                onRetry = aiReportViewModel::loadLatest,
                secondaryActionLabel = if (showLoginAgain) {
                    stringResource(R.string.log_in_again)
                } else {
                    null
                },
                onSecondaryAction = if (showLoginAgain) onSessionExpired else null
            )
        }
        is UiState.Success -> {
            val report = state.data
            if (report == null) {
                EmptyReportView(
                    generating = generating,
                    onGenerate = aiReportViewModel::generate,
                    modifier = modifier,
                    isChinese = isChinese
                )
            } else {
                ReportContent(
                    report = report,
                    generating = generating,
                    onRefresh = aiReportViewModel::loadLatest,
                    onExport = { showExportDialog = true },
                    isExporting = isExporting,
                    modifier = modifier,
                    isChinese = isChinese
                )
            }
        }
    }
}

@Composable
private fun EmptyReportView(
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
            title = if (isChinese) "报告总览" else "Report Overview",
            subtitle = if (isChinese) "综合自我反思报告" else "Integrated self-reflection report"
        )
        if (generating) {
            LoadingView(modifier = Modifier.weight(1f), message = if (isChinese) "生成中..." else stringResource(R.string.generating))
        } else {
            EmptyView(
                message = if (isChinese) "尚未生成综合报告。点击下方按钮生成。" else "No integrated report has been generated yet. Click the button below to generate.",
                actionLabel = if (isChinese) "生成报告" else "Generate Report",
                onAction = onGenerate,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ReportContent(
    report: AiReportDto,
    generating: Boolean,
    onRefresh: () -> Unit,
    onExport: () -> Unit,
    isExporting: Boolean,
    modifier: Modifier = Modifier,
    isChinese: Boolean
) {
    val displayText = report.reportText?.takeIf { it.isNotBlank() } ?: fallbackReportText(isChinese)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = InsightSpacing.ScreenHorizontal),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        AppTopBar(
            title = if (isChinese) "报告总览" else "Report Overview",
            subtitle = report.createdAt?.takeIf { it.isNotBlank() } ?: if (isChinese) "综合自我反思报告" else "Integrated self-reflection report"
        )

        InsightCard(
            title = if (isChinese) "综合报告" else "Integrated Report",
            badge = report.generatedBy?.takeIf { it.isNotBlank() } ?: if (isChinese) "反思" else "Reflection"
        ) {
            MarkdownLikeText(text = displayText)
        }

        report.sourceExplanation?.takeIf { it.isNotBlank() }?.let { explanation ->
            InsightCard(
                title = if (isChinese) "AI 来源说明" else "AI Source",
                badge = report.generatedBy?.takeIf { it.isNotBlank() } ?: if (isChinese) "来源" else "Source"
            ) {
                Text(
                    text = if (isChinese) localizeSourceExplanation(explanation) else explanation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = InsightMuted
                )
            }
        }

        InsightCard(
            title = if (isChinese) "安全说明" else "Safety Note",
            badge = if (isChinese) "身心健康" else "Wellness"
        ) {
            Text(
                text = report.safetyNotice?.takeIf { it.isNotBlank() }
                    ?: if (isChinese) {
                        "InsightSelf 仅用于自我反思和身心健康教育，不构成医疗、心理诊断、法律、财务或命运预测建议。"
                    } else {
                        "InsightSelf is for self-reflection and wellness education only. It is not medical, psychological diagnosis, legal, financial, or fate-prediction advice."
                    },
                style = MaterialTheme.typography.bodyMedium,
                color = InsightMuted
            )
        }

        SecondaryActionButton(
            text = if (isChinese) "刷新" else "Refresh",
            onClick = onRefresh
        )

        // 导出报告按钮
        PrimaryActionButton(
            text = if (isChinese) "导出报告" else "Export Report",
            onClick = onExport,
            loading = isExporting,
            enabled = !generating && !isExporting
        )

        Spacer(modifier = Modifier.height(96.dp))
    }
}

private fun localizeSourceExplanation(explanation: String): String {
    return when {
        explanation.contains("Qwen", ignoreCase = true) ->
            "由 Qwen 基于 InsightSelf 的紧凑结构化上下文生成。"
        explanation.contains("template fallback", ignoreCase = true) ->
            "由于未配置 AI 服务，当前报告由确定性的模板兜底生成。"
        else -> "由 InsightSelf 后端生成。"
    }
}

private fun fallbackReportText(isChinese: Boolean): String {
    return if (isChinese) {
        "# 综合洞察\n\n" +
                "## 结论\n\n" +
                "这份报告结合了您的个人档案、基于 6tail/lunar-java 计算的八字结果、Swiss Ephemeris 今日星盘提示和测评历史，形成一份温和的自我反思总结。\n\n" +
                "## 当前模式\n\n" +
                "您的综合模式正在形成中。\n\n" +
                "## 测评说明\n\n" +
                "您的最新测评结果已保存。\n\n" +
                "## 温和建议\n\n" +
                "今天选择一个小的行动，观察它如何影响您的能量和沟通，并根据需要调整。不要把任何分数视为固定的标签或预测。"
    } else {
        "# Integrated Insight\n\n" +
                "## Conclusion\n\n" +
                "This report combines your saved profile, a 6tail/lunar-java Bazi result, today's Swiss Ephemeris astrology prompt, and assessment history into a soft self-reflection summary.\n\n" +
                "## Current Pattern\n\n" +
                "Your integrated pattern is forming.\n\n" +
                "## Assessment Note\n\n" +
                "Your latest assessment result has been saved.\n\n" +
                "## Gentle Suggestion\n\n" +
                "Choose one small action today, observe how it affects your energy and communication, and adjust without treating any score as a fixed label or prediction."
    }
}

@Composable
private fun MarkdownLikeText(text: String) {
    val titleLarge = MaterialTheme.typography.titleLarge
    val titleMedium = MaterialTheme.typography.titleMedium
    val titleSmall = MaterialTheme.typography.titleSmall
    val bodyLarge = MaterialTheme.typography.bodyLarge

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ReportMarkdownParser.parse(text).forEach { line ->
            val isHeading = line.headingLevel > 0
            val lineStyle = when (line.headingLevel) {
                1 -> titleLarge
                2 -> titleMedium
                3 -> titleSmall
                else -> bodyLarge
            }
            Text(
                text = buildAnnotatedString {
                    line.spans.forEach { span ->
                        if (span.text.isEmpty()) return@forEach
                        withStyle(
                            SpanStyle(
                                fontWeight = if (span.bold || isHeading) FontWeight.Bold else FontWeight.Normal
                            )
                        ) {
                            append(span.text)
                        }
                    }
                },
                style = lineStyle,
                color = if (isHeading) InsightText else InsightMuted
            )
        }
    }
}
