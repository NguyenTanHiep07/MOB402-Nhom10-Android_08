package com.mob10.deliveryapp.data.remote

import com.mob10.deliveryapp.data.remote.dto.AdminOrderDto
import com.mob10.deliveryapp.data.remote.dto.AdminUserDto
import retrofit2.Response
import retrofit2.http.GET

interface AdminApiService {

    // TODO: xác nhận đường dẫn chính xác với Thịnh (có thể có phân trang, filter...)
    @GET("api/admin/users")
    suspend fun getAllUsers(): Response<List<AdminUserDto>>

    @GET("api/admin/drivers")
    suspend fun getAllDrivers(): Response<List<AdminUserDto>>

    @GET("api/admin/orders")
    suspend fun getAllOrders(): Response<List<AdminOrderDto>>
}