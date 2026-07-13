package com.example.insightself.data.model

data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)