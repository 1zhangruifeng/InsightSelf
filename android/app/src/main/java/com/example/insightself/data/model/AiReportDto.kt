package com.example.insightself.data.model

data class AiReportDto(
    val id: Long? = null,
    val userId: Long? = null,
    val reportText: String? = null,
    val generatedBy: String? = null,
    val createdAt: String? = null,
    val sourceExplanation: String? = null,
    val safetyNotice: String? = null
)
