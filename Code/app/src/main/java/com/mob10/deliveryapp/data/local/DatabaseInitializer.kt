package com.mob10.deliveryapp.data.local

import com.mob10.deliveryapp.data.local.entity.UserEntity
import com.mob10.deliveryapp.data.model.Role
import kotlinx.coroutines.flow.first

class DatabaseInitializer(private val db: AppDatabase) {
    suspend fun initialize() {
        val userDao = db.userDao()
        val users = userDao.getAllUsers().first()
        
        if (users.isEmpty()) {
            // Sample Clients
            userDao.insert(UserEntity(username = "client1", fullName = "Nguyễn Văn A", phoneNumber = "0123456789", role = Role.CLIENT))
            userDao.insert(UserEntity(username = "client2", fullName = "Trần Thị B", phoneNumber = "0987654321", role = Role.CLIENT))
            
            // Sample Delivery Staff
            userDao.insert(UserEntity(username = "shipper1", fullName = "Lê Văn C", phoneNumber = "0111222333", role = Role.DELIVERY))
            userDao.insert(UserEntity(username = "shipper2", fullName = "Phạm Văn D", phoneNumber = "0444555666", role = Role.DELIVERY))
            
            // Sample Admin
            userDao.insert(UserEntity(username = "admin", fullName = "Quản trị viên", phoneNumber = "0000000000", role = Role.ADMIN))
        }
    }
}
