package com.mob10.deliveryapp.data.repository

import androidx.room.withTransaction
import com.mob10.deliveryapp.data.local.AppDatabase
import com.mob10.deliveryapp.data.local.dao.DeliveryRequestDao
import com.mob10.deliveryapp.data.local.dao.PackageDao
import com.mob10.deliveryapp.data.local.dao.StatusHistoryDao
import com.mob10.deliveryapp.data.local.entity.DeliveryRequestEntity
import com.mob10.deliveryapp.data.local.entity.PackageEntity
import com.mob10.deliveryapp.data.local.entity.StatusHistoryEntity
import com.mob10.deliveryapp.data.model.DeliveryStatus
import kotlinx.coroutines.flow.Flow

data class NewPackageInfo(
    val name: String,
    val packageType: String? = null,
    val weightKg: Double,
    val notes: String? = null,
    val isFragile: Boolean = false
)

class DeliveryRepository(
    private val db: AppDatabase,
    private val requestDao: DeliveryRequestDao,
    private val packageDao: PackageDao,
    private val historyDao: StatusHistoryDao
) {
    val allRequests: Flow<List<DeliveryRequestEntity>> = requestDao.getAllRequests()
    val pendingRequests: Flow<List<DeliveryRequestEntity>> = requestDao.getPendingRequests()

    fun getRequestsForClient(clientId: Int) = requestDao.getRequestsByClient(clientId)
    fun getRequestsForDelivery(deliveryId: Int) = requestDao.getRequestsByDelivery(deliveryId)
    fun getTotalCount() = requestDao.getTotalCount()
    fun getPendingCount() = requestDao.getPendingCount()
    fun getActiveCountForClient(clientId: Int) = requestDao.getActiveCountForClient(clientId)
    fun getDeliveredCountForClient(clientId: Int) = requestDao.getDeliveredCountForClient(clientId)
    fun getDeliveredTodayCountForDriver(deliveryId: Int, startOfDay: Long) =
        requestDao.getDeliveredTodayCountForDriver(deliveryId, startOfDay)

    /**
     * Tạo đơn hàng mới – toàn bộ 3 thao tác (tạo đơn, tạo kiện hàng, tạo lịch sử ban đầu)
     * được bọc trong cùng một transaction.
     * Nếu bất kỳ bước nào thất bại, tất cả sẽ bị rollback.
     */
    suspend fun createRequest(
        clientId: Int,
        pickupAddress: String,
        deliveryAddress: String,
        senderName: String,
        senderPhone: String,
        recipientName: String,
        recipientPhone: String,
        distanceKm: Double,
        packages: List<NewPackageInfo>,
        pricingRuleId: Int? = null,
        scheduledPickupTime: Long? = null,
        note: String? = null
    ): Long = db.withTransaction {
        val totalWeight = packages.sumOf { it.weightKg }
        val hasFragile = packages.any { it.isFragile }

        val baseFee = 10_000.0
        val distanceFee = distanceKm * 5_000.0
        val weightFee = totalWeight * 3_000.0
        val fragileCharge = if (hasFragile) 5_000.0 else 0.0
        val totalCost = baseFee + distanceFee + weightFee + fragileCharge

        val request = DeliveryRequestEntity(
            clientId = clientId,
            pickupAddress = pickupAddress,
            deliveryAddress = deliveryAddress,
            senderName = senderName,
            senderPhone = senderPhone,
            recipientName = recipientName,
            recipientPhone = recipientPhone,
            distanceKm = distanceKm,
            baseFee = baseFee,
            distanceFee = distanceFee,
            weightFee = weightFee,
            fragileCharge = fragileCharge,
            totalCost = totalCost,
            pricingRuleId = pricingRuleId,
            scheduledPickupTime = scheduledPickupTime,
            note = note,
            status = DeliveryStatus.PENDING
        )

        // 1. Tạo đơn giao hàng
        val requestId = requestDao.insert(request).toInt()

        // 2. Tạo các kiện hàng
        packages.forEach { pkg ->
            packageDao.insert(
                PackageEntity(
                    deliveryRequestId = requestId,
                    name = pkg.name,
                    packageType = pkg.packageType,
                    weightKg = pkg.weightKg,
                    notes = pkg.notes,
                    isFragile = pkg.isFragile
                )
            )
        }

        // 3. Tạo lịch sử trạng thái ban đầu
        historyDao.insert(
            StatusHistoryEntity(
                deliveryRequestId = requestId,
                fromStatus = null,
                toStatus = DeliveryStatus.PENDING,
                updatedBy = clientId,
                note = "Đơn hàng được tạo"
            )
        )

        requestId.toLong()
    }

    /**
     * Cập nhật trạng thái đơn hàng và ghi lịch sử trong cùng một transaction.
     * @return true nếu cập nhật thành công
     */
    suspend fun updateRequestStatus(
        requestId: Int,
        newStatus: DeliveryStatus,
        updatedBy: Int? = null,
        note: String? = null
    ): Boolean = db.withTransaction {
        val currentRequest = requestDao.getRequestById(requestId) ?: return@withTransaction false
        val currentStatus = currentRequest.status

        if (!isValidTransition(currentStatus, newStatus)) return@withTransaction false

        requestDao.updateStatus(requestId, newStatus)
        historyDao.insert(
            StatusHistoryEntity(
                deliveryRequestId = requestId,
                fromStatus = currentStatus,
                toStatus = newStatus,
                updatedBy = updatedBy,
                note = note
            )
        )
        true
    }

    /**
     * Tài xế nhận đơn – phân công tài xế và chuyển PENDING → ACCEPTED trong transaction.
     */
    suspend fun acceptRequest(requestId: Int, deliveryPersonId: Int): Boolean = db.withTransaction {
        val currentRequest = requestDao.getRequestById(requestId) ?: return@withTransaction false
        if (currentRequest.status != DeliveryStatus.PENDING) return@withTransaction false

        requestDao.assignToDelivery(requestId, deliveryPersonId, DeliveryStatus.ACCEPTED)
        historyDao.insert(
            StatusHistoryEntity(
                deliveryRequestId = requestId,
                fromStatus = DeliveryStatus.PENDING,
                toStatus = DeliveryStatus.ACCEPTED,
                updatedBy = deliveryPersonId,
                note = "Tài xế đã nhận đơn"
            )
        )
        true
    }


    private fun isValidTransition(from: DeliveryStatus, to: DeliveryStatus): Boolean {
        return when (from) {
            DeliveryStatus.PENDING -> to == DeliveryStatus.ACCEPTED || to == DeliveryStatus.CANCELLED
            DeliveryStatus.ACCEPTED -> to == DeliveryStatus.PICKED_UP || to == DeliveryStatus.CANCELLED
            DeliveryStatus.PICKED_UP -> to == DeliveryStatus.IN_TRANSIT
            DeliveryStatus.IN_TRANSIT -> to == DeliveryStatus.DELIVERED
            else -> false // DELIVERED và CANCELLED là trạng thái cuối
        }
    }

    suspend fun getRequestHistory(requestId: Int) = historyDao.getHistoryForRequest(requestId)
    suspend fun getRequestPackages(requestId: Int) = packageDao.getPackagesForRequest(requestId)
}
