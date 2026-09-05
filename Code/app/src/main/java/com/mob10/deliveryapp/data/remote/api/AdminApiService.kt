package com.mob10.deliveryapp.data.remote.api

import com.mob10.deliveryapp.data.remote.dto.AdminDriverResponseDto
import com.mob10.deliveryapp.data.remote.dto.AdminUserResponseDto
import com.mob10.deliveryapp.data.remote.dto.OrderResponseDto
import retrofit2.Response
import retrofit2.http.GET

/**
 * Admin API Service — 4 endpoints theo API Contract.
 *
 * Tất cả đều cần Authorization header với role ADMIN.
 */
interface AdminApiService {

    /** Danh sách tất cả tài khoản trong hệ thống. */
    @GET("admin/users")
    suspend fun getUsers(): Response<List<AdminUserResponseDto>>

    /** Danh sách tài xế kèm thống kê điểm tin cậy. */
    @GET("admin/drivers")
    suspend fun getDrivers(): Response<List<AdminDriverResponseDto>>

    /** Tài xế có điểm tin cậy dưới 70 — cần cảnh báo. */
    @GET("admin/drivers/alerts")
    suspend fun getDriverAlerts(): Response<List<AdminDriverResponseDto>>

    /** Toàn bộ đơn hàng trong hệ thống. */
    @GET("admin/orders")
    suspend fun getAllOrders(): Response<List<OrderResponseDto>>
}
