package com.mob10.deliveryapp.data.local.dao

import androidx.room.*
import com.mob10.deliveryapp.data.local.entity.StatusHistoryEntity

@Dao
interface StatusHistoryDao {
    @Query("SELECT * FROM status_history WHERE deliveryRequestId = :requestId ORDER BY timestamp ASC")
    suspend fun getHistoryForRequest(requestId: Int): List<StatusHistoryEntity>

    @Insert
    suspend fun insert(history: StatusHistoryEntity): Long
}
