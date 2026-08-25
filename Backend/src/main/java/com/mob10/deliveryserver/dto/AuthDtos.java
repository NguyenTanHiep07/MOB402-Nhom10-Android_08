package com.mob10.deliveryserver.dto;

import com.mob10.deliveryserver.domain.Role;
import jakarta.validation.constraints.NotBlank;

public final class AuthDtos {
    private AuthDtos() {}
    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    public record UserSummary(Long id, String username, String fullName, String phoneNumber, Role role, String licensePlate) {}
    public record LoginResponse(String accessToken, String tokenType, long expiresInMs, UserSummary user) {}
}
