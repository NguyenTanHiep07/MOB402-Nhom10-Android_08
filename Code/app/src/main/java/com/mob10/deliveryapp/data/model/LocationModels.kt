package com.mob10.deliveryapp.data.model

/**
 * Domain models cho Location/Route — UI layer sử dụng thay vì DTO.
 */

/**
 * Gợi ý địa chỉ từ autocomplete.
 *
 * Android phải lưu [formattedAddress], [latitude], [longitude] của item người dùng đã chọn.
 * Không xem chuỗi người dùng tự gõ nhưng chưa chọn suggestion là địa chỉ đã xác thực.
 */
data class AddressSuggestion(
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

/**
 * Kết quả ước lượng tuyến đường và phí tạm tính.
 * Giá trị chỉ để preview — server tính lại khi tạo đơn.
 */
data class RouteEstimate(
    val distanceKm: Double,
    val estimatedDurationMinutes: Int,
    val baseFee: Double,
    val distanceFee: Double,
    val weightFee: Double,
    val serviceFee: Double,
    val totalFee: Double
)
