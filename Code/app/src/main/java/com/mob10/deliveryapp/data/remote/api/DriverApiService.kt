package com.mob10.deliveryapp.data.remote.api

import com.mob10.deliveryapp.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * API tài xế — 8 endpoint theo hợp đồng API.
 *
 * Tất cả đều cần Authorization header (AuthInterceptor tự gắn).
 * Server xác định driver từ token, không cần truyền driverId.
 */
interface DriverApiService {
    /** Đơn chờ mà tài xế hiện tại chưa từ chối. */
    @GET("driver/orders/open")
    suspend fun getOpenOrders(): Response<List<OrderResponseDto>>

    /** Đơn tài xế đang/đã phụ trách (My Orders). */
    @GET("driver/orders/mine")
    suspend fun getMyOrders(): Response<List<OrderResponseDto>>

    /** Nhận đơn (atomic — chỉ 1 tài xế thành công). */
    @POST("driver/orders/{id}/accept")
    suspend fun acceptOrder(@Path("id") orderId: Long): Response<OrderResponseDto>

    /** Từ chối đơn nhưng vẫn giữ đơn trong danh sách chờ chung. */
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

    /** Điểm tin cậy và trạng thái giới hạn của tài xế hiện tại. */
    @GET("driver/statistics/me")
    suspend fun getMyStatistics(): Response<DriverStatisticsResponseDto>

    /** Cập nhật trạng thái sẵn sàng: AVAILABLE, BUSY, OFFLINE. */
    @PATCH("driver/availability")
    suspend fun updateAvailability(
        @Body request: UpdateAvailabilityRequestDto
    ): Response<String>  // Backend trả về enum string
}

data class DeliveryPhoto(val image: String?)

interface DeliveryPhotoApi {
    @GET("orders/{id}/driver-avatar")
    suspend fun driverAvatar(@Path("id") id: Long): Response<DeliveryPhoto>
    @POST("driver/orders/{id}/complete-with-photo")
    suspend fun completeWithPhoto(@Path("id") id: Long, @Body photo: DeliveryPhoto): Response<OrderResponseDto>
    @GET("orders/{id}/delivery-photo")
    suspend fun deliveryPhoto(@Path("id") id: Long): Response<DeliveryPhoto>
}
