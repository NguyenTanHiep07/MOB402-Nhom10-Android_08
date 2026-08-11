package com.mob10.deliveryapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mob10.deliveryapp.data.model.DeliveryStatus

@Entity(tableName = "status_history")
data class StatusHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val deliveryRequestId: Int,
    val status: DeliveryStatus,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String? = null
)
