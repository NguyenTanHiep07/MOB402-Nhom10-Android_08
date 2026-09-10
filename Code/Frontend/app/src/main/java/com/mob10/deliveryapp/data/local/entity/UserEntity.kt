package com.mob10.deliveryapp.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mob10.deliveryapp.data.model.Role

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["username"], unique = true),
        Index(value = ["phoneNumber"], unique = true)
    ]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val password: String,
    val fullName: String,
    val phoneNumber: String,
    val role: Role,
    val licensePlate: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
