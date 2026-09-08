package com.mob10.deliveryserver.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

public final class RatingDtos {
    private RatingDtos() {}

    public record CreateRatingRequest(
            @NotNull Long deliveryRequestId,
            Long clientId,
            Long driverId,
            @NotNull @Min(1) @Max(5) Integer stars,
            @Size(max = 1000) String comment) {}

    public record RatingResponse(
            Long id,
            Long deliveryRequestId,
            Long clientId,
            Long driverId,
            int stars,
            String comment,
            Instant createdAt) {}

    public record DriverRatingSummary(
            Long driverId,
            long ratingCount,
            BigDecimal averageStars) {}
}
