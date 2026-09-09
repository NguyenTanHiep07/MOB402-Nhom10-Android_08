package com.mob10.deliveryapp.data.remote.api

import com.mob10.deliveryapp.data.remote.dto.LoginRequest
import com.mob10.deliveryapp.data.remote.dto.LoginResponse
import com.mob10.deliveryapp.data.remote.dto.RegisterRequest
import com.mob10.deliveryapp.data.remote.dto.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Auth API Service.
 * Không cần Authorization header (AuthInterceptor tự bỏ qua /auth/login và /auth/register).
 */
interface AuthApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // TODO: xác nhận lại với Thịnh endpoint và response thật của register (đang tạm theo pattern login).
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>
}