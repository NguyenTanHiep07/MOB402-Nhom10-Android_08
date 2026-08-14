package com.mob10.deliveryapp.data.local

import com.mob10.deliveryapp.data.local.entity.DeliveryRequestEntity
import com.mob10.deliveryapp.data.local.entity.PackageEntity
import com.mob10.deliveryapp.data.local.entity.StatusHistoryEntity
import com.mob10.deliveryapp.data.local.entity.UserEntity
import com.mob10.deliveryapp.data.model.DeliveryStatus
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

        // Add Dummy Delivery Requests if missing
        val deliveryDao = db.deliveryRequestDao()
        val allRequests = deliveryDao.getAllRequests().first()
        val hasNewOrders = allRequests.any { it.status == com.mob10.deliveryapp.data.model.DeliveryStatus.CHO_TIEP_NHAN }
        val hasActiveOrders = allRequests.any { it.status == com.mob10.deliveryapp.data.model.DeliveryStatus.DA_CHAP_NHAN }
        val packageDao = db.packageDao()

        // Lấy user IDs thực tế từ DB thay vì hard-code
        val allUsers = userDao.getAllUsers().first()
        val client1Id = allUsers.find { it.username == "client1" }?.id ?: return
        val client2Id = allUsers.find { it.username == "client2" }?.id ?: return
        val shipper1Id = allUsers.find { it.username == "shipper1" }?.id ?: return

        if (!hasNewOrders) {
            // Dummy Order 1 (New Order - Chờ tiếp nhận)
            val request1 = com.mob10.deliveryapp.data.local.entity.DeliveryRequestEntity(
                clientId = client1Id,
                distanceKm = 2.5,
                baseFee = 10000.0,
                distanceFee = 12500.0,
                weightFee = 5000.0,
                totalCost = 27500.0,
                status = com.mob10.deliveryapp.data.model.DeliveryStatus.CHO_TIEP_NHAN,
                senderName = "Nhà hàng ABC",
                pickupAddress = "123 Nguyễn Văn A, Quận 1",
                senderPhone = "0901234567",
                recipientName = "Nguyễn Văn B",
                deliveryAddress = "456 Lê Văn B, Quận 3",
                recipientPhone = "0987654321"
            )
            val req1Id = deliveryDao.insert(request1).toInt()
            packageDao.insert(com.mob10.deliveryapp.data.local.entity.PackageEntity(deliveryRequestId = req1Id, name = "Cơm gà", weightKg = 0.5, quantity = 1, note = "Nhiều cơm"))
            packageDao.insert(com.mob10.deliveryapp.data.local.entity.PackageEntity(deliveryRequestId = req1Id, name = "Coca Cola", weightKg = 0.3, quantity = 2))
        }

        if (!hasActiveOrders) {
            // Dummy Order 2 (Active Order - Đã nhận đơn - assigned to shipper1)
            val request2 = com.mob10.deliveryapp.data.local.entity.DeliveryRequestEntity(
                clientId = client2Id,
                deliveryPersonId = shipper1Id,
                distanceKm = 4.0,
                baseFee = 10000.0,
                distanceFee = 20000.0,
                weightFee = 0.0,
                totalCost = 30000.0,
                status = com.mob10.deliveryapp.data.model.DeliveryStatus.DA_CHAP_NHAN,
                senderName = "Phở Hòa",
                pickupAddress = "260C Pasteur, Quận 3",
                senderPhone = "0900000000",
                recipientName = "Trần Thị C",
                deliveryAddress = "789 Lý Tự Trọng, Quận 1",
                recipientPhone = "0111111111"
            )
            val req2Id = deliveryDao.insert(request2).toInt()
            packageDao.insert(com.mob10.deliveryapp.data.local.entity.PackageEntity(deliveryRequestId = req2Id, name = "Phở Bò", weightKg = 0.6, quantity = 2))
        }
    }
}

