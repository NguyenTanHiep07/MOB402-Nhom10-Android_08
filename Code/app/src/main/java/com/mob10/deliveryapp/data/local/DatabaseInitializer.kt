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
        val requestDao = db.deliveryRequestDao()
        val packageDao = db.packageDao()
        val historyDao = db.statusHistoryDao()

        val existingUsers = userDao.getAllUsers().first()
        if (existingUsers.isNotEmpty()) return // Đã seed trước đó

        // ─── Seed Users ───────────────────────────────────────────────────────
        val clientId1 = userDao.insert(UserEntity(username = "client1", fullName = "Nguyễn Văn A", phoneNumber = "0123456789", role = Role.CLIENT)).toInt()
        val clientId2 = userDao.insert(UserEntity(username = "client2", fullName = "Trần Thị B", phoneNumber = "0987654321", role = Role.CLIENT)).toInt()
        val shipperId1 = userDao.insert(UserEntity(username = "shipper1", fullName = "Lê Văn C", phoneNumber = "0111222333", role = Role.DELIVERY)).toInt()
        userDao.insert(UserEntity(username = "shipper2", fullName = "Phạm Văn D", phoneNumber = "0444555666", role = Role.DELIVERY))
        userDao.insert(UserEntity(username = "admin", fullName = "Quản trị viên", phoneNumber = "0000000000", role = Role.ADMIN))

        // ─── Seed Delivery Requests ──────────────────────────────────────────
        val now = System.currentTimeMillis()
        val oneHour = 3_600_000L
        val oneDay = 86_400_000L

        // Đơn 1: Đang giao (IN_TRANSIT) – assigned cho shipper1
        val req1Id = requestDao.insert(
            DeliveryRequestEntity(
                clientId = clientId1,
                deliveryPersonId = shipperId1,
                pickupAddress = "12 Nguyễn Trãi, Quận 5, TP.HCM",
                deliveryAddress = "85 Lê Lợi, Quận 1, TP.HCM",
                senderName = "Nguyễn Văn A",
                senderPhone = "0123456789",
                recipientName = "Hoàng Minh Tuấn",
                recipientPhone = "0369852147",
                distanceKm = 3.5,
                baseFee = 10_000.0,
                distanceFee = 17_500.0,
                weightFee = 9_000.0,
                fragileCharge = 5_000.0,
                totalCost = 41_500.0,
                status = DeliveryStatus.IN_TRANSIT,
                actualPickupTime = now - oneHour,
                createdAt = now - 2 * oneHour
            )
        ).toInt()

        packageDao.insert(PackageEntity(deliveryRequestId = req1Id, name = "Điện thoại Samsung", weightKg = 0.5, isFragile = true, notes = "Hàng điện tử, cẩn thận"))
        packageDao.insert(PackageEntity(deliveryRequestId = req1Id, name = "Phụ kiện điện thoại", weightKg = 0.5, isFragile = false))

        historyDao.insert(StatusHistoryEntity(deliveryRequestId = req1Id, fromStatus = null, toStatus = DeliveryStatus.PENDING, updatedBy = clientId1, note = "Đơn hàng được tạo", timestamp = now - 2 * oneHour))
        historyDao.insert(StatusHistoryEntity(deliveryRequestId = req1Id, fromStatus = DeliveryStatus.PENDING, toStatus = DeliveryStatus.ACCEPTED, updatedBy = shipperId1, note = "Tài xế đã nhận đơn", timestamp = now - oneHour - 30 * 60_000))
        historyDao.insert(StatusHistoryEntity(deliveryRequestId = req1Id, fromStatus = DeliveryStatus.ACCEPTED, toStatus = DeliveryStatus.PICKED_UP, updatedBy = shipperId1, note = "Đã lấy hàng", timestamp = now - oneHour))
        historyDao.insert(StatusHistoryEntity(deliveryRequestId = req1Id, fromStatus = DeliveryStatus.PICKED_UP, toStatus = DeliveryStatus.IN_TRANSIT, updatedBy = shipperId1, note = "Đang trên đường giao", timestamp = now - 30 * 60_000))

        // Đơn 2: Chờ phân công (PENDING)
        val req2Id = requestDao.insert(
            DeliveryRequestEntity(
                clientId = clientId2,
                pickupAddress = "24 Trần Hưng Đạo, Quận 1, TP.HCM",
                deliveryAddress = "108 Võ Văn Tần, Quận 3, TP.HCM",
                senderName = "Trần Thị B",
                senderPhone = "0987654321",
                recipientName = "Lê Thị Hoa",
                recipientPhone = "0912345678",
                distanceKm = 2.1,
                baseFee = 10_000.0,
                distanceFee = 10_500.0,
                weightFee = 6_000.0,
                totalCost = 26_500.0,
                status = DeliveryStatus.PENDING,
                createdAt = now - oneDay / 2
            )
        ).toInt()

        packageDao.insert(PackageEntity(deliveryRequestId = req2Id, name = "Quần áo thời trang", weightKg = 2.0, notes = "Gói kín, không làm nhăn"))
        historyDao.insert(StatusHistoryEntity(deliveryRequestId = req2Id, fromStatus = null, toStatus = DeliveryStatus.PENDING, updatedBy = clientId2, note = "Đơn hàng được tạo", timestamp = now - oneDay / 2))

        // Đơn 3: Đã giao (DELIVERED) cho client1
        val req3Id = requestDao.insert(
            DeliveryRequestEntity(
                clientId = clientId1,
                deliveryPersonId = shipperId1,
                pickupAddress = "50 Đinh Tiên Hoàng, Bình Thạnh, TP.HCM",
                deliveryAddress = "18 Cách Mạng Tháng 8, Quận 10, TP.HCM",
                senderName = "Nguyễn Văn A",
                senderPhone = "0123456789",
                recipientName = "Phạm Quốc Bảo",
                recipientPhone = "0701234567",
                distanceKm = 5.8,
                baseFee = 10_000.0,
                distanceFee = 29_000.0,
                weightFee = 12_000.0,
                totalCost = 51_000.0,
                status = DeliveryStatus.DELIVERED,
                actualPickupTime = now - oneDay - oneHour,
                actualDeliveryTime = now - oneDay,
                createdAt = now - oneDay - 2 * oneHour
            )
        ).toInt()

        packageDao.insert(PackageEntity(deliveryRequestId = req3Id, name = "Sách giáo khoa", weightKg = 4.0))
        historyDao.insert(StatusHistoryEntity(deliveryRequestId = req3Id, fromStatus = null, toStatus = DeliveryStatus.PENDING, updatedBy = clientId1, timestamp = now - oneDay - 2 * oneHour))
        historyDao.insert(StatusHistoryEntity(deliveryRequestId = req3Id, fromStatus = DeliveryStatus.PENDING, toStatus = DeliveryStatus.ACCEPTED, updatedBy = shipperId1, timestamp = now - oneDay - oneHour))
        historyDao.insert(StatusHistoryEntity(deliveryRequestId = req3Id, fromStatus = DeliveryStatus.ACCEPTED, toStatus = DeliveryStatus.PICKED_UP, updatedBy = shipperId1, timestamp = now - oneDay - 30 * 60_000))
        historyDao.insert(StatusHistoryEntity(deliveryRequestId = req3Id, fromStatus = DeliveryStatus.PICKED_UP, toStatus = DeliveryStatus.IN_TRANSIT, updatedBy = shipperId1, timestamp = now - oneDay - 15 * 60_000))
        historyDao.insert(StatusHistoryEntity(deliveryRequestId = req3Id, fromStatus = DeliveryStatus.IN_TRANSIT, toStatus = DeliveryStatus.DELIVERED, updatedBy = shipperId1, note = "Giao thành công", timestamp = now - oneDay))

        // Đơn 4: Đã huỷ (CANCELLED) cho client2
        val req4Id = requestDao.insert(
            DeliveryRequestEntity(
                clientId = clientId2,
                pickupAddress = "77 Nguyễn Huệ, Quận 1, TP.HCM",
                deliveryAddress = "200 Nguyễn Thị Minh Khai, Quận 1, TP.HCM",
                senderName = "Trần Thị B",
                senderPhone = "0987654321",
                recipientName = "Vũ Thanh Long",
                recipientPhone = "0832109876",
                distanceKm = 1.5,
                baseFee = 10_000.0,
                distanceFee = 7_500.0,
                weightFee = 3_000.0,
                totalCost = 20_500.0,
                status = DeliveryStatus.CANCELLED,
                createdAt = now - 2 * oneDay
            )
        ).toInt()

        packageDao.insert(PackageEntity(deliveryRequestId = req4Id, name = "Mỹ phẩm", weightKg = 1.0))
        historyDao.insert(StatusHistoryEntity(deliveryRequestId = req4Id, fromStatus = null, toStatus = DeliveryStatus.PENDING, updatedBy = clientId2, timestamp = now - 2 * oneDay))
        historyDao.insert(StatusHistoryEntity(deliveryRequestId = req4Id, fromStatus = DeliveryStatus.PENDING, toStatus = DeliveryStatus.CANCELLED, updatedBy = clientId2, note = "Khách hàng huỷ đơn", timestamp = now - 2 * oneDay + oneHour))
    }
}

