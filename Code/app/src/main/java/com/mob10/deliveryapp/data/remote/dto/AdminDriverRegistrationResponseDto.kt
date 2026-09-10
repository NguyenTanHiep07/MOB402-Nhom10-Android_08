package com.mob10.deliveryapp.data.remote.dto

data class AdminDriverRegistrationResponseDto(
    val id: Long,
    val user: AdminUserResponseDto?,
    val licensePlate: String,
    val status: String,
    val createdAt: String
)
