package com.mob10.deliveryserver.dto;

import jakarta.validation.constraints.*;

public final class AccountDtos {
    private AccountDtos() {}
    public record Message(String message) {}
    public record Profile(Long id, String username, String fullName, String phoneNumber,
                          String role, String licensePlate, String email, boolean emailVerified, String avatarBase64) {}
    public record Edit(@NotBlank @Pattern(regexp="[A-Za-z0-9_.-]{3,80}") String username,
                       @NotBlank @Size(max=120) String fullName,
                       @NotBlank @Size(max=20) String phoneNumber,
                       @NotBlank @Size(max=72) String currentPassword,
                       @Size(max=220000) String avatarBase64) {}
    public record Link(@NotBlank @Email @Size(max=254) String email,
                       @NotBlank @Size(max=72) String currentPassword) {}
    public record Verify(@NotNull @Pattern(regexp="[0-9]{6}") String code,
                         @NotBlank @Size(max=72) String currentPassword) {}
    public record Request(@NotBlank @Size(max=20) String phoneNumber) {}
    public record Reset(@NotBlank @Size(max=20) String phoneNumber,
                        @NotNull @Pattern(regexp="[0-9]{6}") String code,
                        @NotNull @Size(min=12,max=64) String newPassword) {}
}
