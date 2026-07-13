package com.example.insightself.data.api

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.concurrent.TimeUnit

class AuthHeaderInterceptorTest {
    @After
    fun tearDown() {
        AuthTokenStore.accessToken = null
    }

    @Test
    fun addsBearerAuthorizationHeaderWhenAccessTokenExists() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(200).setBody("{}"))
        server.start()

        try {
            AuthTokenStore.accessToken = "access-token-123"
            val client = OkHttpClient.Builder()
                .addInterceptor(AuthHeaderInterceptor())
                .build()

            val request = Request.Builder()
                .url(server.url("/secure"))
                .build()

            client.newCall(request).execute().use { response ->
                assertEquals(200, response.code)
            }

            val recordedRequest = server.takeRequest(5, TimeUnit.SECONDS)
            assertEquals("Bearer access-token-123", recordedRequest?.getHeader("Authorization"))
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun leavesAuthorizationHeaderUnsetWhenAccessTokenIsMissing() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(200).setBody("{}"))
        server.start()

        try {
            AuthTokenStore.accessToken = null
            val client = OkHttpClient.Builder()
                .addInterceptor(AuthHeaderInterceptor())
                .build()

            val request = Request.Builder()
                .url(server.url("/public"))
                .build()

            client.newCall(request).execute().use { response ->
                assertEquals(200, response.code)
            }

            val recordedRequest = server.takeRequest(5, TimeUnit.SECONDS)
            assertNull(recordedRequest?.getHeader("Authorization"))
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun leavesAuthorizationHeaderUnsetWhenAccessTokenIsBlank() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(200).setBody("{}"))
        server.start()

        try {
            AuthTokenStore.accessToken = " "
            val client = OkHttpClient.Builder()
                .addInterceptor(AuthHeaderInterceptor())
                .build()

            val request = Request.Builder()
                .url(server.url("/public"))
                .build()

            client.newCall(request).execute().use { response ->
                assertEquals(200, response.code)
            }

            val recordedRequest = server.takeRequest(5, TimeUnit.SECONDS)
            assertNull(recordedRequest?.getHeader("Authorization"))
        } finally {
            server.shutdown()
        }
    }
}
