package com.mob10.deliveryapp.data.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Room stores the authenticated profile locally. Real orders and demo seed belong to the backend. */
class DatabaseInitializer(private val db: AppDatabase) {
    suspend fun initialize() = withContext(Dispatchers.IO) {
        db.openHelper.writableDatabase
        Unit
    }
}
