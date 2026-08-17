package com.mob10.deliveryapp

import com.mob10.deliveryapp.data.local.entity.FeeRuleEntity
import com.mob10.deliveryapp.data.repository.CalculatedFeeResult

enum class PackageType(val displayName: String, val fragileFee: Double, val multiplier: Double) {
    STANDARD("Tiêu chuẩn", 0.0, 1.0),
    FRAGILE("Hàng dễ vỡ", 5_000.0, 1.4),
    EXPRESS("Hỏa tốc", 0.0, 1.8)
}

object FeeCalculatorEngine {
    const val DEFAULT_BASE_FEE = 15_000.0
    const val DEFAULT_PRICE_PER_KM = 5_000.0
    const val DEFAULT_PRICE_PER_KG = 3_000.0
    const val DEFAULT_FRAGILE_FEE = 5_000.0

    /**
     * Tính phí chi tiết dựa trên trọng lượng, khoảng cách, loại hàng hoặc FeeRuleEntity tuỳ chỉnh.
     * Bộ quy tắc chuẩn:
     * - Phí cơ bản: 15.000 VNĐ
     * - Đơn giá khoảng cách: 5.000 VNĐ / km
     * - Đơn giá khối lượng: 3.000 VNĐ / kg
     * - Phụ phí hàng dễ vỡ: 5.000 VNĐ (nếu là hàng dễ vỡ)
     */
    fun calculateDetailedFee(
        weightKg: Double,
        distanceKm: Double,
        isFragile: Boolean = false,
        feeRule: FeeRuleEntity? = null
    ): CalculatedFeeResult {
        if (weightKg <= 0.0 || distanceKm <= 0.0) {
            return CalculatedFeeResult(0.0, 0.0, 0.0, 0.0, 0.0, feeRule?.id)
        }

        val baseFee = feeRule?.baseFee ?: DEFAULT_BASE_FEE
        val pricePerKm = feeRule?.pricePerKm ?: DEFAULT_PRICE_PER_KM
        val pricePerKg = feeRule?.pricePerKg ?: DEFAULT_PRICE_PER_KG
        val fragileFee = feeRule?.fragileFee ?: DEFAULT_FRAGILE_FEE

        val distanceFee = distanceKm * pricePerKm
        val weightFee = weightKg * pricePerKg
        val fragileCharge = if (isFragile) fragileFee else 0.0
        val totalCost = baseFee + distanceFee + weightFee + fragileCharge

        return CalculatedFeeResult(
            baseFee = baseFee,
            distanceFee = distanceFee,
            weightFee = weightFee,
            fragileCharge = fragileCharge,
            totalCost = totalCost,
            appliedRuleId = feeRule?.id
        )
    }

    fun calculateFee(
        weightKg: Double,
        distanceKm: Double,
        packageType: PackageType = PackageType.STANDARD
    ): Double {
        return calculateDetailedFee(
            weightKg = weightKg,
            distanceKm = distanceKm,
            isFragile = packageType == PackageType.FRAGILE
        ).totalCost
    }

    // Validation
    fun isValidPhone(phone: String): Boolean {
        return phone.isNotBlank() && phone.trim().matches(Regex("^(0|\\+84)[3|5|7|8|9][0-9]{8}$"))
    }

    fun isValidWeight(weight: Double?): Boolean {
        return weight != null && weight > 0.0 && weight <= 500.0
    }

    fun isValidDistance(distance: Double?): Boolean {
        return distance != null && distance > 0.0 && distance <= 2000.0
    }

    fun isValidName(name: String): Boolean {
        return name.isNotBlank() && name.trim().length in 2..50
    }

    fun isValidAddress(address: String): Boolean {
        return address.isNotBlank() && address.trim().length in 5..255
    }
}