package com.mob10.deliveryapp.data.local.dao

import androidx.room.*
import com.mob10.deliveryapp.data.local.entity.FeeRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FeeRuleDao {
    @Query("SELECT * FROM fee_rules WHERE isActive = 1 ORDER BY effectiveDate DESC LIMIT 1")
    fun getActiveFeeRule(): Flow<FeeRuleEntity?>

    @Query("SELECT * FROM fee_rules WHERE isActive = 1 ORDER BY effectiveDate DESC LIMIT 1")
    suspend fun getActiveFeeRuleSync(): FeeRuleEntity?

    @Query("SELECT * FROM fee_rules ORDER BY effectiveDate DESC")
    fun getAllFeeRules(): Flow<List<FeeRuleEntity>>

    @Query("SELECT * FROM fee_rules WHERE id = :id")
    suspend fun getFeeRuleById(id: Int): FeeRuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(feeRule: FeeRuleEntity): Long

    @Update
    suspend fun update(feeRule: FeeRuleEntity)

    @Delete
    suspend fun delete(feeRule: FeeRuleEntity)

    @Query("UPDATE fee_rules SET isActive = 0 WHERE id != :activeId")
    suspend fun deactivateOthers(activeId: Int)

    @Query("SELECT COUNT(*) FROM fee_rules")
    suspend fun getCount(): Int
}
