package com.mob10.deliveryapp.data.remote.api

import com.mob10.deliveryapp.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Order API chung — dùng cho tất cả roles (CLIENT, DELIVERY, ADMIN).
 *
 * Phạm vi trả về phụ thuộc vào role trong token:
 * - CLIENT: chỉ đơn của mình
 * - DELIVERY: đơn mình phụ trách
 * - ADMIN: toàn bộ
 */
interface OrderApiService {

    /** Tạo đơn mới (chỉ CLIENT). */
    @POST("orders")
    suspend fun createOrder(@Body request: CreateOrderRequestDto): Response<OrderResponseDto>

    /** Danh sách đơn theo phạm vi quyền hiện tại. */
    @GET("orders")
    suspend fun getOrders(): Response<List<OrderResponseDto>>

    /** Chi tiết đơn. */
    @GET("orders/{id}")
    suspend fun getOrderById(@Path("id") orderId: Long): Response<OrderResponseDto>

    /** Lịch sử trạng thái đơn. */
    @GET("orders/{id}/history")
    suspend fun getOrderHistory(@Path("id") orderId: Long): Response<List<HistoryResponseDto>>

    /** Hủy đơn (chỉ CLIENT chủ đơn, khi CHO_TIEP_NHAN hoặc DA_CHAP_NHAN). */
    @POST("orders/{id}/cancel")
    suspend fun cancelOrder(@Path("id") orderId: Long): Response<OrderResponseDto>
}
