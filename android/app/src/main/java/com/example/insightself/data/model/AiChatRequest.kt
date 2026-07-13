package com.example.insightself.data.model

data class AiChatRequest(
    val userId: Long,
    val message: String,
    val sessionId: String? = null
)
