package com.mob10.deliveryapp.data.remote.api

import com.mob10.deliveryapp.data.remote.dto.LoginRequest
import com.mob10.deliveryapp.data.remote.dto.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Auth API Service — chỉ có endpoint login.
 * Không cần Authorization header (AuthInterceptor tự bỏ qua /auth/login).
 */
interface AuthApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body request: com.mob10.deliveryapp.data.remote.dto.RegisterRequest): Response<LoginResponse>
}
