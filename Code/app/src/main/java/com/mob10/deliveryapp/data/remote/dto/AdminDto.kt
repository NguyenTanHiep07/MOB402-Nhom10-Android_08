package com.mob10.deliveryapp.data.remote.dto

data class AdminUserDto(
    val id: Int,
    val fullName: String? = null,
    val phoneNumber: String? = null,
    val role: String? = null
)

data class AdminOrderDto(
    val id: Int,
    val status: String? = null
)