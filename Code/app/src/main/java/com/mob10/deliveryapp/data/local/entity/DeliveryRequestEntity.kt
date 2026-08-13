package com.mob10.deliveryapp.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mob10.deliveryapp.data.model.DeliveryStatus

@Entity(
    tableName = "delivery_requests",
    indices = [Index(value = ["clientId"]), Index(value = ["deliveryPersonId"])]
)
data class DeliveryRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clientId: Int,
    val deliveryPersonId: Int? = null,
    val distanceKm: Double,
    val baseFee: Double,
    val distanceFee: Double,
    val weightFee: Double,
    val totalCost: Double,
    val status: DeliveryStatus,
    val restaurantName: String = "",
    val restaurantAddress: String = "",
    val restaurantPhone: String = "",
    val customerName: String = "",
    val customerAddress: String = "",
    val customerPhone: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
