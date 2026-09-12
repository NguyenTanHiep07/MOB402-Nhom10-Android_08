package com.mob10.deliveryapp.data.remote.dto

/**
 * DTOs cho Order API.
 * Map trực tiếp với backend OrderDtos.java — OrderResponse, PackageResponse, PersonResponse,
 * CreateOrderRequest, PackageInput, HistoryResponse.
 */

// ── Response DTOs ──────────────────────────────────────────────

data class PersonResponseDto(
    val id: Long,
    val fullName: String?,
    val phoneNumber: String?,
    val licensePlate: String?
)

data class PackageResponseDto(
    val id: Long,
    val name: String,
    val packageType: String?,
    val weightKg: Double,
    val quantity: Int,
    val notes: String?,
    val fragile: Boolean,
    val express: Boolean
)

data class OrderResponseDto(
    val id: Long,
    val client: PersonResponseDto?,
    val deliveryPerson: PersonResponseDto?,
    val pickupAddress: String,
    val deliveryAddress: String,
    val pickupLatitude: Double?,
    val pickupLongitude: Double?,
    val deliveryLatitude: Double?,
    val deliveryLongitude: Double?,
    val senderName: String,
    val senderPhone: String,
    val recipientName: String,
    val recipientPhone: String,
    val distanceKm: Double?,
    val baseFee: Double?,
    val distanceFee: Double?,
    val weightFee: Double?,
    val fragileCharge: Double?,
    val totalCost: Double?,
    val status: String,          // "CHO_TIEP_NHAN", "DA_CHAP_NHAN", ...
    val scheduledPickupTime: String?,
    val actualDeliveryTime: String?,
    val note: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val packages: List<PackageResponseDto>?
)

data class HistoryResponseDto(
    val id: Long,
    val fromStatus: String?,
    val toStatus: String?,
    val updatedBy: Long?,
    val updatedByName: String?,
    val timestamp: String?,
    val note: String?
)

// ── Request DTOs ──────────────────────────────────────────────

data class PackageInputDto(
    val name: String,
    val packageType: String?,
    val weightKg: Double,
    val quantity: Int = 1,
    val notes: String? = null,
    val fragile: Boolean = false,
    val express: Boolean = false
)

data class CreateOrderRequestDto(
    val pickupAddress: String,
    val deliveryAddress: String,
    val pickupLatitude: Double,
    val pickupLongitude: Double,
    val deliveryLatitude: Double,
    val deliveryLongitude: Double,
    val senderName: String,
    val senderPhone: String,
    val recipientName: String,
    val recipientPhone: String,
    val distanceKm: Double,
    val packages: List<PackageInputDto>,
    val scheduledPickupTime: String? = null,
    val note: String? = null
)
