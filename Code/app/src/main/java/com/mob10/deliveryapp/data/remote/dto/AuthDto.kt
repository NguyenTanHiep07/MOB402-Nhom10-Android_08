package com.mob10.deliveryapp.data.remote.dto

/**
 * DTOs cho Authentication API.
 * Map trực tiếp với backend AuthDtos.java
 */

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val accessToken: String,
    val tokenType: String,
    val expiresInMs: Long,
    val user: UserSummaryDto
)

data class UserSummaryDto(
    val id: Long,
    val username: String,
    val fullName: String?,
    val phoneNumber: String?,
    val role: String,           // "CLIENT", "DELIVERY", "ADMIN"
    val licensePlate: String?
)
