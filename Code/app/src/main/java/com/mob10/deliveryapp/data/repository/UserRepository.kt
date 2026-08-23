package com.mob10.deliveryapp.data.repository

import com.mob10.deliveryapp.data.local.dao.UserDao
import com.mob10.deliveryapp.data.local.entity.UserEntity
import com.mob10.deliveryapp.data.session.SessionStorage

class UserRepository(
    private val userDao: UserDao,
    private val sessionStorage: SessionStorage? = null
) {
    suspend fun login(phoneNumber: String, password: String): UserEntity? {
        val user = userDao.login(phoneNumber, password) ?: return null
        sessionStorage?.saveUserId(user.id)
        return user
    }

    /**
     * Khôi phục phiên bằng dữ liệu Room hiện tại, không tin vào role đã lưu cũ.
     * Nếu tài khoản đã bị xóa thì session không còn hợp lệ và phải được dọn ngay.
     */
    suspend fun restoreSession(): UserEntity? {
        val storage = sessionStorage ?: return null
        val userId = storage.getUserId() ?: return null
        val user = userDao.getUserById(userId)
        if (user == null) {
            storage.clear()
        }
        return user
    }

    suspend fun logout() {
        sessionStorage?.clear()
    }

    suspend fun updateUser(user: UserEntity) {
        userDao.update(user)
    }

    fun getTotalUserCount() = userDao.getTotalUserCount()
    
    fun getCountByRole(role: com.mob10.deliveryapp.data.model.Role) = userDao.getCountByRole(role)
}
