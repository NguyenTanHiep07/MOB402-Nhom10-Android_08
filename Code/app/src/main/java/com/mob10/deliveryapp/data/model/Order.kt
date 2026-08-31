package com.mob10.deliveryapp.data.model

/**
 * Domain models tách biệt khỏi Room Entity và DTO.
 * UI layer sử dụng các model này, không import DTO hay Entity trực tiếp.
 */

data class Order(
    val id: Long,
    val client: Person?,
    val deliveryPerson: Person?,
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
    val distanceKm: Double,
    val baseFee: Double,
    val distanceFee: Double,
    val weightFee: Double,
    val fragileCharge: Double,
    val totalCost: Double,
    val status: DeliveryStatus,
    val scheduledPickupTime: String?,
    val actualDeliveryTime: String?,
    val note: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val packages: List<OrderPackage>
)

data class Person(
    val id: Long,
    val fullName: String,
    val phoneNumber: String?,
    val licensePlate: String?
)

data class OrderPackage(
    val id: Long,
    val name: String,
    val packageType: String?,
    val weightKg: Double,
    val quantity: Int,
    val notes: String?,
    val isFragile: Boolean,
    val isExpress: Boolean
)

data class StatusHistory(
    val id: Long,
    val fromStatus: DeliveryStatus?,
    val toStatus: DeliveryStatus?,
    val updatedBy: Long?,
    val updatedByName: String?,
    val timestamp: String?,
    val note: String?
)

data class DriverStatistics(
    val driverId: Long,
    val totalAccepted: Int,
    val totalRejected: Int,
    val penalizedRejections: Int,
    val reliabilityScore: Double,
    val lockedUntil: String?,
    val isLocked: Boolean,
    val isWarning: Boolean
)

data class RejectionReason(
    val code: String,
    val label: String,
    val isValid: Boolean,
    val penaltyPoints: Int,
    val requiresNote: Boolean
)

data class RejectInfo(
    val message: String,
    val penaltyApplied: Boolean,
    val statistics: DriverStatistics?
)
