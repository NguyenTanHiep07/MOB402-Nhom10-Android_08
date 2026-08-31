package com.mob10.deliveryapp.data.remote.dto

/**
 * DTOs cho Driver/Shipper API.
 * Map trực tiếp với backend OrderDtos.java — phần driver-specific:
 * RejectOrderRequest, UpdateStatusRequest, UpdateAvailabilityRequest,
 * RejectionReasonResponse, DriverStatisticsResponse, RejectResult.
 */

// ── Request DTOs ──────────────────────────────────────────────

data class RejectOrderRequestDto(
    val reasonCode: String,
    val note: String? = null
)

data class UpdateStatusRequestDto(
    val status: String,         // "DA_DEN_NHA_HANG", "DA_LAY_HANG", ...
    val note: String? = null
)

data class UpdateAvailabilityRequestDto(
    val availability: String    // "AVAILABLE", "BUSY", "OFFLINE"
)

// ── Response DTOs ──────────────────────────────────────────────

data class RejectionReasonResponseDto(
    val code: String,
    val label: String,
    val valid: Boolean,
    val penaltyPoints: Int,
    val requiresNote: Boolean
)

data class DriverStatisticsResponseDto(
    val driverId: Long,
    val totalAccepted: Int,
    val totalRejected: Int,
    val penalizedRejections: Int,
    val reliabilityScore: Double,
    val lockedUntil: String?,
    val locked: Boolean,
    val warning: Boolean
)

data class RejectResultDto(
    val message: String,
    val penaltyApplied: Boolean,
    val statistics: DriverStatisticsResponseDto?
)
