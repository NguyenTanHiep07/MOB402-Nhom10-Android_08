package com.mob10.deliveryapp.data.repository

import com.mob10.deliveryapp.data.local.dao.UserDao
import com.mob10.deliveryapp.data.local.entity.UserEntity
import com.mob10.deliveryapp.data.model.Role
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {

    /**
     * Đăng nhập chỉ bằng số điện thoại và mật khẩu.
     */
    suspend fun login(phoneNumber: String, password: String): UserEntity? =
        userDao.login(phoneNumber, password)

    suspend fun getUserById(userId: Int): UserEntity? =
        userDao.getUserById(userId)

    suspend fun insertUser(user: UserEntity): Long =
        userDao.insert(user)

    fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllUsers()

    fun getUsersByRole(role: Role): Flow<List<UserEntity>> = userDao.getUsersByRole(role)

    fun getCountByRole(role: Role) = userDao.getCountByRole(role)

    fun getTotalUserCount() = userDao.getTotalUserCount()
}



