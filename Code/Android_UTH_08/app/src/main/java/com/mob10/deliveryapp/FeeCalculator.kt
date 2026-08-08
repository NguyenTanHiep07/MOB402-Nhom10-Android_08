package com.mob10.deliveryapp

object FeeCalculator {
    private const val BASE_FEE = 10000.0   // Phí cơ bản 10.000đ
    private const val FEE_PER_KM = 5000.0   // 5.000đ/km
    private const val FEE_PER_KG = 3000.0   // 3.000đ/kg

    fun calculateFee(distanceKm: Double, weightKg: Double): Double {
        if (distanceKm <= 0 || weightKg <= 0) return 0.0
        return BASE_FEE + (distanceKm * FEE_PER_KM) + (weightKg * FEE_PER_KG)
    }
}