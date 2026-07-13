package com.example.insightself.data.api

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?
)
