package com.mob10.deliveryapp.data.local

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.sqlite.db.SupportSQLiteOpenHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import androidx.test.core.app.ApplicationProvider
import android.content.Context

@RunWith(RobolectricTestRunner::class)
class RoomMigrationTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun createInMemoryDb(): SupportSQLiteDatabase {
        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name("test_migration.db")
            .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    // Tạo bảng version 1 mẫu
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS users (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            username TEXT NOT NULL,
                            password TEXT NOT NULL,
                            fullName TEXT NOT NULL,
                            phoneNumber TEXT NOT NULL,
                            role TEXT NOT NULL
                        )
                        """.trimIndent()
                    )
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS delivery_requests (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            clientId INTEGER NOT NULL,
                            deliveryPersonId INTEGER,
                            distanceKm REAL NOT NULL,
                            baseFee REAL NOT NULL,
                            distanceFee REAL NOT NULL,
                            weightFee REAL NOT NULL,
                            totalCost REAL NOT NULL,
                            status TEXT NOT NULL,
                            pickupAddress TEXT NOT NULL,
                            deliveryAddress TEXT NOT NULL,
                            senderName TEXT NOT NULL,
                            senderPhone TEXT NOT NULL,
                            recipientName TEXT NOT NULL,
                            recipientPhone TEXT NOT NULL,
                            fragileCharge REAL NOT NULL,
                            pricingRuleId INTEGER,
                            scheduledPickupTime INTEGER,
                            note TEXT,
                            createdAt INTEGER NOT NULL,
                            actualDeliveryTime INTEGER
                        )
                        """.trimIndent()
                    )
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS packages (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            deliveryRequestId INTEGER NOT NULL,
                            name TEXT NOT NULL,
                            packageType TEXT,
                            weightKg REAL NOT NULL,
                            quantity INTEGER NOT NULL,
                            notes TEXT,
                            note TEXT,
                            isFragile INTEGER NOT NULL
                        )
                        """.trimIndent()
                    )
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS status_history (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            deliveryRequestId INTEGER NOT NULL,
                            fromStatus TEXT,
                            toStatus TEXT NOT NULL,
                            updatedBy INTEGER,
                            timestamp INTEGER NOT NULL,
                            note TEXT
                        )
                        """.trimIndent()
                    )
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()

        context.deleteDatabase("test_migration.db")
        return FrameworkSQLiteOpenHelperFactory().create(config).writableDatabase
    }

    @Test
    fun testMigration1To6() {
        val db = createInMemoryDb()

        // Thêm dữ liệu mẫu vào v1
        db.execSQL("INSERT INTO users (id, username, password, fullName, phoneNumber, role) VALUES (1, 'client1', 'old_plain_pass', 'Nguyen Van A', '0123456789', 'CLIENT')")

        // Chạy migration 1 -> 6
        AppDatabase.MIGRATION_1_6.migrate(db)

        // Kiểm tra dữ liệu sau migration
        val cursor = db.query("SELECT id, username, password, fullName, phoneNumber, role, licensePlate, createdAt FROM users WHERE id = 1")
        assertTrue(cursor.moveToFirst())
        assertEquals("client1", cursor.getString(1))
        assertEquals("", cursor.getString(2)) // password đã được xoá an toàn
        assertEquals("Nguyen Van A", cursor.getString(3))
        assertEquals("0123456789", cursor.getString(4))
        cursor.close()

        // Kiểm tra bảng fee_rules đã được tạo
        val feeCursor = db.query("SELECT count(*) FROM sqlite_master WHERE type='table' AND name='fee_rules'")
        assertTrue(feeCursor.moveToFirst())
        assertEquals(1, feeCursor.getInt(0))
        feeCursor.close()

        db.close()
    }

    @Test
    fun testMigration5To6() {
        val db = createInMemoryDb()

        // Thêm user có password cũ
        db.execSQL("INSERT INTO users (id, username, password, fullName, phoneNumber, role) VALUES (2, 'driver1', 'plain_pass_123', 'Le Van C', '0111222333', 'DRIVER')")

        // Chạy migration 5 -> 6
        AppDatabase.MIGRATION_5_6.migrate(db)

        val cursor = db.query("SELECT password FROM users WHERE id = 2")
        assertTrue(cursor.moveToFirst())
        assertEquals("", cursor.getString(0))
        cursor.close()

        db.close()
    }
}
