package com.example.insightself.data.model

data class AssessmentResultDto(
    val id: Long? = null,
    val userId: Long? = null,
    val type: String? = null,
    val resultLabel: String? = null,
    val scores: Map<String, Double>? = null,
    val summary: String? = null,
    val createdAt: String? = null
)