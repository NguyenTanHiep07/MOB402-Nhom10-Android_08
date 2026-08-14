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
            val shipper1Id = userDao.getAllUsers().first().find { it.username == "shipper1" }?.id ?: 3
            val request2 = com.mob10.deliveryapp.data.local.entity.DeliveryRequestEntity(
                clientId = 2,
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

