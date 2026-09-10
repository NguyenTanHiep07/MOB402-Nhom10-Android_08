package com.mob10.deliveryserver.dto;

import com.mob10.deliveryserver.domain.DriverAvailability;
import com.mob10.deliveryserver.domain.Role;
import com.mob10.deliveryserver.dto.OrderDtos.DriverStatisticsResponse;
import java.time.Instant;

public final class AdminDtos {
    private AdminDtos() {}
    public record UserResponse(Long id, String username, String fullName, String phoneNumber, Role role,
                               String licensePlate, DriverAvailability availability, boolean active, Instant createdAt) {}
    public record DriverResponse(UserResponse user, DriverStatisticsResponse statistics) {}
}
