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

        /** Migration từ Version 1 lên Version 6 */
        val MIGRATION_1_6 = object : androidx.room.migration.Migration(1, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Cập nhật bảng users
                db.execSQL("ALTER TABLE users ADD COLUMN licensePlate TEXT")
                db.execSQL("ALTER TABLE users ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_users_username ON users (username)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_users_phoneNumber ON users (phoneNumber)")
                db.execSQL("UPDATE users SET password = ''")

                // 2. Tạo bảng fee_rules (chưa có ở v1)
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS fee_rules (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        ruleName TEXT NOT NULL,
                        baseFee REAL NOT NULL,
                        pricePerKm REAL NOT NULL,
                        pricePerKg REAL NOT NULL,
                        fragileFee REAL NOT NULL,
                        fragileMultiplier REAL NOT NULL,
                        expressMultiplier REAL NOT NULL,
                        effectiveDate INTEGER NOT NULL,
                        isActive INTEGER NOT NULL,
                        createdBy INTEGER,
                        FOREIGN KEY(createdBy) REFERENCES users(id) ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_fee_rules_createdBy ON fee_rules (createdBy)")

                // 3. Đảm bảo indices cho delivery_requests, packages, status_history
                db.execSQL("CREATE INDEX IF NOT EXISTS index_delivery_requests_clientId ON delivery_requests (clientId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_delivery_requests_deliveryPersonId ON delivery_requests (deliveryPersonId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_delivery_requests_pricingRuleId ON delivery_requests (pricingRuleId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_delivery_requests_status ON delivery_requests (status)")

                db.execSQL("CREATE INDEX IF NOT EXISTS index_packages_deliveryRequestId ON packages (deliveryRequestId)")

                db.execSQL("CREATE INDEX IF NOT EXISTS index_status_history_deliveryRequestId ON status_history (deliveryRequestId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_status_history_updatedBy ON status_history (updatedBy)")
            }
        }

        /** Migration từ Version 2 lên Version 6 */
        val MIGRATION_2_6 = object : androidx.room.migration.Migration(2, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                MIGRATION_1_6.migrate(db)
            }
        }

        /** Migration từ Version 3 lên Version 6 */
        val MIGRATION_3_6 = object : androidx.room.migration.Migration(3, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_users_username ON users (username)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_users_phoneNumber ON users (phoneNumber)")
                db.execSQL("UPDATE users SET password = ''")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_delivery_requests_status ON delivery_requests (status)")
            }
        }

        /** Migration từ Version 4 lên Version 6 */
        val MIGRATION_4_6 = object : androidx.room.migration.Migration(4, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                MIGRATION_3_6.migrate(db)
            }
        }

        /** Migration từ Version 5 lên Version 6: Xoá mật khẩu plain-text cục bộ để chuyển sang Backend API */
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
                .addMigrations(
                    MIGRATION_1_6,
                    MIGRATION_2_6,
                    MIGRATION_3_6,
                    MIGRATION_4_6,
                    MIGRATION_5_6
                )
                .fallbackToDestructiveMigration()
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
