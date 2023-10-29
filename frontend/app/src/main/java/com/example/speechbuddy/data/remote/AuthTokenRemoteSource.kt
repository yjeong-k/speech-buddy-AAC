package com.example.speechbuddy.data.remote

import com.example.speechbuddy.data.remote.models.AuthTokenDto
import com.example.speechbuddy.data.remote.requests.AuthLoginRequest
import com.example.speechbuddy.data.remote.requests.AuthResetPasswordRequest
import com.example.speechbuddy.data.remote.requests.AuthSignupRequest
import com.example.speechbuddy.data.remote.requests.AuthVerifyEmailAcceptRequest
import com.example.speechbuddy.data.remote.requests.AuthVerifyEmailSendRequest
import com.example.speechbuddy.service.AuthService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response
import javax.inject.Inject

class AuthTokenRemoteSource @Inject constructor(private val authService: AuthService) {

    suspend fun signupAuthToken(authSignupRequest: AuthSignupRequest): Flow<Response<Void>> =
        flow {
            val result = authService.signup(authSignupRequest)
            emit(result)
        }

    suspend fun loginAuthToken(authLoginRequest: AuthLoginRequest): Flow<Response<AuthTokenDto>> =
        flow {
            val result = authService.login(authLoginRequest)
            emit(result)
        }

    suspend fun verifySendSignupAuthToken(authVerifyEmailSendRequest: AuthVerifyEmailSendRequest): Flow<Response<Void>> =
        flow {
            val result = authService.verifySendSignup(authVerifyEmailSendRequest)
            emit(result)
        }

    suspend fun verifySendPWAuthToken(authVerifyEmailSendRequest: AuthVerifyEmailSendRequest): Flow<Response<Void>> =
        flow {
            val result = authService.verifySendPW(authVerifyEmailSendRequest)
            emit(result)
        }

    suspend fun verifyAcceptSignupAuthToken(authVerifyEmailAcceptRequest: AuthVerifyEmailAcceptRequest): Flow<Response<Void>> =
        flow {
            val result = authService.verifyAcceptSignup(authVerifyEmailAcceptRequest)
            emit(result)
        }

    suspend fun verifyAcceptPWAuthToken(authVerifyEmailAcceptRequest: AuthVerifyEmailAcceptRequest): Flow<Response<AuthTokenDto>> =
        flow {
            val result = authService.verifyAcceptPW(authVerifyEmailAcceptRequest)
            emit(result)
        }

    suspend fun resetPasswordAuthToken(
        accessToken: String,
        authResetPasswordRequest: AuthResetPasswordRequest
    ): Flow<Response<Void>> =
        flow {
            val result = authService.resetPassword(accessToken, authResetPasswordRequest)
            emit(result)
        }
}