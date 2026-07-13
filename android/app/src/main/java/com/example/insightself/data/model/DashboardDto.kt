package com.example.insightself.data.model

data class DashboardDto(
    val profile: ProfileDto? = null,
    val bazi: BaziDto? = null,
    val zodiacDaily: ZodiacDailyDto? = null,
    val assessmentResults: List<AssessmentResultDto>? = null,
    val latestAiReport: AiReportDto? = null,
    val integratedSummary: String? = null,
    val safetyNotice: String? = null
)
