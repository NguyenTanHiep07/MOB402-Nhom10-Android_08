package com.mob10.deliveryapp.data.model

data class AdminDriverRequest(
    val id: Long,
    val user: AdminUser?,
    val licensePlate: String,
    val status: String,
    val createdAt: String
)
