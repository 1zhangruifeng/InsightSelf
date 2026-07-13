package com.example.insightself.data.api

import com.example.insightself.data.auth.SessionAuth
import okhttp3.Interceptor
import okhttp3.Response

class AuthHeaderInterceptor(
    private val accessTokenProvider: () -> String? = { SessionAuth.bearerTokenOrNull() }
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val accessToken = accessTokenProvider()
        val request = if (accessToken.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request()
                .newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        }
        return chain.proceed(request)
    }
}
