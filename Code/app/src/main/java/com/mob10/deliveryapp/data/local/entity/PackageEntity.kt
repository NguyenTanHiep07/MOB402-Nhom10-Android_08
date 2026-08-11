package com.mob10.deliveryapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "packages")
data class PackageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val deliveryRequestId: Int,
    val name: String,
    val weightKg: Double
)
