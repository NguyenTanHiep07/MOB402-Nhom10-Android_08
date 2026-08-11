package com.mob10.deliveryapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mob10.deliveryapp.data.model.DeliveryStatus

@Entity(tableName = "delivery_requests")
data class DeliveryRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clientId: Int,
    val deliveryPersonId: Int? = null,
    val distanceKm: Double,
    val baseFee: Double,
    val distanceFee: Double,
    val weightFee: Double,
    val totalCost: Double,
    val status: DeliveryStatus = DeliveryStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
)
