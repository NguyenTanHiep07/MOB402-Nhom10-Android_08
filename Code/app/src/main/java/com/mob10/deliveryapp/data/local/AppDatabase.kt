package com.mob10.deliveryapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mob10.deliveryapp.data.local.dao.*
import com.mob10.deliveryapp.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        DeliveryRequestEntity::class,
        PackageEntity::class,
        StatusHistoryEntity::class,
        FeeRuleEntity::class
    ],
    version = 6,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun deliveryRequestDao(): DeliveryRequestDao
    abstract fun packageDao(): PackageDao
    abstract fun statusHistoryDao(): StatusHistoryDao
    abstract fun feeRuleDao(): FeeRuleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Version 5 used a local demo login. Keep the structured data, discard its plaintext credentials.
        val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("UPDATE users SET password = ''")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "delivery_database"
                )
                .addMigrations(MIGRATION_5_6)
                .addCallback(object : Callback() {
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        // Bật thực thi Foreign Key constraints (Room tắt theo mặc định)
                        db.execSQL("PRAGMA foreign_keys = ON")
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
