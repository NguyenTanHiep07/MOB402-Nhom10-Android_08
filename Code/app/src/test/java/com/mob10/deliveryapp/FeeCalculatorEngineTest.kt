package com.mob10.deliveryapp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FeeCalculatorEngineTest {

    @Test
    fun test_TC_FEE_01_StandardPackage_5km_2kg() {
        val result = FeeCalculatorEngine.calculateDetailedFee(
            weightKg = 2.0,
            distanceKm = 5.0,
            isFragile = false
        )
        assertEquals(15_000.0, result.baseFee, 0.01)
        assertEquals(25_000.0, result.distanceFee, 0.01)
        assertEquals(6_000.0, result.weightFee, 0.01)
        assertEquals(0.0, result.fragileCharge, 0.01)
        assertEquals(46_000.0, result.totalCost, 0.01)
    }

    @Test
    fun test_TC_FEE_02_FragilePackage_10km_1_5kg() {
        val result = FeeCalculatorEngine.calculateDetailedFee(
            weightKg = 1.5,
            distanceKm = 10.0,
            isFragile = true
        )
        assertEquals(15_000.0, result.baseFee, 0.01)
        assertEquals(50_000.0, result.distanceFee, 0.01)
        assertEquals(4_500.0, result.weightFee, 0.01)
        assertEquals(5_000.0, result.fragileCharge, 0.01)
        assertEquals(74_500.0, result.totalCost, 0.01)
    }

    @Test
    fun test_TC_FEE_03_HeavyPackage_12km_10kg() {
        val result = FeeCalculatorEngine.calculateDetailedFee(
            weightKg = 10.0,
            distanceKm = 12.0,
            isFragile = false
        )
        assertEquals(15_000.0, result.baseFee, 0.01)
        assertEquals(60_000.0, result.distanceFee, 0.01)
        assertEquals(30_000.0, result.weightFee, 0.01)
        assertEquals(0.0, result.fragileCharge, 0.01)
        assertEquals(105_000.0, result.totalCost, 0.01)
    }

    @Test
    fun test_TC_FEE_04_MinimumPackage_1km_1kg() {
        val result = FeeCalculatorEngine.calculateDetailedFee(
            weightKg = 1.0,
            distanceKm = 1.0,
            isFragile = false
        )
        assertEquals(15_000.0, result.baseFee, 0.01)
        assertEquals(5_000.0, result.distanceFee, 0.01)
        assertEquals(3_000.0, result.weightFee, 0.01)
        assertEquals(0.0, result.fragileCharge, 0.01)
        assertEquals(23_000.0, result.totalCost, 0.01)
    }

    @Test
    fun test_PhoneValidation() {
        assertTrue(FeeCalculatorEngine.isValidPhone("0901234567"))
        assertTrue(FeeCalculatorEngine.isValidPhone("0987654321"))
        assertTrue(FeeCalculatorEngine.isValidPhone("+84901234567"))
        assertTrue(FeeCalculatorEngine.isValidPhone("0323456789"))

        assertFalse(FeeCalculatorEngine.isValidPhone(""))
        assertFalse(FeeCalculatorEngine.isValidPhone("123456"))
        assertFalse(FeeCalculatorEngine.isValidPhone("01234567890"))
        assertFalse(FeeCalculatorEngine.isValidPhone("abcdefghij"))
    }

    @Test
    fun test_WeightAndDistanceValidation() {
        assertTrue(FeeCalculatorEngine.isValidWeight(1.0))
        assertTrue(FeeCalculatorEngine.isValidWeight(50.0))
        assertFalse(FeeCalculatorEngine.isValidWeight(0.0))
        assertFalse(FeeCalculatorEngine.isValidWeight(-2.0))
        assertFalse(FeeCalculatorEngine.isValidWeight(600.0))

        assertTrue(FeeCalculatorEngine.isValidDistance(5.5))
        assertFalse(FeeCalculatorEngine.isValidDistance(0.0))
        assertFalse(FeeCalculatorEngine.isValidDistance(-1.0))
    }
}
