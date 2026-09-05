package com.mob10.deliveryserver.dto;

import com.mob10.deliveryserver.domain.DeliveryStatus;
import com.mob10.deliveryserver.domain.DriverAvailability;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class OrderDtos {
    private OrderDtos() {}

    public record PackageInput(
            @NotBlank @Size(max = 150) String name,
            @Size(max = 50) String packageType,
            @NotNull @DecimalMin("0.01") @Digits(integer = 8, fraction = 2) BigDecimal weightKg,
            @Min(1) int quantity,
            @Size(max = 500) String notes,
            boolean fragile,
            boolean express) {}

    public record CreateOrderRequest(
            @NotBlank @Size(max = 500) String pickupAddress,
            @NotBlank @Size(max = 500) String deliveryAddress,
            @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal pickupLatitude,
            @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal pickupLongitude,
            @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal deliveryLatitude,
            @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal deliveryLongitude,
            @NotBlank @Size(max = 120) String senderName,
            @NotBlank @Pattern(regexp = "^(0[0-9]{9}|\\+84[0-9]{9})$") String senderPhone,
            @NotBlank @Size(max = 120) String recipientName,
            @NotBlank @Pattern(regexp = "^(0[0-9]{9}|\\+84[0-9]{9})$") String recipientPhone,
            @NotNull @DecimalMin("0.01") BigDecimal distanceKm,
            @NotEmpty List<@Valid PackageInput> packages,
            Instant scheduledPickupTime,
            @Size(max = 1000) String note) {}

    public record PackageResponse(Long id, String name, String packageType, BigDecimal weightKg,
                                  int quantity, String notes, boolean fragile, boolean express) {}
    public record PersonResponse(Long id, String fullName, String phoneNumber, String licensePlate) {}
    public record OrderResponse(Long id, PersonResponse client, PersonResponse deliveryPerson,
                                String pickupAddress, String deliveryAddress,
                                BigDecimal pickupLatitude, BigDecimal pickupLongitude,
                                BigDecimal deliveryLatitude, BigDecimal deliveryLongitude,
                                String senderName, String senderPhone,
                                String recipientName, String recipientPhone, BigDecimal distanceKm,
                                BigDecimal baseFee, BigDecimal distanceFee, BigDecimal weightFee,
                                BigDecimal fragileCharge, BigDecimal totalCost, DeliveryStatus status,
                                Instant scheduledPickupTime, Instant actualDeliveryTime, String note,
                                Instant createdAt, Instant updatedAt, List<PackageResponse> packages) {}
    public record UpdateStatusRequest(@NotNull DeliveryStatus status, @Size(max = 500) String note) {}
    public record UpdateAvailabilityRequest(@NotNull DriverAvailability availability) {}
    public record RejectOrderRequest(@NotBlank String reasonCode, @Size(max = 500) String note) {}
    public record HistoryResponse(Long id, DeliveryStatus fromStatus, DeliveryStatus toStatus,
                                  Long updatedBy, String updatedByName, Instant timestamp, String note) {}
    public record RejectionReasonResponse(String code, String label, boolean valid,
                                          int penaltyPoints, boolean requiresNote) {}
    public record DriverStatisticsResponse(Long driverId, int totalAccepted, int totalRejected,
                                           int penalizedRejections, BigDecimal reliabilityScore,
                                           Instant lockedUntil, boolean locked, boolean warning, DriverAvailability availability) {}
    public record RejectResult(String message, boolean penaltyApplied, DriverStatisticsResponse statistics) {}
}
