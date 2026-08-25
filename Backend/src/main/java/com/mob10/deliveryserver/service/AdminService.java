package com.mob10.deliveryserver.service;

import com.mob10.deliveryserver.domain.*;
import com.mob10.deliveryserver.dto.AdminDtos.*;
import com.mob10.deliveryserver.dto.OrderDtos.*;
import com.mob10.deliveryserver.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class AdminService {
    private final UserRepository users;
    private final DeliveryRequestRepository orders;
    private final DriverStatisticsRepository statistics;
    private final DtoMapper mapper;
    private final BigDecimal alertThreshold;

    public AdminService(UserRepository users, DeliveryRequestRepository orders,
                        DriverStatisticsRepository statistics, DtoMapper mapper,
                        @Value("${app.reliability.alert-score-threshold}") BigDecimal alertThreshold) {
        this.users = users; this.orders = orders; this.statistics = statistics;
        this.mapper = mapper; this.alertThreshold = alertThreshold;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> allUsers() {
        return users.findAll().stream().map(this::toUser).toList();
    }

    @Transactional(readOnly = true)
    public List<DriverResponse> drivers() {
        return users.findAllByRoleOrderByIdAsc(Role.DELIVERY).stream().map(driver ->
                new DriverResponse(toUser(driver), mapper.toStatistics(statistics.findById(driver.getId())
                        .orElseGet(() -> new DriverStatistics(driver))))).toList();
    }

    @Transactional(readOnly = true)
    public List<DriverResponse> alerts() {
        return statistics.findAllByReliabilityScoreLessThanOrderByReliabilityScoreAsc(alertThreshold).stream()
                .map(stats -> new DriverResponse(toUser(users.getReferenceById(stats.getDriverId())), mapper.toStatistics(stats)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> allOrders() {
        return orders.findAllDetailed().stream().map(mapper::toOrderResponse).toList();
    }

    private UserResponse toUser(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getFullName(), user.getPhoneNumber(),
                user.getRole(), user.getLicensePlate(), user.getDriverAvailability(), user.isActive(), user.getCreatedAt());
    }
}
