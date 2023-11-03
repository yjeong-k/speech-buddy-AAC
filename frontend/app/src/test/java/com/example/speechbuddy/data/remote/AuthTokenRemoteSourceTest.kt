package com.example.speechbuddy.data.remote

import com.example.speechbuddy.data.remote.models.AuthTokenDto
import com.example.speechbuddy.data.remote.requests.AuthLoginRequest
import com.example.speechbuddy.data.remote.requests.AuthVerifyEmailRequest
import com.example.speechbuddy.service.AuthService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class AuthTokenRemoteSourceTest {

    private lateinit var authService: AuthService
    private lateinit var authTokenRemoteSource: AuthTokenRemoteSource
    private val errorResponseBody =
        "{\"error\":\"Something went wrong\"}".toResponseBody("application/json".toMediaType())

    @Before
    fun setUp() {
        authService = mockk()
        authTokenRemoteSource = AuthTokenRemoteSource(authService)
    }

    @Test
    fun `should return response with auth token dto when request is valid for login`() =
        runBlocking {
            val request = AuthLoginRequest(email = "test@example.com", password = "password123")
            val expectedResponse: Response<AuthTokenDto> = mockk(relaxed = true)

            coEvery { authService.login(request) } returns expectedResponse

            val result = authTokenRemoteSource.loginAuthToken(request).first()
            assertEquals(expectedResponse, result)
            coVerify(exactly = 1) { authService.login(request) }
        }

    @Test
    fun `should return response with auth token dto when request is valid for verify email`() =
        runBlocking {
            val request = AuthVerifyEmailRequest(email = "test@example.com", code = "123456")
            val expectedResponse: Response<AuthTokenDto> = mockk(relaxed = true)

            coEvery { authService.verifyEmailForResetPassword(request) } returns expectedResponse

            val result = authTokenRemoteSource.verifyEmailForResetPasswordAuthToken(request).first()
            assertEquals(expectedResponse, result)
            coVerify(exactly = 1) { authService.verifyEmailForResetPassword(request) }
        }

    @Test
    fun `should return response with error when request is invalid for login`(): Unit =
        runBlocking {
            val request = AuthLoginRequest(email = "test@example.com", password = "password123")
            val expectedResponse = Response.error<AuthTokenDto>(400, errorResponseBody)
            coEvery { authService.login(request) } returns expectedResponse

            val result = authTokenRemoteSource.loginAuthToken(request).first()

            assertEquals(expectedResponse, result)
            coVerify(exactly = 1) { authService.login(request) }
        }

    @Test
    fun `should return response with error when request is invalid for verify email`(): Unit =
        runBlocking {
            val request = AuthVerifyEmailRequest(email = "test@example.com", code = "123456")
            val expectedResponse = Response.error<AuthTokenDto>(400, errorResponseBody)
            coEvery { authService.verifyEmailForResetPassword(request) } returns expectedResponse

            val result = authTokenRemoteSource.verifyEmailForResetPasswordAuthToken(request).first()

            assertEquals(expectedResponse, result)
            coVerify(exactly = 1) { authService.verifyEmailForResetPassword(request) }
        }

}
