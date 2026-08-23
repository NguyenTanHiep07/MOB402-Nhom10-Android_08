package com.mob10.deliveryapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "fee_rules",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["createdBy"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("createdBy")]
)
data class FeeRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ruleName: String = "Bảng giá tiêu chuẩn",
    val baseFee: Double = 15_000.0,
    val pricePerKm: Double = 5_000.0,
    val pricePerKg: Double = 3_000.0,
    val fragileFee: Double = 5_000.0,
    val fragileMultiplier: Double = 1.4,
    val expressMultiplier: Double = 1.8,
    val effectiveDate: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val createdBy: Int? = null
)
