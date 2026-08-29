package com.mob10.deliveryapp.data.remote.dto

/**
 * Request gửi lên backend khi khách đánh giá tài xế.
 * clientId và driverId KHÔNG được nhập tay từ UI:
 * - clientId lấy từ session đăng nhập (AuthViewModel.currentUser.id)
 * - driverId lấy từ deliveryPersonId của chính đơn hàng đó
 * TODO: xác nhận field names chính xác với Thịnh khi có API Contract thật.
 */
data class RatingRequest(
    val deliveryRequestId: Int,
    val clientId: Int,
    val driverId: Int,
    val stars: Int,          // 1..5
    val comment: String? = null
)

data class RatingResponse(
    val id: Int,
    val deliveryRequestId: Int,
    val clientId: Int,
    val driverId: Int,
    val stars: Int,
    val comment: String?,
    val createdAt: String
)