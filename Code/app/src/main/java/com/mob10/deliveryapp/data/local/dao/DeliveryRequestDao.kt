package com.mob10.deliveryapp.data.local.dao

import androidx.room.*
import com.mob10.deliveryapp.data.local.entity.DeliveryRequestEntity
import com.mob10.deliveryapp.data.model.DeliveryStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface DeliveryRequestDao {
    @Query("SELECT * FROM delivery_requests ORDER BY createdAt DESC")
    fun getAllRequests(): Flow<List<DeliveryRequestEntity>>

    @Query("SELECT * FROM delivery_requests WHERE clientId = :clientId ORDER BY createdAt DESC")
    fun getRequestsByClient(clientId: Int): Flow<List<DeliveryRequestEntity>>

    @Query("SELECT * FROM delivery_requests WHERE deliveryPersonId = :deliveryId ORDER BY createdAt DESC")
    fun getRequestsByDelivery(deliveryId: Int): Flow<List<DeliveryRequestEntity>>

    @Query("SELECT * FROM delivery_requests WHERE status = 'CHO_TIEP_NHAN' ORDER BY createdAt DESC")
    fun getPendingRequests(): Flow<List<DeliveryRequestEntity>>

    @Query("SELECT * FROM delivery_requests WHERE id = :id")
    suspend fun getRequestById(id: Int): DeliveryRequestEntity?

    @Query("SELECT COUNT(*) FROM delivery_requests")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM delivery_requests WHERE status = 'CHO_TIEP_NHAN'")
    fun getPendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM delivery_requests WHERE clientId = :clientId AND status NOT IN ('CHO_TIEP_NHAN','DA_HUY')")
    fun getActiveCountForClient(clientId: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM delivery_requests WHERE clientId = :clientId AND status = 'DA_GIAO'")
    fun getDeliveredCountForClient(clientId: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM delivery_requests WHERE deliveryPersonId = :deliveryId AND status = 'DA_GIAO' AND actualDeliveryTime >= :startOfDay")
    fun getDeliveredTodayCountForDriver(deliveryId: Int, startOfDay: Long): Flow<Int>

    @Insert
    suspend fun insert(request: DeliveryRequestEntity): Long

    @Update
    suspend fun update(request: DeliveryRequestEntity)

    @Query("UPDATE delivery_requests SET status = :status WHERE id = :requestId")
    suspend fun updateStatus(requestId: Int, status: DeliveryStatus)

    @Query("UPDATE delivery_requests SET deliveryPersonId = :deliveryId, status = :status WHERE id = :requestId")
    suspend fun assignToDelivery(requestId: Int, deliveryId: Int, status: DeliveryStatus)
}

