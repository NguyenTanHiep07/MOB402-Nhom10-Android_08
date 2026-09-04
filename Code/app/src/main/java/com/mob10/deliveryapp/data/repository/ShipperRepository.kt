package com.mob10.deliveryapp.data.repository

import com.mob10.deliveryapp.data.model.DeliveryStatus
import com.mob10.deliveryapp.data.model.DriverStatistics
import com.mob10.deliveryapp.data.model.Order
import com.mob10.deliveryapp.data.model.RejectionReason
import com.mob10.deliveryapp.data.model.RejectInfo
import com.mob10.deliveryapp.data.model.StatusHistory
import com.mob10.deliveryapp.data.remote.RemoteDataSource
import com.mob10.deliveryapp.data.remote.api.DriverApiService
import com.mob10.deliveryapp.data.remote.api.OrderApiService
import com.mob10.deliveryapp.data.remote.dto.RejectOrderRequestDto
import com.mob10.deliveryapp.data.remote.dto.UpdateAvailabilityRequestDto
import com.mob10.deliveryapp.data.remote.dto.UpdateStatusRequestDto
import com.mob10.deliveryapp.data.remote.mapper.OrderMapper.toDomain
import com.mob10.deliveryapp.data.remote.mapper.OrderMapper.toDomainHistoryList
import com.mob10.deliveryapp.data.remote.mapper.OrderMapper.toDomainList
import com.mob10.deliveryapp.data.remote.mapper.OrderMapper.toDomainReasonList
import com.mob10.deliveryapp.data.util.NetworkResult

/**
 * Nguồn dữ liệu REST duy nhất cho nghiệp vụ tài xế.
 *
 * Repository không biến lỗi mạng/API thành kết quả thành công giả. Nhờ vậy UI phân biệt
 * chính xác Loading/Empty/Error, 401, 403, 409, 423 và lỗi máy chủ. Cache/offline là P1
 * và chỉ nên bổ sung khi có cơ chế lưu đồng bộ rõ ràng.
 */
class ShipperRepository(
    private val driverApi: DriverApiService,
    private val orderApi: OrderApiService
) {
    suspend fun getOpenOrders(): NetworkResult<List<Order>> =
        RemoteDataSource.safeApiCall { driverApi.getOpenOrders() }
            .mapData { it.toDomainList() }

    suspend fun getMyOrders(): NetworkResult<List<Order>> =
        RemoteDataSource.safeApiCall { driverApi.getMyOrders() }
            .mapData { it.toDomainList() }

    suspend fun acceptOrder(orderId: Long): NetworkResult<Order> {
        val result = RemoteDataSource.safeApiCall { driverApi.acceptOrder(orderId) }
        if (result is NetworkResult.Error) {
            return when (result.code) {
                "ORDER_ALREADY_TAKEN" -> result.copy(
                    message = "Đơn hàng #$orderId đã được tài xế khác nhận trước."
                )
                "DRIVER_TEMPORARILY_LOCKED" -> result.copy(
                    message = "Bạn đang bị giới hạn nhận đơn tạm thời. Vui lòng thử lại sau."
                )
                else -> result
            }
        }
        return result.mapData { it.toDomain() }
    }

    suspend fun rejectOrder(
        orderId: Long,
        reasonCode: String,
        note: String? = null
    ): NetworkResult<RejectInfo> =
        RemoteDataSource.safeApiCall {
            driverApi.rejectOrder(
                orderId,
                RejectOrderRequestDto(reasonCode = reasonCode, note = note?.trim()?.ifBlank { null })
            )
        }.mapData { it.toDomain() }

    suspend fun updateOrderStatus(
        orderId: Long,
        newStatus: DeliveryStatus,
        note: String? = null
    ): NetworkResult<Order> =
        RemoteDataSource.safeApiCall {
            driverApi.updateOrderStatus(
                orderId,
                UpdateStatusRequestDto(status = newStatus.name, note = note?.trim()?.ifBlank { null })
            )
        }.mapData { it.toDomain() }

    suspend fun getOrderById(orderId: Long): NetworkResult<Order> =
        RemoteDataSource.safeApiCall { orderApi.getOrderById(orderId) }
            .mapData { it.toDomain() }

    suspend fun getOrderHistory(orderId: Long): NetworkResult<List<StatusHistory>> =
        RemoteDataSource.safeApiCall { orderApi.getOrderHistory(orderId) }
            .mapData { it.toDomainHistoryList() }

    suspend fun getRejectionReasons(): NetworkResult<List<RejectionReason>> =
        RemoteDataSource.safeApiCall { driverApi.getRejectionReasons() }
            .mapData { it.toDomainReasonList() }

    suspend fun getMyStatistics(): NetworkResult<DriverStatistics> =
        RemoteDataSource.safeApiCall { driverApi.getMyStatistics() }
            .mapData { it.toDomain() }

    suspend fun updateAvailability(availability: String): NetworkResult<String> =
        RemoteDataSource.safeApiCall {
            driverApi.updateAvailability(UpdateAvailabilityRequestDto(availability))
        }
}

private fun <T, R> NetworkResult<T>.mapData(transform: (T) -> R): NetworkResult<R> = when (this) {
    is NetworkResult.Success -> try {
        NetworkResult.Success(transform(data))
    } catch (_: RuntimeException) {
        NetworkResult.Error(
            code = "INVALID_SERVER_RESPONSE",
            message = "Dữ liệu máy chủ trả về không đúng định dạng."
        )
    }
    is NetworkResult.Empty -> NetworkResult.Empty
    is NetworkResult.Error -> this
    is NetworkResult.Loading -> NetworkResult.Loading
}
