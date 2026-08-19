package com.mob10.deliveryapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mob10.deliveryapp.data.model.DeliveryStatus

@Entity(
    tableName = "delivery_requests",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["deliveryPersonId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = FeeRuleEntity::class,
            parentColumns = ["id"],
            childColumns = ["pricingRuleId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("clientId"),
        Index("deliveryPersonId"),
        Index("pricingRuleId"),
        Index("status")
    ]
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
    val status: DeliveryStatus = DeliveryStatus.CHO_TIEP_NHAN,
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
