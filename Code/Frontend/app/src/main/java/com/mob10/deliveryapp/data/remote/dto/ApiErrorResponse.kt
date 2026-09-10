package com.mob10.deliveryapp.data.remote.dto

/**
 * Cấu trúc lỗi thống nhất từ backend.
 * Mọi lỗi API đều trả về format này (xem GlobalExceptionHandler trên server).
 *
 * Ví dụ response 409:
 * ```json
 * {
 *   "timestamp": "2026-08-31T02:00:00Z",
 *   "status": 409,
 *   "code": "ORDER_ALREADY_TAKEN",
 *   "message": "Tài xế khác đã nhận trước",
 *   "path": "/api/driver/orders/5/accept",
 *   "fields": {}
 * }
 * ```
 */
data class ApiErrorResponse(
    val timestamp: String? = null,
    val status: Int = 0,
    val code: String? = null,
    val message: String? = null,
    val path: String? = null,
    val fields: Map<String, String>? = null
)
