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
    val pickupAddress: String = "",
    val deliveryAddress: String = "",
    val senderName: String = "",
    val senderPhone: String = "",
    val recipientName: String = "",
    val recipientPhone: String = "",
    val fragileCharge: Double = 0.0,
    val pricingRuleId: Int? = null,
    val scheduledPickupTime: Long? = null,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val actualDeliveryTime: Long? = null
)
