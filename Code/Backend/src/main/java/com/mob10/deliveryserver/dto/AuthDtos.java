package com.mob10.deliveryserver.dto;

import com.mob10.deliveryserver.domain.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {}
    public record LoginRequest(@NotBlank String phoneNumber, @NotBlank String password) {}
    public record RegisterRequest(
            @NotBlank @Pattern(regexp = "^(0\\d{9}|\\+84\\d{9})$") String phoneNumber,
            @NotBlank @Size(min = 6, max = 128) String password,
            @NotBlank @Size(max = 100) String fullName) {}
    public record RegisterResponse(Long id, String phoneNumber, String fullName, Role role) {}
    public record UserSummary(Long id, String username, String fullName, String phoneNumber, Role role, String licensePlate) {}
    public record LoginResponse(String accessToken, String tokenType, long expiresInMs, UserSummary user) {}
}
