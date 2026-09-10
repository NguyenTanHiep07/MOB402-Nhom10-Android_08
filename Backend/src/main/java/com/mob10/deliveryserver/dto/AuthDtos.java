package com.mob10.deliveryserver.dto;

import com.mob10.deliveryserver.domain.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {}
    public record LoginRequest(@NotBlank String phoneNumber, @NotBlank String password) {}
    public record RegisterRequest(
            @NotBlank @Size(min = 3, max = 50) String username,
            @NotBlank @Size(min = 6, max = 100) String password,
            @NotBlank @Size(min = 2, max = 100) String fullName,
            @NotBlank @Pattern(regexp = "^0\\d{8,10}$", message = "Số điện thoại không hợp lệ") String phoneNumber
    ) {}
    public record UserSummary(Long id, String username, String fullName, String phoneNumber, Role role, String licensePlate) {}
    public record LoginResponse(String accessToken, String tokenType, long expiresInMs, UserSummary user) {}
    public record MessageResponse(String message) {}
}
