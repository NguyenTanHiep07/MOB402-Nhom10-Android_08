package com.mob10.deliveryapp.data.repository

import androidx.room.withTransaction
import com.mob10.deliveryapp.data.local.AppDatabase
import com.mob10.deliveryapp.data.local.dao.DeliveryRequestDao
import com.mob10.deliveryapp.data.local.dao.FeeRuleDao
import com.mob10.deliveryapp.data.local.dao.PackageDao
import com.mob10.deliveryapp.data.local.dao.StatusHistoryDao
import com.mob10.deliveryapp.data.local.entity.DeliveryRequestEntity
import com.mob10.deliveryapp.data.local.entity.FeeRuleEntity
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
    val isFragile: Boolean = false,
    val isExpress: Boolean = false
)

data class CalculatedFeeResult(
    val baseFee: Double,
    val distanceFee: Double,
    val weightFee: Double,
    val fragileCharge: Double,
    val totalCost: Double,
    val appliedRuleId: Int? = null
)

class DeliveryRepository(
    private val db: AppDatabase,
    private val requestDao: DeliveryRequestDao,
    private val packageDao: PackageDao,
    private val historyDao: StatusHistoryDao,
    private val feeRuleDao: FeeRuleDao = db.feeRuleDao()
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

    // Fee Rule Queries
    fun getActiveFeeRule(): Flow<FeeRuleEntity?> = feeRuleDao.getActiveFeeRule()
    suspend fun getActiveFeeRuleSync(): FeeRuleEntity? = feeRuleDao.getActiveFeeRuleSync()
    fun getAllFeeRules(): Flow<List<FeeRuleEntity>> = feeRuleDao.getAllFeeRules()

    /**
     * Tính toán phí giao hàng dự kiến dựa trên FeeRule đang kích hoạt hoặc bảng giá mặc định
     */
    suspend fun calculateEstimatedFee(
        distanceKm: Double,
        weightKg: Double,
        isFragile: Boolean = false,
        customFeeRule: FeeRuleEntity? = null
    ): CalculatedFeeResult {
        val rule = customFeeRule ?: feeRuleDao.getActiveFeeRuleSync()
        val baseFee = rule?.baseFee ?: 15_000.0
        val pricePerKm = rule?.pricePerKm ?: 5_000.0
        val pricePerKg = rule?.pricePerKg ?: 3_000.0
        val fragileFee = rule?.fragileFee ?: 5_000.0

        val distanceFee = distanceKm * pricePerKm
        val weightFee = weightKg * pricePerKg
        val fragileCharge = if (isFragile) fragileFee else 0.0
        val totalCost = baseFee + distanceFee + weightFee + fragileCharge

        return CalculatedFeeResult(
            baseFee = baseFee,
            distanceFee = distanceFee,
            weightFee = weightFee,
            fragileCharge = fragileCharge,
            totalCost = totalCost,
            appliedRuleId = rule?.id
        )
    }

    /**
     * Tạo đơn hàng mới – toàn bộ 3 thao tác:
     * 1. Tạo DeliveryRequestEntity với trạng thái ban đầu CHO_TIEP_NHAN
     * 2. Tạo các PackageEntity thuộc về đơn
     * 3. Tạo StatusHistoryEntity ban đầu (fromStatus = null, toStatus = CHO_TIEP_NHAN)
     * được thực thi nguyên tử trong một Room Database Transaction (withTransaction).
     * Nếu có bất kỳ lỗi nào, toàn bộ dữ liệu sẽ tự động rollback.
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
        val hasExpress = packages.any { it.isExpress }

        val baseFee = 10_000.0
        val distanceFee = distanceKm * 5_000.0
        val weightFee = totalWeight * 3_000.0
        // This existing field stores the combined optional-service charge.
        val fragileCharge = (if (hasFragile) 5_000.0 else 0.0) + (if (hasExpress) 10_000.0 else 0.0)
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
            pricingRuleId = pricingRuleId ?: activeRule?.id,
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
    suspend fun getRequestById(requestId: Int) = requestDao.getRequestById(requestId)
}
