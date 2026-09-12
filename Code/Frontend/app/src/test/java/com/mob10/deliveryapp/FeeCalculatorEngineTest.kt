package com.mob10.deliveryapp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FeeCalculatorEngineTest {
    @Test
    fun `quote includes fragile service fee`() {
        val quote = FeeCalculatorEngine.quote(weightKg = 2.0, distanceKm = 4.0, packageType = PackageType.FRAGILE)

        assertEquals(15_000, quote.baseFee)
        assertEquals(20_000, quote.distanceFee)
        assertEquals(6_000, quote.weightFee)
        assertEquals(5_000, quote.serviceFee)
        assertEquals(46_000, quote.totalFee)
    }

    @Test
    fun `accepts Vietnamese phone formats used by the app`() {
        assertTrue(FeeCalculatorEngine.isValidPhone("0123456789"))
        assertTrue(FeeCalculatorEngine.isValidPhone("+84123456789"))
        assertFalse(FeeCalculatorEngine.isValidPhone("12345"))
    }
}
