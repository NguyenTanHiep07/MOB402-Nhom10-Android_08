package com.mob10.deliveryapp.data.remote.mapper

import com.mob10.deliveryapp.data.local.entity.DeliveryRequestEntity
import com.mob10.deliveryapp.data.local.entity.PackageEntity
import com.mob10.deliveryapp.data.model.*
import com.mob10.deliveryapp.data.remote.dto.*

/**
 * Mapper: chuyển đổi DTO (network layer) → Domain Model (UI layer).
 *
 * Quy tắc:
 * - Null-safe: mọi field nullable đều có default value.
 * - Status string phải khớp DeliveryStatus trong API Contract; mã lạ được báo là response lỗi.
 * - Số tiền server trả BigDecimal nhưng Gson parse thành Double — đủ chính xác cho hiển thị.
 */
object OrderMapper {

    fun OrderResponseDto.toDomain(): Order = Order(
        id = id,
        client = client?.toDomain(),
        deliveryPerson = deliveryPerson?.toDomain(),
        pickupAddress = pickupAddress,
        deliveryAddress = deliveryAddress,
        pickupLatitude = pickupLatitude,
        pickupLongitude = pickupLongitude,
        deliveryLatitude = deliveryLatitude,
        deliveryLongitude = deliveryLongitude,
        senderName = senderName,
        senderPhone = senderPhone,
        recipientName = recipientName,
        recipientPhone = recipientPhone,
        distanceKm = distanceKm ?: 0.0,
        baseFee = baseFee ?: 0.0,
        distanceFee = distanceFee ?: 0.0,
        weightFee = weightFee ?: 0.0,
        fragileCharge = fragileCharge ?: 0.0,
        totalCost = totalCost ?: 0.0,
        status = parseStatus(status),
        scheduledPickupTime = scheduledPickupTime,
        actualDeliveryTime = actualDeliveryTime,
        note = note,
        createdAt = createdAt,
        updatedAt = updatedAt,
        packages = packages?.map { it.toDomain() } ?: emptyList()
    )

    fun PersonResponseDto.toDomain(): Person = Person(
        id = id,
        fullName = fullName ?: "Không rõ",
        phoneNumber = phoneNumber,
        licensePlate = licensePlate
    )

    fun PackageResponseDto.toDomain(): OrderPackage = OrderPackage(
        id = id,
        name = name,
        packageType = packageType,
        weightKg = weightKg,
        quantity = quantity,
        notes = notes,
        isFragile = fragile,
        isExpress = express
    )

    fun HistoryResponseDto.toDomain(): StatusHistory = StatusHistory(
        id = id,
        fromStatus = fromStatus?.let { parseStatus(it) },
        toStatus = toStatus?.let { parseStatus(it) },
        updatedBy = updatedBy,
        updatedByName = updatedByName,
        timestamp = timestamp,
        note = note
    )

    fun DriverStatisticsResponseDto.toDomain(): DriverStatistics = DriverStatistics(
        driverId = driverId,
        totalAccepted = totalAccepted,
        totalRejected = totalRejected,
        penalizedRejections = penalizedRejections,
        reliabilityScore = reliabilityScore,
        lockedUntil = lockedUntil,
        isLocked = locked,
        isWarning = warning,
        availability = availability
    )

    fun RejectionReasonResponseDto.toDomain(): RejectionReason = RejectionReason(
        code = code,
        label = label,
        isValid = valid,
        penaltyPoints = penaltyPoints,
        requiresNote = requiresNote
    )

    fun RejectResultDto.toDomain(): RejectInfo = RejectInfo(
        message = message,
        penaltyApplied = penaltyApplied,
        statistics = statistics?.toDomain()
    )

    // ── List extensions ──

    fun List<OrderResponseDto>.toDomainList(): List<Order> = map { it.toDomain() }
    fun List<HistoryResponseDto>.toDomainHistoryList(): List<StatusHistory> = map { it.toDomain() }
    fun List<RejectionReasonResponseDto>.toDomainReasonList(): List<RejectionReason> = map { it.toDomain() }

    fun DeliveryRequestEntity.toDomain(packages: List<PackageEntity> = emptyList()): Order = Order(
        id = id.toLong(),
        client = Person(
            id = clientId.toLong(),
            fullName = senderName.ifEmpty { "Khách hàng #$clientId" },
            phoneNumber = senderPhone,
            licensePlate = null
        ),
        deliveryPerson = deliveryPersonId?.let { delId ->
            Person(id = delId.toLong(), fullName = "Tài xế #$delId", phoneNumber = null, licensePlate = null)
        },
        pickupAddress = pickupAddress,
        deliveryAddress = deliveryAddress,
        pickupLatitude = null,
        pickupLongitude = null,
        deliveryLatitude = null,
        deliveryLongitude = null,
        senderName = senderName,
        senderPhone = senderPhone,
        recipientName = recipientName,
        recipientPhone = recipientPhone,
        distanceKm = distanceKm,
        baseFee = baseFee,
        distanceFee = distanceFee,
        weightFee = weightFee,
        fragileCharge = fragileCharge,
        totalCost = totalCost,
        status = status,
        scheduledPickupTime = scheduledPickupTime?.toString(),
        actualDeliveryTime = actualDeliveryTime?.toString(),
        note = note,
        createdAt = createdAt.toString(),
        updatedAt = null,
        packages = packages.map { pkg ->
            OrderPackage(
                id = pkg.id.toLong(),
                name = pkg.name,
                packageType = pkg.packageType,
                weightKg = pkg.weightKg,
                quantity = pkg.quantity,
                notes = pkg.notes,
                isFragile = pkg.isFragile,
                isExpress = false
            )
        }
    )

    // ── Helper ──

    private fun parseStatus(status: String): DeliveryStatus = DeliveryStatus.valueOf(status)
}
