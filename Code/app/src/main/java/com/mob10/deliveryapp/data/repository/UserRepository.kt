package com.mob10.deliveryapp.data.repository

import com.mob10.deliveryapp.data.local.dao.UserDao
import com.mob10.deliveryapp.data.local.entity.UserEntity

class UserRepository(private val userDao: UserDao) {
    suspend fun login(phoneNumber: String, password: String): UserEntity? {
        return userDao.login(phoneNumber, password)
    }
}
