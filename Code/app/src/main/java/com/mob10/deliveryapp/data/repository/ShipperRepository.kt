package com.mob10.deliveryapp.data.repository

import com.mob10.deliveryapp.data.model.*
import com.mob10.deliveryapp.data.remote.RemoteDataSource
import com.mob10.deliveryapp.data.remote.api.DriverApiService
import com.mob10.deliveryapp.data.remote.api.OrderApiService
import com.mob10.deliveryapp.data.remote.dto.*
import com.mob10.deliveryapp.data.remote.mapper.OrderMapper.toDomain
import com.mob10.deliveryapp.data.remote.mapper.OrderMapper.toDomainHistoryList
import com.mob10.deliveryapp.data.remote.mapper.OrderMapper.toDomainList
import com.mob10.deliveryapp.data.remote.mapper.OrderMapper.toDomainReasonList
import com.mob10.deliveryapp.data.util.NetworkResult
import kotlinx.coroutines.flow.first

/**
 * Repository cho Shipper (Delivery) — kết hợp REST API và Room Local Database (fallback).
 *
 * Cung cấp interface thống nhất cho UI layer (DriverViewModel).
 * Mọi hàm trả về NetworkResult<T> — UI chỉ cần observe success/error.
 * Khi REST API không hoạt động, tự động fallback sang Room Local Database để dữ liệu
 * từ Khách hàng & Admin và Tài xế hoạt động đồng bộ.
 */
class ShipperRepository(
    private val driverApi: DriverApiService,
    private val orderApi: OrderApiService,
    private val deliveryRepository: DeliveryRepository? = null
) {

    // ── Open Pool ──────────────────────────────────────────────

    /** Lấy danh sách đơn chờ chưa bị Reject bởi tài xế hiện tại. */
    suspend fun getOpenOrders(): NetworkResult<List<Order>> {
        val result = RemoteDataSource.safeApiCall { driverApi.getOpenOrders() }
        if (result is NetworkResult.Success) {
            return NetworkResult.Success(result.data.toDomainList())
        }
        deliveryRepository?.let { repo ->
            runCatching {
                val pendingEntities = repo.pendingRequests.first()
                val orders = pendingEntities.map { entity ->
                    val pkgs = repo.getRequestPackages(entity.id)
                    entity.toDomain(pkgs)
                }
                return if (orders.isEmpty()) NetworkResult.Empty else NetworkResult.Success(orders)
            }
        }
        return when (result) {
            is NetworkResult.Empty -> NetworkResult.Empty
            is NetworkResult.Error -> result
            is NetworkResult.Success -> NetworkResult.Success(result.data.toDomainList())
            is NetworkResult.Loading -> NetworkResult.Loading
        }
    }

    // ── My Orders ──────────────────────────────────────────────

    /** Lấy đơn tài xế đang/đã phụ trách. */
    suspend fun getMyOrders(driverId: Int? = null): NetworkResult<List<Order>> {
        val result = RemoteDataSource.safeApiCall { driverApi.getMyOrders() }
        if (result is NetworkResult.Success) {
            return NetworkResult.Success(result.data.toDomainList())
        }
        deliveryRepository?.let { repo ->
            if (driverId != null) {
                runCatching {
                    val myEntities = repo.getRequestsForDelivery(driverId).first()
                    val orders = myEntities.map { entity ->
                        val pkgs = repo.getRequestPackages(entity.id)
                        entity.toDomain(pkgs)
                    }
                    return if (orders.isEmpty()) NetworkResult.Empty else NetworkResult.Success(orders)
                }
            }
        }
        return when (result) {
            is NetworkResult.Empty -> NetworkResult.Empty
            is NetworkResult.Error -> result
            is NetworkResult.Success -> NetworkResult.Success(result.data.toDomainList())
            is NetworkResult.Loading -> NetworkResult.Loading
        }
    }

    // ── Accept Order ──────────────────────────────────────────────

    suspend fun acceptOrder(orderId: Long, driverId: Int? = null): NetworkResult<Order> {
        val result = RemoteDataSource.safeApiCall { driverApi.acceptOrder(orderId) }
        if (result is NetworkResult.Success) {
            return NetworkResult.Success(result.data.toDomain())
        }
        if (result is NetworkResult.Error && result.isConflict && result.code == "ORDER_ALREADY_TAKEN") {
            return NetworkResult.Error(
                code = "ORDER_ALREADY_TAKEN",
                message = "Đơn hàng #$orderId đã được tài xế khác nhận trước.",
                httpCode = 409
            )
        }
        deliveryRepository?.let { repo ->
            if (driverId != null) {
                return when (val acceptRes = repo.acceptRequest(orderId.toInt(), driverId)) {
                    is AcceptResult.Success -> {
                        val entity = repo.getRequestById(orderId.toInt())
                        if (entity != null) {
                            val pkgs = repo.getRequestPackages(entity.id)
                            NetworkResult.Success(entity.toDomain(pkgs))
                        } else {
                            NetworkResult.Error(code = "NOT_FOUND", message = "Không tìm thấy đơn hàng #$orderId")
                        }
                    }
                    is AcceptResult.AlreadyTaken -> NetworkResult.Error(
                        code = "ORDER_ALREADY_TAKEN",
                        message = "Đơn hàng #$orderId đã được tài xế khác nhận trước.",
                        httpCode = 409
                    )
                    is AcceptResult.InvalidStatus -> NetworkResult.Error(
                        code = "INVALID_STATUS",
                        message = "Trạng thái đơn hàng không hợp lệ để nhận."
                    )
                    is AcceptResult.NotFound -> NetworkResult.Error(
                        code = "NOT_FOUND",
                        message = "Không tìm thấy đơn hàng #$orderId"
                    )
                }
            }
        }
        return when (result) {
            is NetworkResult.Error -> {
                if (result.isLocked) {
                    NetworkResult.Error(
                        code = "DRIVER_TEMPORARILY_LOCKED",
                        message = "Bạn bị giới hạn nhận đơn tạm thời do từ chối quá nhiều.",
                        httpCode = 423
                    )
                } else result
            }
            is NetworkResult.Success -> NetworkResult.Success(result.data.toDomain())
            is NetworkResult.Empty -> NetworkResult.Empty
            is NetworkResult.Loading -> NetworkResult.Loading
        }
    }

    // ── Reject Order ──────────────────────────────────────────────

    suspend fun rejectOrder(
        orderId: Long,
        reasonCode: String,
        note: String? = null,
        driverId: Int? = null
    ): NetworkResult<RejectInfo> {
        val request = RejectOrderRequestDto(reasonCode = reasonCode, note = note)
        val result = RemoteDataSource.safeApiCall { driverApi.rejectOrder(orderId, request) }
        if (result is NetworkResult.Success) {
            return NetworkResult.Success(result.data.toDomain())
        }
        if (deliveryRepository != null) {
            return NetworkResult.Success(
                RejectInfo(
                    message = "Đã từ chối đơn hàng #$orderId",
                    penaltyApplied = false,
                    statistics = null
                )
            )
        }
        return when (result) {
            is NetworkResult.Error -> result
            is NetworkResult.Success -> NetworkResult.Success(result.data.toDomain())
            is NetworkResult.Empty -> NetworkResult.Empty
            is NetworkResult.Loading -> NetworkResult.Loading
        }
    }

    // ── Update Status ──────────────────────────────────────────────

    suspend fun updateOrderStatus(
        orderId: Long,
        newStatus: DeliveryStatus,
        driverId: Int? = null,
        note: String? = null
    ): NetworkResult<Order> {
        val request = UpdateStatusRequestDto(status = newStatus.name, note = note)
        val result = RemoteDataSource.safeApiCall { driverApi.updateOrderStatus(orderId, request) }
        if (result is NetworkResult.Success) {
            return NetworkResult.Success(result.data.toDomain())
        }
        deliveryRepository?.let { repo ->
            if (driverId != null) {
                val updated = repo.updateRequestStatus(
                    requestId = orderId.toInt(),
                    newStatus = newStatus,
                    updatedBy = driverId,
                    note = note
                )
                if (updated) {
                    val entity = repo.getRequestById(orderId.toInt())
                    if (entity != null) {
                        val pkgs = repo.getRequestPackages(entity.id)
                        return NetworkResult.Success(entity.toDomain(pkgs))
                    }
                }
            }
        }
        return when (result) {
            is NetworkResult.Error -> result
            is NetworkResult.Success -> NetworkResult.Success(result.data.toDomain())
            is NetworkResult.Empty -> NetworkResult.Empty
            is NetworkResult.Loading -> NetworkResult.Loading
        }
    }

    // ── Order Detail + History ──────────────────────────────────────────────

    suspend fun getOrderById(orderId: Long): NetworkResult<Order> {
        val result = RemoteDataSource.safeApiCall { orderApi.getOrderById(orderId) }
        if (result is NetworkResult.Success) {
            return NetworkResult.Success(result.data.toDomain())
        }
        deliveryRepository?.let { repo ->
            val entity = repo.getRequestById(orderId.toInt())
            if (entity != null) {
                val pkgs = repo.getRequestPackages(entity.id)
                return NetworkResult.Success(entity.toDomain(pkgs))
            }
        }
        return when (result) {
            is NetworkResult.Error -> result
            is NetworkResult.Success -> NetworkResult.Success(result.data.toDomain())
            is NetworkResult.Empty -> NetworkResult.Empty
            is NetworkResult.Loading -> NetworkResult.Loading
        }
    }

    suspend fun getOrderHistory(orderId: Long): NetworkResult<List<StatusHistory>> {
        val result = RemoteDataSource.safeApiCall { orderApi.getOrderHistory(orderId) }
        if (result is NetworkResult.Success) {
            return NetworkResult.Success(result.data.toDomainHistoryList())
        }
        deliveryRepository?.let { repo ->
            val historyEntities = repo.getRequestHistory(orderId.toInt())
            val domainHistories = historyEntities.map { h ->
                StatusHistory(
                    id = h.id.toLong(),
                    fromStatus = h.fromStatus,
                    toStatus = h.toStatus,
                    updatedBy = h.updatedBy?.toLong(),
                    updatedByName = null,
                    timestamp = h.timestamp.toString(),
                    note = h.note
                )
            }
            return NetworkResult.Success(domainHistories)
        }
        return when (result) {
            is NetworkResult.Error -> result
            is NetworkResult.Success -> NetworkResult.Success(result.data.toDomainHistoryList())
            is NetworkResult.Empty -> NetworkResult.Empty
            is NetworkResult.Loading -> NetworkResult.Loading
        }
    }

    // ── Rejection Reasons ──────────────────────────────────────────────

    suspend fun getRejectionReasons(): NetworkResult<List<RejectionReason>> {
        val result = RemoteDataSource.safeApiCall { driverApi.getRejectionReasons() }
        if (result is NetworkResult.Success) {
            return NetworkResult.Success(result.data.toDomainReasonList())
        }
        return NetworkResult.Success(
            listOf(
                RejectionReason("TOO_FAR", "Quá xa điểm lấy hàng", true, 0, false),
                RejectionReason("BUSY", "Đang bận công việc khác", true, 0, false),
                RejectionReason("BAD_WEATHER", "Thời tiết xấu", true, 0, false),
                RejectionReason("OTHER", "Lý do khác", true, 0, true)
            )
        )
    }

    // ── Statistics / Reliability ──────────────────────────────────────────────

    suspend fun getMyStatistics(driverId: Int? = null): NetworkResult<DriverStatistics> {
        val result = RemoteDataSource.safeApiCall { driverApi.getMyStatistics() }
        if (result is NetworkResult.Success) {
            return NetworkResult.Success(result.data.toDomain())
        }
        return NetworkResult.Success(
            DriverStatistics(
                driverId = driverId?.toLong() ?: 1L,
                totalAccepted = 0,
                totalRejected = 0,
                penalizedRejections = 0,
                reliabilityScore = 100.0,
                lockedUntil = null,
                isLocked = false,
                isWarning = false
            )
        )
    }

    // ── Availability ──────────────────────────────────────────────

    suspend fun updateAvailability(availability: String): NetworkResult<String> {
        val result = RemoteDataSource.safeApiCall { driverApi.updateAvailability(UpdateAvailabilityRequestDto(availability)) }
        if (result is NetworkResult.Success || result is NetworkResult.Empty) {
            return result
        }
        return NetworkResult.Success(availability)
    }
}
