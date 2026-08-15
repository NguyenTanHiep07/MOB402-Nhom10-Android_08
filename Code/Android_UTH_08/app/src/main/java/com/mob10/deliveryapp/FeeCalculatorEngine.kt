package com.mob10.deliveryapp

enum class PackageType(val displayName: String, val multiplier: Double) {
    STANDARD("Tiêu chuẩn", 1.0),
    FRAGILE("Hàng dễ vỡ", 1.4),
    EXPRESS("Hỏa tốc", 1.8)
}

object FeeCalculatorEngine {
    private const val BASE_FEE = 15_000.0
    private const val PRICE_PER_KG = 5_000.0
    private const val PRICE_PER_KM = 3_500.0

    fun calculateFee(weightKg: Double, distanceKm: Double, packageType: PackageType): Double {
        if (weightKg <= 0 || distanceKm <= 0) return 0.0
        val baseCalc = BASE_FEE + (weightKg * PRICE_PER_KG) + (distanceKm * PRICE_PER_KM)
        return baseCalc * packageType.multiplier
    }

    fun isValidPhone(phone: String): Boolean {
        return phone.matches(Regex("^(0|\\+84)[3|5|7|8|9][0-9]{8}$"))
    }
}