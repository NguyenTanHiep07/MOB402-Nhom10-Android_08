package com.mob10.deliveryapp.data.remote.dto

data class RegisterRequest(
    val username: String,
    val password: String,
    val fullName: String,
    val phoneNumber: String
)
