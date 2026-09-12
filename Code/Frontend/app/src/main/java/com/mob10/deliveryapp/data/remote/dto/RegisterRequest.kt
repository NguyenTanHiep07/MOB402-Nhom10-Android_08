package com.mob10.deliveryapp.data.remote.dto

data class RegisterRequest(
    val phoneNumber: String,
    val password: String,
    val fullName: String
)
