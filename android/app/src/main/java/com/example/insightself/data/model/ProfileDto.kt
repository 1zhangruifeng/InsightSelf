package com.example.insightself.data.model

data class ProfileDto(
    val id: Long? = null,
    val userId: Long? = null,
    val nickname: String,
    val gender: String? = null,
    val birthDate: String,
    val birthTime: String? = null,
    val birthPlace: String? = null,
    val birthTimezone: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val calendarType: String = "SOLAR",
    val preference: String = "BALANCED",
    val aiEnabled: Boolean = false,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val language: String = "en"
)
