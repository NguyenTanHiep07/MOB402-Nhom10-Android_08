package com.mob10.deliveryapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mob10.deliveryapp.data.model.DeliveryStatus

@Entity(
    tableName = "status_history",
    foreignKeys = [
        ForeignKey(
            entity = DeliveryRequestEntity::class,
            parentColumns = ["id"],
            childColumns = ["deliveryRequestId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["updatedBy"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("deliveryRequestId"),
        Index("updatedBy")
    ]
)
data class StatusHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val deliveryRequestId: Int,

    // Trạng thái cũ (null khi tạo đơn lần đầu)
    val fromStatus: DeliveryStatus? = null,

    // Trạng thái mới
    val toStatus: DeliveryStatus,

    // Người thực hiện cập nhật (userId, null = hệ thống)
    val updatedBy: Int? = null,

    // Thời gian cập nhật
    val timestamp: Long = System.currentTimeMillis(),

    // Ghi chú
    val note: String? = null
)


