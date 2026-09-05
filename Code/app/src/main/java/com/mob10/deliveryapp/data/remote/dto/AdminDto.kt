package com.mob10.deliveryapp.data.remote.dto

/**
 * DTOs cho Admin API.
 * Map trực tiếp với backend AdminDtos.java.
 */

// ── Response DTOs ──────────────────────────────────────────────

/**
 * Thông tin tài khoản — trả về bởi GET /admin/users.
 *
 * @param role "CLIENT", "DELIVERY", "ADMIN"
 * @param availability "AVAILABLE", "BUSY", "OFFLINE" (chỉ DELIVERY có giá trị)
 */
data class AdminUserResponseDto(
    val id: Long,
    val username: String,
    val fullName: String?,
    val phoneNumber: String?,
    val role: String,
    val licensePlate: String?,
    val availability: String?,
    val active: Boolean,
    val createdAt: String?
)

/**
 * Thông tin tài xế kèm thống kê — trả về bởi GET /admin/drivers và /admin/drivers/alerts.
 */
data class AdminDriverResponseDto(
    val user: AdminUserResponseDto,
    val statistics: DriverStatisticsResponseDto?
)
