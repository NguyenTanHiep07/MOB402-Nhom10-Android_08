package com.mob10.deliveryapp.data.remote.dto

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val accessToken: String,
    val tokenType: String,
    val expiresInMs: Long,
    val user: AuthUserDto
)

data class AuthUserDto(
    val id: Int,
    val username: String,
    val fullName: String,
    val phoneNumber: String,
    val role: String,
    val licensePlate: String? = null
)

/**
 * Đăng ký tài khoản mới.
 * Ràng buộc theo yêu cầu: username tối thiểu 8 ký tự, phoneNumber đúng 10 số.
 * TODO: xác nhận với Thịnh field chính xác backend cần (role mặc định là gì, có cần confirmPassword không).
 */
data class RegisterRequest(
    val username: String,
    val password: String,
    val fullName: String,
    val phoneNumber: String,
    val role: String = "CLIENT"
)

data class RegisterResponse(
    val id: Int,
    val username: String,
    val fullName: String,
    val phoneNumber: String,
    val role: String
)