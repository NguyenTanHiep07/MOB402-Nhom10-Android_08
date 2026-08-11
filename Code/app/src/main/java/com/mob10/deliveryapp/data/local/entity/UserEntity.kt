package com.mob10.deliveryapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mob10.deliveryapp.data.model.Role

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val password: String = "123456",
    val fullName: String,
    val phoneNumber: String,
    val role: Role
)
