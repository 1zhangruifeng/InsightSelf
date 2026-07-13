package com.example.insightself.data.model

data class AssessmentSubmitRequest(
    val userId: Long,
    val answers: List<Answer>
) {
    data class Answer(
        val questionId: Long,
        val score: Int
    )
}