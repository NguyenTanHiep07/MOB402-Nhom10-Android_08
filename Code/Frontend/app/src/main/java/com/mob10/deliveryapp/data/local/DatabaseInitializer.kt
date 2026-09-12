package com.mob10.deliveryapp.data.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Khởi tạo kết nối CSDL cục bộ
class DatabaseInitializer(private val db: AppDatabase) {
    suspend fun initialize() = withContext(Dispatchers.IO) {
        db.openHelper.writableDatabase
        Unit
    }
}
