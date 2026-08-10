package com.mob10.deliveryapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mob10.deliveryapp.data.local.dao.*
import com.mob10.deliveryapp.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        DeliveryRequestEntity::class,
        PackageEntity::class,
        StatusHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun deliveryRequestDao(): DeliveryRequestDao
    abstract fun packageDao(): PackageDao
    abstract fun statusHistoryDao(): StatusHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "delivery_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
