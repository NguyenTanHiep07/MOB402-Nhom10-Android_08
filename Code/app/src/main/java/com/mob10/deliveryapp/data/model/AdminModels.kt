package com.mob10.deliveryapp.data.model

/**
 * Domain models cho Admin — UI layer sử dụng thay vì DTO.
 */

/**
 * Thông tin tài khoản (dành cho màn hình quản trị).
 */
data class AdminUser(
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
 * Thông tin tài xế kèm thống kê — dùng cho danh sách tài xế và cảnh báo.
 */
data class AdminDriver(
    val user: AdminUser,
    val statistics: DriverStatistics?
)
