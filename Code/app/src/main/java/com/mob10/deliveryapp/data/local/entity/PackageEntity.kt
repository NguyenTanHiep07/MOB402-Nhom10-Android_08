package com.mob10.deliveryapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "packages",
    foreignKeys = [
        ForeignKey(
            entity = DeliveryRequestEntity::class,
            parentColumns = ["id"],
            childColumns = ["deliveryRequestId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("deliveryRequestId")]
)
data class PackageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val deliveryRequestId: Int,
    val name: String,
    val packageType: String? = null,
    val weightKg: Double,
    val quantity: Int = 1,
    val notes: String? = null,
    val note: String? = null,
    val isFragile: Boolean = false
)
