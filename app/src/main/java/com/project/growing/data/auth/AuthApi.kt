package com.project.growing.data.auth

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("/login")
    suspend fun login(
        @Body request: LoginRequest,
    ): Response<LoginResponse>

    @POST("/Signup")
    suspend fun signUp(
        @Body request: SignUpRequest,
    ): Response<SignUpResponse>
}