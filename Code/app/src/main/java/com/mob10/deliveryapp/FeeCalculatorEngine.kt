package com.mob10.deliveryapp

enum class PackageType(val displayName: String, val serviceFee: Long) {
    STANDARD("Tiêu chuẩn", 0),
    FRAGILE("Hàng dễ vỡ", 5_000),
    EXPRESS("Hỏa tốc", 10_000)
}

data class FeeQuote(
    val baseFee: Long = 0,
    val distanceFee: Long = 0,
    val weightFee: Long = 0,
    val serviceFee: Long = 0
) {
    val totalFee: Long get() = baseFee + distanceFee + weightFee + serviceFee
}

object FeeCalculatorEngine {
    private const val BASE_FEE = 10_000L
    private const val PRICE_PER_KM = 5_000L
    private const val PRICE_PER_KG = 3_000L

    fun quote(weightKg: Double, distanceKm: Double, packageType: PackageType): FeeQuote {
        if (weightKg <= 0 || distanceKm <= 0) return FeeQuote()
        return FeeQuote(
            baseFee = BASE_FEE,
            distanceFee = (distanceKm * PRICE_PER_KM).toLong(),
            weightFee = (weightKg * PRICE_PER_KG).toLong(),
            serviceFee = packageType.serviceFee
        )
    }

    fun calculateFee(weightKg: Double, distanceKm: Double, packageType: PackageType): Double =
        quote(weightKg, distanceKm, packageType).totalFee.toDouble()

    fun isValidPhone(phone: String): Boolean = phone.matches(Regex("^(0\\d{9}|\\+84\\d{9})$"))
}
