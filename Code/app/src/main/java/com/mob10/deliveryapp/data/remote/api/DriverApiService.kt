package com.mob10.deliveryapp.data.remote.api

import com.mob10.deliveryapp.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Driver/Shipper API Service — 8 endpoints theo API Contract.
 *
 * Tất cả đều cần Authorization header (AuthInterceptor tự gắn).
 * Server xác định driver từ token, không cần truyền driverId.
 */
interface DriverApiService {

    /** Đơn chờ chưa bị Reject bởi tài xế hiện tại (Open Pool). */
    @GET("driver/orders/open")
    suspend fun getOpenOrders(): Response<List<OrderResponseDto>>

    /** Đơn tài xế đang/đã phụ trách (My Orders). */
    @GET("driver/orders/mine")
    suspend fun getMyOrders(): Response<List<OrderResponseDto>>

    /** Nhận đơn (atomic — chỉ 1 tài xế thành công). */
    @POST("driver/orders/{id}/accept")
    suspend fun acceptOrder(@Path("id") orderId: Long): Response<OrderResponseDto>

    /** Từ chối đơn (không xóa khỏi Open Pool chung). */
    @POST("driver/orders/{id}/reject")
    suspend fun rejectOrder(
        @Path("id") orderId: Long,
        @Body request: RejectOrderRequestDto
    ): Response<RejectResultDto>

    /** Cập nhật trạng thái đơn theo chuỗi hợp lệ. */
    @PATCH("driver/orders/{id}/status")
    suspend fun updateOrderStatus(
        @Path("id") orderId: Long,
        @Body request: UpdateStatusRequestDto
    ): Response<OrderResponseDto>

    /** Danh sách lý do từ chối từ backend. */
    @GET("driver/rejection-reasons")
    suspend fun getRejectionReasons(): Response<List<RejectionReasonResponseDto>>

    /** Reliability Score và trạng thái khóa của tài xế hiện tại. */
    @GET("driver/statistics/me")
    suspend fun getMyStatistics(): Response<DriverStatisticsResponseDto>

    /** Cập nhật trạng thái sẵn sàng: AVAILABLE, BUSY, OFFLINE. */
    @PATCH("driver/availability")
    suspend fun updateAvailability(
        @Body request: UpdateAvailabilityRequestDto
    ): Response<String>  // Backend trả về enum string
}
