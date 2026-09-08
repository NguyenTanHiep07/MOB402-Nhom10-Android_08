package com.mob10.deliveryserver.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PricingService {
    private static final BigDecimal BASE_FEE = new BigDecimal("15000");
    private static final BigDecimal PRICE_PER_KM = new BigDecimal("5000");
    private static final BigDecimal PRICE_PER_KG = new BigDecimal("3000");
    private static final BigDecimal FRAGILE_FEE = new BigDecimal("5000");
    private static final BigDecimal EXPRESS_FEE = new BigDecimal("10000");

    public PricingQuote quote(BigDecimal distanceKm, BigDecimal totalWeightKg, boolean fragile, boolean express) {
        BigDecimal normalizedDistance = money(distanceKm);
        BigDecimal normalizedWeight = money(totalWeightKg);
        BigDecimal distanceFee = money(normalizedDistance.multiply(PRICE_PER_KM));
        BigDecimal weightFee = money(normalizedWeight.multiply(PRICE_PER_KG));
        BigDecimal serviceFee = (fragile ? FRAGILE_FEE : BigDecimal.ZERO)
                .add(express ? EXPRESS_FEE : BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = BASE_FEE.add(distanceFee).add(weightFee).add(serviceFee)
                .setScale(2, RoundingMode.HALF_UP);
        if (total.compareTo(new BigDecimal("999999999999.99")) > 0) {
            throw new com.mob10.deliveryserver.exception.ApiException(org.springframework.http.HttpStatus.BAD_REQUEST,
                    "PACKAGE_DATA_TOO_LARGE", "Tổng khối lượng hoặc số lượng kiện hàng vượt giới hạn xử lý");
        }
        return new PricingQuote(BASE_FEE.setScale(2), distanceFee, weightFee, serviceFee, total);
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public record PricingQuote(
            BigDecimal baseFee,
            BigDecimal distanceFee,
            BigDecimal weightFee,
            BigDecimal serviceFee,
            BigDecimal totalFee) {}
}
