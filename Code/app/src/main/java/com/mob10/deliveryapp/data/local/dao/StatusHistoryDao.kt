package com.mob10.deliveryapp.data.local.dao

import androidx.room.*
import com.mob10.deliveryapp.data.local.entity.StatusHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StatusHistoryDao {
    @Query("SELECT * FROM status_history WHERE deliveryRequestId = :requestId ORDER BY timestamp ASC")
    fun getHistoryForRequestFlow(requestId: Int): Flow<List<StatusHistoryEntity>>

    @Query("SELECT * FROM status_history WHERE deliveryRequestId = :requestId ORDER BY timestamp ASC")
    suspend fun getHistoryForRequest(requestId: Int): List<StatusHistoryEntity>

    @Insert
    suspend fun insert(history: StatusHistoryEntity): Long
}

