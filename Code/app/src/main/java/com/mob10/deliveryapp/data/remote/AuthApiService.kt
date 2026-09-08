package com.mob10.deliveryapp.data.remote

import com.mob10.deliveryapp.data.remote.dto.LoginRequest
import com.mob10.deliveryapp.data.remote.dto.LoginResponse
import com.mob10.deliveryapp.data.remote.dto.RegisterRequest
import com.mob10.deliveryapp.data.remote.dto.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // TODO: xác nhận endpoint chính xác và field response với Thịnh (email đã ghi endpoint login,
    // nhưng chưa xác nhận endpoint register).
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>
}