package com.mob10.deliveryserver.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public final class LocationDtos {
    private LocationDtos() {}

    public record AddressSuggestionResponse(
            String placeId,
            String formattedAddress,
            String primaryText,
            String secondaryText,
            String ward,
            String district,
            String province,
            String country,
            BigDecimal latitude,
            BigDecimal longitude) {}

    public record CoordinateInput(
            @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal latitude,
            @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal longitude) {}

    public record RouteEstimateRequest(
            @NotNull @Valid CoordinateInput pickup,
            @NotNull @Valid CoordinateInput delivery,
            @NotNull @DecimalMin("0.01") BigDecimal totalWeightKg,
            boolean fragile,
            boolean express) {}

    public record RouteEstimateResponse(
            BigDecimal distanceKm,
            int estimatedDurationMinutes,
            BigDecimal baseFee,
            BigDecimal distanceFee,
            BigDecimal weightFee,
            BigDecimal serviceFee,
            BigDecimal totalFee) {}
}
