package com.mob10.deliveryapp.data.repository

import com.mob10.deliveryapp.data.local.dao.UserDao
import com.mob10.deliveryapp.data.local.entity.UserEntity
import com.mob10.deliveryapp.data.session.SessionStorage

class UserRepository(
    private val userDao: UserDao,
    private val sessionStorage: SessionStorage? = null
) {
    // TH - tuần 3,4: luồng room ban đầu; app hiện đăng nhập qua server
    suspend fun login(phoneNumber: String, password: String): UserEntity? {
        // TH - tuần 3,4: không tìm thấy user thì trả null
        val user = userDao.login(phoneNumber, password) ?: return null
        // TH - tuần 3,4: chỉ lưu id sau khi đăng nhập đúng
        sessionStorage?.saveUserId(user.id)
        return user
    }

    // TH - tuần 3,4: lấy lại user cũ; nếu mất thì xoá phiên hỏng
    suspend fun restoreSession(): UserEntity? {
        // TH - tuần 3,4: không có session hoặc id thì về login
        val storage = sessionStorage ?: return null
        val userId = storage.getUserId() ?: return null
        // TH - tuần 3,4: đọc lại user và role mới nhất từ room
        val user = userDao.getUserById(userId)
        if (user == null) {
            // TH - tuần 3,4: user bị xoá nên session không còn hợp lệ
            storage.clear()
        }
        return user
    }

    // TH - tuần 3,4: xoá phiên đăng nhập đã lưu
    suspend fun logout() {
        sessionStorage?.clear()
    }

    // TH - phần auth phát triển sau: lưu user server và ghi nhớ id
    suspend fun saveAuthenticatedUser(user: UserEntity) {
        userDao.upsert(user)
        sessionStorage?.saveUserId(user.id)
    }

    suspend fun updateUser(user: UserEntity) {
        userDao.update(user)
    }

    fun getTotalUserCount() = userDao.getTotalUserCount()
    
    fun getCountByRole(role: com.mob10.deliveryapp.data.model.Role) = userDao.getCountByRole(role)

    fun getUsersByRole(role: com.mob10.deliveryapp.data.model.Role) = userDao.getUsersByRole(role)
}
