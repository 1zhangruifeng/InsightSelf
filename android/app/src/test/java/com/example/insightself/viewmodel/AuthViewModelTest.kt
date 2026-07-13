package com.example.insightself.viewmodel

import com.example.insightself.data.api.ApiResponse
import com.example.insightself.data.api.AuthTokenStore
import com.example.insightself.data.local.UserSession
import com.example.insightself.data.local.UserSessionStore
import com.example.insightself.data.model.ProfileDto
import com.example.insightself.data.model.UserDto
import com.example.insightself.data.repository.AuthGateway
import com.example.insightself.data.repository.ProfileGateway
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import retrofit2.HttpException
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @After
    fun tearDown() {
        AuthTokenStore.accessToken = null
    }

    @Test
    fun routeFromSavedSessionWithExistingProfileOpensMainAndRestoresToken() = runTest {
        val sessionStore = FakeUserSessionStore(
            UserSession(
                userId = USER_ID,
                username = "demo",
                accessToken = "saved-access",
                refreshToken = "saved-refresh",
                accessTokenExpiresAt = null,
                refreshTokenExpiresAt = null,
                isLoggedIn = true
            )
        )
        val profileGateway = FakeProfileGateway(
            getProfile = { ApiResponse(success = true, message = "ok", data = profile()) }
        )
        val viewModel = viewModel(sessionStore = sessionStore, profileGateway = profileGateway)

        viewModel.routeFromSavedSession()
        advanceUntilIdle()

        assertEquals(UiState.Success(AuthDestination.Main), viewModel.startupState.value)
        assertEquals("saved-access", AuthTokenStore.accessToken)
        assertEquals(listOf(USER_ID), profileGateway.requestedUserIds)
        assertEquals(0, sessionStore.clearCount)
    }

    @Test
    fun routeFromSavedSessionWithoutAccessTokenClearsSessionAndOpensLogin() = runTest {
        val sessionStore = FakeUserSessionStore(
            UserSession(
                userId = USER_ID,
                username = "demo",
                accessToken = null,
                refreshToken = "saved-refresh",
                accessTokenExpiresAt = null,
                refreshTokenExpiresAt = null,
                isLoggedIn = true
            )
        )
        val viewModel = viewModel(sessionStore = sessionStore)

        viewModel.routeFromSavedSession()
        advanceUntilIdle()

        assertEquals(UiState.Success(AuthDestination.Login), viewModel.startupState.value)
        assertEquals(1, sessionStore.clearCount)
        assertNull(AuthTokenStore.accessToken)
    }

    @Test
    fun routeFromSavedSessionWithMissingProfileOpensOnboarding() = runTest {
        val sessionStore = loggedInSessionStore()
        val profileGateway = FakeProfileGateway(
            getProfile = { throw httpException(404) }
        )
        val viewModel = viewModel(sessionStore = sessionStore, profileGateway = profileGateway)

        viewModel.routeFromSavedSession()
        advanceUntilIdle()

        assertEquals(UiState.Success(AuthDestination.Onboarding), viewModel.startupState.value)
        assertEquals(0, sessionStore.clearCount)
    }

    @Test
    fun routeFromSavedSessionWithExpiredTokenClearsSessionAndOpensLogin() = runTest {
        val sessionStore = loggedInSessionStore()
        val profileGateway = FakeProfileGateway(
            getProfile = { throw httpException(401) }
        )
        val viewModel = viewModel(sessionStore = sessionStore, profileGateway = profileGateway)

        viewModel.routeFromSavedSession()
        advanceUntilIdle()

        assertEquals(UiState.Success(AuthDestination.Login), viewModel.startupState.value)
        assertEquals(1, sessionStore.clearCount)
        assertNull(AuthTokenStore.accessToken)
    }

    @Test
    fun loginWithExistingProfileSavesSessionAndOpensMain() = runTest {
        val sessionStore = loggedOutSessionStore()
        val authGateway = FakeAuthGateway(
            loginResponse = ApiResponse(success = true, message = "ok", data = user())
        )
        val profileGateway = FakeProfileGateway(
            getProfile = { ApiResponse(success = true, message = "ok", data = profile()) }
        )
        val viewModel = viewModel(
            authGateway = authGateway,
            profileGateway = profileGateway,
            sessionStore = sessionStore
        )

        viewModel.login(" demo ", "password")
        advanceUntilIdle()

        assertEquals(UiState.Success(AuthDestination.Main), viewModel.authState.value)
        assertEquals("access-token", sessionStore.savedSession?.accessToken)
        assertEquals("access-token", AuthTokenStore.accessToken)
        assertEquals(listOf("demo" to "password"), authGateway.loginRequests)
    }

    @Test
    fun loginWithMissingBackendAccessTokenShowsError() = runTest {
        val authGateway = FakeAuthGateway(
            loginResponse = ApiResponse(success = true, message = "ok", data = user(accessToken = null))
        )
        val viewModel = viewModel(authGateway = authGateway)

        viewModel.login("demo", "password")
        advanceUntilIdle()

        assertEquals(UiState.Error("Backend did not return an access token."), viewModel.authState.value)
    }

    @Test
    fun blankLoginInputShowsValidationErrorWithoutCallingBackend() = runTest {
        val authGateway = FakeAuthGateway()
        val viewModel = viewModel(authGateway = authGateway)

        viewModel.login(" ", "password")
        advanceUntilIdle()

        assertEquals(UiState.Error("Username and password are required."), viewModel.authState.value)
        assertTrue(authGateway.loginRequests.isEmpty())
    }

    @Test
    fun shortLoginPasswordShowsValidationErrorWithoutCallingBackend() = runTest {
        val authGateway = FakeAuthGateway()
        val viewModel = viewModel(authGateway = authGateway)

        viewModel.login("demo", "123456")
        advanceUntilIdle()

        assertEquals(UiState.Error("Password must be at least 8 characters."), viewModel.authState.value)
        assertTrue(authGateway.loginRequests.isEmpty())
    }

    @Test
    fun seedDemoSavesReturnedDemoSessionAndOpensMain() = runTest {
        val sessionStore = loggedOutSessionStore()
        val authGateway = FakeAuthGateway(
            seedDemoResponse = ApiResponse(success = true, message = "demo seeded", data = user(username = "hkict_demo"))
        )
        val viewModel = viewModel(authGateway = authGateway, sessionStore = sessionStore)

        viewModel.seedDemo()
        advanceUntilIdle()

        assertEquals(UiState.Success(AuthDestination.Main), viewModel.authState.value)
        assertEquals(1, authGateway.seedDemoCount)
        assertEquals("hkict_demo", sessionStore.savedSession?.username)
        assertEquals("access-token", sessionStore.savedSession?.accessToken)
        assertEquals("access-token", AuthTokenStore.accessToken)
    }

    private fun viewModel(
        authGateway: FakeAuthGateway = FakeAuthGateway(),
        profileGateway: FakeProfileGateway = FakeProfileGateway(),
        sessionStore: FakeUserSessionStore = loggedOutSessionStore()
    ): AuthViewModel {
        return AuthViewModel(
            authRepository = authGateway,
            profileRepository = profileGateway,
            sessionDataStore = sessionStore,
            syncLanguagePreference = {}
        )
    }

    private fun loggedInSessionStore(): FakeUserSessionStore {
        return FakeUserSessionStore(
            UserSession(
                userId = USER_ID,
                username = "demo",
                accessToken = "saved-access",
                refreshToken = "saved-refresh",
                accessTokenExpiresAt = null,
                refreshTokenExpiresAt = null,
                isLoggedIn = true
            )
        )
    }

    private fun loggedOutSessionStore(): FakeUserSessionStore {
        return FakeUserSessionStore(
            UserSession(
                userId = null,
                username = null,
                accessToken = null,
                refreshToken = null,
                accessTokenExpiresAt = null,
                refreshTokenExpiresAt = null,
                isLoggedIn = false
            )
        )
    }

    private fun user(accessToken: String? = "access-token", username: String = "demo"): UserDto {
        return UserDto(
            userId = USER_ID,
            username = username,
            createdAt = null,
            accessToken = accessToken,
            refreshToken = "refresh-token",
            accessTokenExpiresAt = "2026-05-15T12:00:00Z",
            refreshTokenExpiresAt = "2026-05-16T12:00:00Z"
        )
    }

    private fun profile(): ProfileDto {
        return ProfileDto(
            id = 1,
            userId = USER_ID,
            nickname = "Demo",
            birthDate = "1990-01-01"
        )
    }

    private fun httpException(code: Int): HttpException {
        val responseBody = "{}".toResponseBody("application/json".toMediaType())
        return HttpException(Response.error<Any>(code, responseBody))
    }

    private class FakeAuthGateway(
        var loginResponse: ApiResponse<UserDto> = ApiResponse(success = false, message = "not configured", data = null),
        var registerResponse: ApiResponse<UserDto> = ApiResponse(success = false, message = "not configured", data = null),
        var seedDemoResponse: ApiResponse<UserDto> = ApiResponse(success = false, message = "not configured", data = null)
    ) : AuthGateway {
        val loginRequests = mutableListOf<Pair<String, String>>()
        val registerRequests = mutableListOf<Pair<String, String>>()
        var seedDemoCount = 0
            private set

        override suspend fun register(username: String, password: String): ApiResponse<UserDto> {
            registerRequests += username to password
            return registerResponse
        }

        override suspend fun login(username: String, password: String): ApiResponse<UserDto> {
            loginRequests += username to password
            return loginResponse
        }

        override suspend fun seedDemo(): ApiResponse<UserDto> {
            seedDemoCount += 1
            return seedDemoResponse
        }
    }

    private class FakeProfileGateway(
        private val getProfile: suspend (Long) -> ApiResponse<ProfileDto> = {
            ApiResponse(success = false, message = "not configured", data = null)
        }
    ) : ProfileGateway {
        val requestedUserIds = mutableListOf<Long>()

        override suspend fun getProfile(userId: Long): ApiResponse<ProfileDto> {
            requestedUserIds += userId
            return getProfile.invoke(userId)
        }
    }

    private class FakeUserSessionStore(
        initialSession: UserSession
    ) : UserSessionStore {
        private val sessions = MutableStateFlow(initialSession)
        var clearCount = 0
            private set
        var savedSession: SavedSession? = null
            private set

        override suspend fun saveSession(
            userId: Long,
            username: String,
            accessToken: String,
            refreshToken: String,
            accessTokenExpiresAt: String?,
            refreshTokenExpiresAt: String?
        ) {
            savedSession = SavedSession(
                userId = userId,
                username = username,
                accessToken = accessToken,
                refreshToken = refreshToken,
                accessTokenExpiresAt = accessTokenExpiresAt,
                refreshTokenExpiresAt = refreshTokenExpiresAt
            )
            sessions.value = UserSession(
                userId = userId,
                username = username,
                accessToken = accessToken,
                refreshToken = refreshToken,
                accessTokenExpiresAt = accessTokenExpiresAt,
                refreshTokenExpiresAt = refreshTokenExpiresAt,
                isLoggedIn = true
            )
            AuthTokenStore.accessToken = accessToken
        }

        override suspend fun clearSession() {
            clearCount += 1
            sessions.value = UserSession(
                userId = null,
                username = null,
                accessToken = null,
                refreshToken = null,
                accessTokenExpiresAt = null,
                refreshTokenExpiresAt = null,
                isLoggedIn = false
            )
            AuthTokenStore.accessToken = null
        }

        override fun observeSession(): Flow<UserSession> = sessions

        override suspend fun getCurrentUserId(): Long? = sessions.value.userId
    }

    private data class SavedSession(
        val userId: Long,
        val username: String,
        val accessToken: String,
        val refreshToken: String,
        val accessTokenExpiresAt: String?,
        val refreshTokenExpiresAt: String?
    )

    companion object {
        private const val USER_ID = 42L
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
