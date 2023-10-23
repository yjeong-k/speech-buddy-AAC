package com.example.speechbuddy.service

import com.example.speechbuddy.data.remote.models.AuthTokenDto
import com.example.speechbuddy.data.remote.requests.AuthLoginRequest
import com.example.speechbuddy.data.remote.requests.AuthSignupRequest
import com.example.speechbuddy.data.remote.requests.AuthVerifyEmailAcceptRequest
import com.example.speechbuddy.data.remote.requests.AuthVerifyEmailSendRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {

    @POST("/user/signup/")
    suspend fun signup(
        @Body signupRequest: AuthSignupRequest
    ): Response<Void>

    @POST("/user/login/")
    suspend fun login(
        @Body loginRequest: AuthLoginRequest
    ): Response<AuthTokenDto>

    @POST("/user/validateemail/signup/send/")
    suspend fun verify_send_signup(
        @Body verifyEmailSendRequest: AuthVerifyEmailSendRequest
    ): Response<Void>

    @POST("/user/validateemail/pw/send/")
    suspend fun verify_send_pw(
        @Body verifyEmailSendRequest: AuthVerifyEmailSendRequest
    ): Response<Void>

    @POST("/user/validateemail/signup/accept/")
    suspend fun verify_accept_signup(
        @Body verifyEmailAcceptRequest: AuthVerifyEmailAcceptRequest
    ): Response<Void>

    @POST("/user/validateemail/pw/accept/")
    suspend fun verify_accept_pw(
        @Body verifyEmailAcceptRequest: AuthVerifyEmailAcceptRequest
    ): Response<AuthTokenDto>

}