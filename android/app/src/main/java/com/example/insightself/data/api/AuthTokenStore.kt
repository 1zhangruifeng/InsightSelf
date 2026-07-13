package com.example.insightself.data.api

object AuthTokenStore {
    @Volatile
    var accessToken: String? = null
}
