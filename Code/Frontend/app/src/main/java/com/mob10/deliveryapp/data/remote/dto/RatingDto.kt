package com.mob10.deliveryapp.data.remote.dto

/**
 * DTOs cho Rating API.
 * Map trực tiếp với backend RatingDtos.java.
 *
 * Lưu ý: clientId/driverId trong request chỉ để tương thích Android cũ —
 * server luôn lấy client từ JWT và driver từ đơn hàng.
 */

// ── Request DTO ──────────────────────────────────────────────

data class RatingRequest(
    val deliveryRequestId: Long,
    val clientId: Long,
    val driverId: Long,
    val stars: Int,          // 1..5
    val comment: String? = null
)

// ── Response DTOs ──────────────────────────────────────────────

data class RatingResponse(
    val id: Long,
    val deliveryRequestId: Long,
    val clientId: Long,
    val driverId: Long,
    val stars: Int,
    val comment: String?,
    val createdAt: String
)

/**
 * Tổng quan sao trung bình của tài xế.
 * GET /ratings/drivers/{driverId}/summary
 */
data class DriverRatingSummaryDto(
    val driverId: Long,
    val ratingCount: Long,
    val averageStars: Double
)
