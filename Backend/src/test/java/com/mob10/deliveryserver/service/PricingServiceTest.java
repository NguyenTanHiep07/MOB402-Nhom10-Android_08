package com.mob10.deliveryserver.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PricingServiceTest {
    private final PricingService service = new PricingService();

    @Test
    void quoteUsesServerPricingRule() {
        var quote = service.quote(new BigDecimal("8.40"), new BigDecimal("2.50"), true, false);

        assertEquals(new BigDecimal("15000.00"), quote.baseFee());
        assertEquals(new BigDecimal("42000.00"), quote.distanceFee());
        assertEquals(new BigDecimal("7500.00"), quote.weightFee());
        assertEquals(new BigDecimal("5000.00"), quote.serviceFee());
        assertEquals(new BigDecimal("69500.00"), quote.totalFee());
    }
}
