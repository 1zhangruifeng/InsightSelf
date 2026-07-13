package com.example.insightself.util

import com.example.insightself.data.model.AssessmentResultDto
import com.example.insightself.ui.screens.assessment.AssessmentTypeConfig
import com.example.insightself.ui.screens.assessment.assessmentConfigs

data class AssessmentTypeSnapshot(
    val config: AssessmentTypeConfig,
    val latestResult: AssessmentResultDto?
)

/** One latest result per configured assessment type, in hub display order. */
fun latestAssessmentSnapshots(results: List<AssessmentResultDto>): List<AssessmentTypeSnapshot> {
    val latestByType = results
        .filter { !it.type.isNullOrBlank() }
        .groupBy { it.type!! }
        .mapValues { (_, items) ->
            items.maxWithOrNull(
                compareBy<AssessmentResultDto> { it.createdAt.orEmpty() }
                    .thenBy { it.id ?: 0L }
            )
        }

    return assessmentConfigs.map { config ->
        AssessmentTypeSnapshot(config, latestByType[config.type])
    }
}

fun displayAssessmentResultLabel(resultLabel: String?, isChinese: Boolean): String {
    if (resultLabel.isNullOrBlank()) {
        return if (isChinese) "结果" else "Result"
    }
    if (!isChinese) return resultLabel
    return when (resultLabel) {
        "Secure" -> "安全型"
        "Anxious-leaning" -> "焦虑型倾向"
        "Avoidant-leaning" -> "回避型倾向"
        else -> resultLabel
    }
}
