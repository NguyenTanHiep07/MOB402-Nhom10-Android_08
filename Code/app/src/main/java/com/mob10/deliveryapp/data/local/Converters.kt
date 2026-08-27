package com.mob10.deliveryapp.data.local

import androidx.room.TypeConverter
import com.mob10.deliveryapp.data.model.DeliveryStatus
import com.mob10.deliveryapp.data.model.Role

class Converters {
    @TypeConverter
    fun fromRole(role: Role): String = role.name

    @TypeConverter
    fun toRole(role: String): Role = Role.valueOf(role)

    @TypeConverter
    fun fromStatus(status: DeliveryStatus): String = status.name

    @TypeConverter
    fun toStatus(status: String): DeliveryStatus = DeliveryStatus.valueOf(status)
}
