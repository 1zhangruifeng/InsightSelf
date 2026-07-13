package com.example.insightself.data.model

data class AssessmentQuestionDto(
    val id: Long,
    val questionText: String,
    val dimension: String,
    val reverseScore: Boolean,
    val instrumentVersion: String? = null,
    val sourceNote: String? = null
)
