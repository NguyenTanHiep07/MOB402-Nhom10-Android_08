package com.mob10.deliveryapp.data.repository

import com.mob10.deliveryapp.data.model.Order
import com.mob10.deliveryapp.data.model.StatusHistory
import com.mob10.deliveryapp.data.remote.RemoteDataSource
import com.mob10.deliveryapp.data.remote.api.OrderApiService
import com.mob10.deliveryapp.data.remote.dto.CreateOrderRequestDto
import com.mob10.deliveryapp.data.remote.mapper.OrderMapper.toDomain
import com.mob10.deliveryapp.data.remote.mapper.OrderMapper.toDomainHistoryList
import com.mob10.deliveryapp.data.remote.mapper.OrderMapper.toDomainList
import com.mob10.deliveryapp.data.util.NetworkResult
import com.mob10.deliveryapp.data.util.mapData

/**
 * Repository REST cho Client (khách hàng) — thao tác đơn hàng qua API backend.
 *
 * Tất cả endpoint đều yêu cầu JWT token với role CLIENT.
 * Server xác định client từ token — không cần truyền clientId.
 *
 * Interface này là nguồn dữ liệu duy nhất cho UI khách hàng,
 * thay thế việc gọi Room trực tiếp (DeliveryRepository) cho các luồng online.
 */
class ClientOrderRepository(
    private val orderApi: OrderApiService
) {

    /**
     * Tạo đơn hàng mới trên backend.
     *
     * Server sẽ tính lại quãng đường và phí từ tọa độ —
     * không tin giá trị distanceKm do client gửi.
     */
    suspend fun createOrder(request: CreateOrderRequestDto): NetworkResult<Order> =
        RemoteDataSource.safeApiCall {
            orderApi.createOrder(request)
        }.mapData { it.toDomain() }

    /**
     * Danh sách đơn của client hiện tại.
     * Backend trả về theo phạm vi quyền token → chỉ đơn của mình.
     */
    suspend fun getMyOrders(): NetworkResult<List<Order>> =
        RemoteDataSource.safeApiCall {
            orderApi.getOrders()
        }.mapData { it.toDomainList() }

    /** Chi tiết một đơn hàng. */
    suspend fun getOrderById(orderId: Long): NetworkResult<Order> =
        RemoteDataSource.safeApiCall {
            orderApi.getOrderById(orderId)
        }.mapData { it.toDomain() }

    /** Lịch sử trạng thái đơn hàng. */
    suspend fun getOrderHistory(orderId: Long): NetworkResult<List<StatusHistory>> =
        RemoteDataSource.safeApiCall {
            orderApi.getOrderHistory(orderId)
        }.mapData { it.toDomainHistoryList() }

    /**
     * Hủy đơn hàng.
     *
     * Chỉ được phép khi đơn đang ở trạng thái CHO_TIEP_NHAN hoặc DA_CHAP_NHAN.
     * Backend kiểm tra ownership — chỉ chủ đơn mới được hủy.
     */
    suspend fun cancelOrder(orderId: Long): NetworkResult<Order> {
        val result = RemoteDataSource.safeApiCall {
            orderApi.cancelOrder(orderId)
        }
        if (result is NetworkResult.Error && result.code == "INVALID_STATUS_TRANSITION") {
            return result.copy(
                message = "Đơn hàng đã được xử lý, không thể hủy vào lúc này."
            )
        }
        return result.mapData { it.toDomain() }
    }
}
