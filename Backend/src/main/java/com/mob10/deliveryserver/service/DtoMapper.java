package com.mob10.deliveryserver.service;

import com.mob10.deliveryserver.domain.*;
import com.mob10.deliveryserver.dto.AuthDtos.UserSummary;
import com.mob10.deliveryserver.dto.OrderDtos.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DtoMapper {
    private final int alertScoreThreshold;

    public DtoMapper(@Value("${app.reliability.alert-score-threshold}") int alertScoreThreshold) {
        this.alertScoreThreshold = alertScoreThreshold;
    }

    public UserSummary toUserSummary(User user) {
        return new UserSummary(user.getId(), user.getUsername(), user.getFullName(), user.getPhoneNumber(),
                user.getRole(), user.getLicensePlate());
    }

    public OrderResponse toOrderResponse(DeliveryRequest order) {
        return new OrderResponse(order.getId(), toPerson(order.getClient()), toPerson(order.getDeliveryPerson()),
                order.getPickupAddress(), order.getDeliveryAddress(), order.getPickupLatitude(),
                order.getPickupLongitude(), order.getDeliveryLatitude(), order.getDeliveryLongitude(),
                order.getSenderName(), order.getSenderPhone(),
                order.getRecipientName(), order.getRecipientPhone(), order.getDistanceKm(), order.getBaseFee(),
                order.getDistanceFee(), order.getWeightFee(), order.getFragileCharge(), order.getTotalCost(),
                order.getStatus(), order.getScheduledPickupTime(), order.getActualDeliveryTime(), order.getNote(),
                order.getCreatedAt(), order.getUpdatedAt(), order.getPackages().stream().map(this::toPackage).toList());
    }

    public HistoryResponse toHistoryResponse(StatusHistory history) {
        User updater = history.getUpdatedBy();
        return new HistoryResponse(history.getId(), history.getFromStatus(), history.getToStatus(),
                updater == null ? null : updater.getId(), updater == null ? null : updater.getFullName(),
                history.getTimestamp(), history.getNote());
    }

    public DriverStatisticsResponse toStatistics(DriverStatistics stats) {
        return new DriverStatisticsResponse(stats.getDriverId(), stats.getTotalAccepted(), stats.getTotalRejected(),
                stats.getPenalizedRejections(), stats.getReliabilityScore(), stats.getLockedUntil(), stats.isLocked(),
                stats.getReliabilityScore().doubleValue() < alertScoreThreshold, stats.getAvailability());
    }

    private PersonResponse toPerson(User user) {
        return user == null ? null : new PersonResponse(user.getId(), user.getFullName(), user.getPhoneNumber(), user.getLicensePlate());
    }

    private PackageResponse toPackage(PackageItem item) {
        return new PackageResponse(item.getId(), item.getName(), item.getPackageType(), item.getWeightKg(),
                item.getQuantity(), item.getNotes(), item.isFragile(), item.isExpress());
    }
}
