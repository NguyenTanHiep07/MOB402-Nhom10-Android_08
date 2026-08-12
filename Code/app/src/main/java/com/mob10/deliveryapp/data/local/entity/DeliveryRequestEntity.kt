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
        )
    ],
    indices = [
        Index("clientId"),
        Index("deliveryPersonId")
    ]
)
data class DeliveryRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    // Quan hệ người dùng
    val clientId: Int,
    val deliveryPersonId: Int? = null,

    // Địa chỉ lấy hàng
    val pickupAddress: String,
    val pickupLat: Double? = null,
    val pickupLng: Double? = null,

    // Địa chỉ giao hàng
    val deliveryAddress: String,
    val deliveryLat: Double? = null,
    val deliveryLng: Double? = null,

    // Thông tin người gửi
    val senderName: String,
    val senderPhone: String,

    // Thông tin người nhận
    val recipientName: String,
    val recipientPhone: String,

    // Thời gian
    val scheduledPickupTime: Long? = null,
    val actualPickupTime: Long? = null,
    val scheduledDeliveryTime: Long? = null,
    val actualDeliveryTime: Long? = null,

    // Phí cơ bản
    val distanceKm: Double,
    val baseFee: Double,
    val distanceFee: Double,
    val weightFee: Double,

    // Phụ phí
    val fragileCharge: Double = 0.0,
    val insuranceCharge: Double = 0.0,

    // Tổng
    val totalCost: Double,

    // Mã quy tắc tính phí
    val pricingRuleId: Int? = null,

    // Trạng thái & thời gian tạo/cập nhật
    val status: DeliveryStatus = DeliveryStatus.PENDING,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

