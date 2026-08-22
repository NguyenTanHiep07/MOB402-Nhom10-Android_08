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
    val quantity: Int = 1,
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
    suspend fun getRequestByIdForClient(requestId: Int, clientId: Int) =
        requestDao.getRequestByIdForClient(requestId, clientId)
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
            status = DeliveryStatus.CHO_TIEP_NHAN
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
                    quantity = pkg.quantity,
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
                toStatus = DeliveryStatus.CHO_TIEP_NHAN,
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

        if (newStatus == DeliveryStatus.DA_GIAO) {
            requestDao.updateStatusWithTime(requestId, newStatus, System.currentTimeMillis())
        } else {
            requestDao.updateStatus(requestId, newStatus)
        }
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
     * Tài xế nhận đơn – phân công tài xế và chuyển CHO_TIEP_NHAN → DA_CHAP_NHAN trong transaction.
     */
    suspend fun acceptRequest(requestId: Int, deliveryPersonId: Int): Boolean = db.withTransaction {
        val currentRequest = requestDao.getRequestById(requestId) ?: return@withTransaction false
        if (currentRequest.status != DeliveryStatus.CHO_TIEP_NHAN) return@withTransaction false

        requestDao.assignToDelivery(requestId, deliveryPersonId, DeliveryStatus.DA_CHAP_NHAN)
        historyDao.insert(
            StatusHistoryEntity(
                deliveryRequestId = requestId,
                fromStatus = DeliveryStatus.CHO_TIEP_NHAN,
                toStatus = DeliveryStatus.DA_CHAP_NHAN,
                updatedBy = deliveryPersonId,
                note = "Tài xế đã nhận đơn"
            )
        )
        true
    }
    /**
     * Client hủy đơn hàng của chính mình.
     * - Kiểm tra đúng chủ đơn (ownership check)
     * - Update có điều kiện để tránh race với Accept của Delivery
     */
    suspend fun cancelRequestByClient(requestId: Int, clientId: Int): CancelResult = db.withTransaction {
        val request = requestDao.getRequestByIdForClient(requestId, clientId)
            ?: return@withTransaction CancelResult.NotOwnerOrNotFound

        val affectedRows = requestDao.cancelRequestConditional(
            requestId = requestId,
            clientId = clientId,
            newStatus = DeliveryStatus.DA_HUY
        )

        if (affectedRows == 0) {
            return@withTransaction CancelResult.StatusChanged
        }

        historyDao.insert(
            StatusHistoryEntity(
                deliveryRequestId = requestId,
                fromStatus = request.status,
                toStatus = DeliveryStatus.DA_HUY,
                updatedBy = clientId,
                note = "Khách hàng hủy đơn"
            )
        )
        CancelResult.Success
    }
    private fun isValidTransition(from: DeliveryStatus, to: DeliveryStatus): Boolean {
        return when (from) {
            DeliveryStatus.CHO_TIEP_NHAN -> to == DeliveryStatus.DA_CHAP_NHAN || to == DeliveryStatus.DA_HUY
            DeliveryStatus.DA_CHAP_NHAN -> to == DeliveryStatus.DA_DEN_NHA_HANG || to == DeliveryStatus.DA_HUY
            DeliveryStatus.DA_DEN_NHA_HANG -> to == DeliveryStatus.DA_LAY_HANG
            DeliveryStatus.DA_LAY_HANG -> to == DeliveryStatus.DA_DEN_KHACH_HANG
            DeliveryStatus.DA_DEN_KHACH_HANG -> to == DeliveryStatus.DA_GIAO
            else -> false // DA_GIAO and DA_HUY are terminal states
        }
    }

    suspend fun getRequestHistory(requestId: Int) = historyDao.getHistoryForRequest(requestId)
    suspend fun getRequestPackages(requestId: Int) = packageDao.getPackagesForRequest(requestId)
}
sealed class CancelResult {
    data object Success : CancelResult()
    data object NotOwnerOrNotFound : CancelResult()
    data object StatusChanged : CancelResult()
}

