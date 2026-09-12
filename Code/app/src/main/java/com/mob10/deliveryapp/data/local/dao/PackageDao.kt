package com.mob10.deliveryapp.data.local.dao

import androidx.room.*
import com.mob10.deliveryapp.data.local.entity.PackageEntity

// DAO quản lý kiện hàng của đơn giao
@Dao
interface PackageDao {
    @Query("SELECT * FROM packages WHERE deliveryRequestId = :requestId")
    suspend fun getPackagesForRequest(requestId: Int): List<PackageEntity>

    @Insert
    suspend fun insert(pkg: PackageEntity): Long
}
