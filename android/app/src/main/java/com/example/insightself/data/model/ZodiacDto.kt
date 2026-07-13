package com.example.insightself.data.model

data class ZodiacDailyDto(
    val id: Long?,
    val userId: Long?,
    val zodiacSign: String?,
    val date: String?,
    val emotionScore: Int?,
    val communicationScore: Int?,
    val actionScore: Int?,
    val suggestion: String?,
    val createdAt: String?
)

data class ZodiacMatchRequest(
    val userId: Long,
    val targetNickname: String,
    val targetBirthDate: String,
    val targetPersonalityTag: String? = null
)

data class ZodiacMatchResultDto(
    val targetNickname: String?,
    val targetZodiacSign: String?,
    val zodiacScore: Int?,
    val personalityScore: Int?,
    val finalScore: Int?,
    val level: String?,
    val communicationTips: String?,
    val riskNotes: String?,
    val collaborationMode: String?
)
