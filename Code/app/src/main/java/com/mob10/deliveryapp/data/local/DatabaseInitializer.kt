package com.mob10.deliveryapp.data.local

import com.mob10.deliveryapp.data.local.entity.UserEntity
import com.mob10.deliveryapp.data.model.Role
import kotlinx.coroutines.flow.first

class DatabaseInitializer(private val db: AppDatabase) {
    suspend fun initialize() {
        val userDao = db.userDao()
        val existingUsers = userDao.getAllUsers().first().associateBy { it.username }

        // Remove the temporary accounts from the previous UI-only seed.
        userDao.deleteByUsernames(listOf("customer", "driver"))

        val sampleUsers = listOf(
            // Sample Clients
            UserEntity(
                username = "client1",
                password = "123456",
                fullName = "Nguyễn Văn A",
                phoneNumber = "0123456789",
                role = Role.CLIENT
            ),
            UserEntity(
                username = "client2",
                password = "123456",
                fullName = "Trần Thị B",
                phoneNumber = "0987654321",
                role = Role.CLIENT
            ),
            // Sample Delivery Staff
            UserEntity(
                username = "shipper1",
                password = "123456",
                fullName = "Lê Văn C",
                phoneNumber = "0111222333",
                role = Role.DELIVERY
            ),
            UserEntity(
                username = "shipper2",
                password = "123456",
                fullName = "Phạm Văn D",
                phoneNumber = "0444555666",
                role = Role.DELIVERY
            ),
            // Sample Admin
            UserEntity(
                username = "admin",
                password = "123456",
                fullName = "Quản trị viên",
                phoneNumber = "0000000000",
                role = Role.ADMIN
            )
        )

        sampleUsers.forEach { sampleUser ->
            val existingUser = existingUsers[sampleUser.username]
            if (existingUser == null ||
                existingUser.password != sampleUser.password ||
                existingUser.fullName != sampleUser.fullName ||
                existingUser.phoneNumber != sampleUser.phoneNumber ||
                existingUser.role != sampleUser.role
            ) {
                userDao.insert(sampleUser)
            }
        }
    }
}
