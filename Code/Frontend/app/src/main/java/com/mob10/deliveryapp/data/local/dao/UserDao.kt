package com.mob10.deliveryapp.data.local.dao

import androidx.room.*
import com.mob10.deliveryapp.data.local.entity.UserEntity
import com.mob10.deliveryapp.data.model.Role
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE phoneNumber = :phoneNumber AND password = :password LIMIT 1")
    suspend fun login(phoneNumber: String, password: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Int): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity): Long

    @Upsert
    suspend fun upsert(user: UserEntity)

    @Update
    suspend fun update(user: UserEntity)

    @Query("DELETE FROM users WHERE username IN (:usernames)")
    suspend fun deleteByUsernames(usernames: List<String>): Int

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE role = :role")
    fun getUsersByRole(role: Role): Flow<List<UserEntity>>

    @Query("SELECT COUNT(*) FROM users WHERE role = :role")
    fun getCountByRole(role: Role): Flow<Int>

    @Query("SELECT COUNT(*) FROM users")
    fun getTotalUserCount(): Flow<Int>
}

