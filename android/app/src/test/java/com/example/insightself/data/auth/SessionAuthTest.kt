package com.example.insightself.data.auth

import com.example.insightself.data.api.AuthTokenStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SessionAuthTest {
    @Test
    fun bearerTokenOrNull_returnsMemoryTokenWhenPresent() {
        AuthTokenStore.accessToken = "in-memory-token"
        assertEquals("in-memory-token", SessionAuth.bearerTokenOrNull())
        AuthTokenStore.accessToken = null
    }

    @Test
    fun bearerTokenOrNull_returnsNullWhenUninitializedAndMemoryEmpty() {
        AuthTokenStore.accessToken = null
        assertNull(SessionAuth.bearerTokenOrNull())
    }
}
