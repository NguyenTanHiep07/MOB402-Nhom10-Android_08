package com.mob10.deliveryapp

// Phân loại loại hàng hóa và phụ phí dịch vụ tương ứng
enum class PackageType(val displayName: String, val serviceFee: Long) {
    STANDARD("Tiêu chuẩn", 0),
    FRAGILE("Hàng dễ vỡ", 5_000),
    EXPRESS("Hỏa tốc", 10_000)
}

// Bảng chi tiết cấu thành chi phí giao hàng
data class FeeQuote(
    val baseFee: Long = 0,
    val distanceFee: Long = 0,
    val weightFee: Long = 0,
    val serviceFee: Long = 0
) {
    val totalFee: Long get() = baseFee + distanceFee + weightFee + serviceFee
}

// Bộ máy tính toán cước phí vận chuyển
object FeeCalculatorEngine {
    // Đồng bộ với FeeRule mặc định được seed trong Room.
    private const val BASE_FEE = 15_000L
    private const val PRICE_PER_KM = 5_000L
    private const val PRICE_PER_KG = 3_000L

    // Tính toán chi tiết các thành phần cước phí dựa trên khối lượng, quãng đường và loại hàng
    fun quote(weightKg: Double, distanceKm: Double, packageType: PackageType): FeeQuote {
        if (!weightKg.isFinite() || !distanceKm.isFinite() || weightKg <= 0 || distanceKm <= 0) {
            return FeeQuote()
        }
        return FeeQuote(
            baseFee = BASE_FEE,
            distanceFee = (distanceKm * PRICE_PER_KM).toLong(),
            weightFee = (weightKg * PRICE_PER_KG).toLong(),
            serviceFee = packageType.serviceFee
        )
    }

    // Trả về tổng cước phí giao hàng (Double)
    fun calculateFee(weightKg: Double, distanceKm: Double, packageType: PackageType): Double =
        quote(weightKg, distanceKm, packageType).totalFee.toDouble()

    // Kiểm tra định dạng số điện thoại hợp lệ
    fun isValidPhone(phone: String): Boolean = phone.matches(Regex("^(0\\d{9}|\\+84\\d{9})$"))
}
