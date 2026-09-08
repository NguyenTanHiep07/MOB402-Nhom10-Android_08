package com.mob10.deliveryapp.data.remote.dto

/**
 * DTOs cho Location/Route API.
 * Map trực tiếp với backend LocationDtos.java.
 *
 * Lưu ý từ API Contract:
 * - Android KHÔNG gọi trực tiếp Photon/OSRM, chỉ gọi qua backend.
 * - Autocomplete: chỉ dành cho CLIENT role, debounce 400-500ms.
 * - Route estimate: dùng preview, server tính lại khi tạo đơn.
 */

// ── Autocomplete ──────────────────────────────────────────────

data class AddressSuggestionResponseDto(
    val placeId: String?,
    val formattedAddress: String,
    val primaryText: String?,
    val secondaryText: String?,
    val ward: String?,
    val district: String?,
    val province: String?,
    val country: String?,
    val latitude: Double,
    val longitude: Double
)

// ── Route Estimate ──────────────────────────────────────────────

data class CoordinateInputDto(
    val latitude: Double,
    val longitude: Double
)

data class RouteEstimateRequestDto(
    val pickup: CoordinateInputDto,
    val delivery: CoordinateInputDto,
    val totalWeightKg: Double,
    val fragile: Boolean = false,
    val express: Boolean = false
)

data class RouteEstimateResponseDto(
    val distanceKm: Double,
    val estimatedDurationMinutes: Int,
    val baseFee: Double,
    val distanceFee: Double,
    val weightFee: Double,
    val serviceFee: Double,
    val totalFee: Double
)
