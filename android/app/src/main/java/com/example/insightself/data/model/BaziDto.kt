package com.example.insightself.data.model

data class BaziDto(
    val id: Long?,
    val userId: Long?,
    val yearPillar: String?,
    val monthPillar: String?,
    val dayPillar: String?,
    val hourPillar: String?,
    val woodScore: Int?,
    val fireScore: Int?,
    val earthScore: Int?,
    val metalScore: Int?,
    val waterScore: Int?,
    val elementScores: Map<String, Int>?,
    val conclusion: String?,
    val evidence: String?,
    val suggestion: String?,
    val createdAt: String?
)
