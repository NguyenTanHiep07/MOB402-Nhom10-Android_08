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

/**
 * Kết quả Accept đơn hàng – giúp UI phân biệt lý do thất bại.
 */
sealed class AcceptResult {
    object Success : AcceptResult()
    object AlreadyTaken : AcceptResult()
    object NotFound : AcceptResult()
    object InvalidStatus : AcceptResult()
}

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

// Repository quản lý toàn bộ nghiệp vụ đơn giao hàng, tính phí, phân phối và trạng thái
class DeliveryRepository(
    private val db: AppDatabase,
    private val requestDao: DeliveryRequestDao,
    private val packageDao: PackageDao,
    private val historyDao: StatusHistoryDao,
    private val feeRuleDao: FeeRuleDao = db.feeRuleDao()
) {
    // Flow danh sách tất cả các đơn hàng
    val allRequests: Flow<List<DeliveryRequestEntity>> = requestDao.getAllRequests()

    // Flow danh sách đơn hàng đang chờ tiếp nhận
    val pendingRequests: Flow<List<DeliveryRequestEntity>> = requestDao.getPendingRequests()

    // Lấy danh sách đơn hàng theo ID khách hàng
    fun getRequestsForClient(clientId: Int) = requestDao.getRequestsByClient(clientId)

    // Lấy chi tiết đơn hàng của khách hàng cụ thể
    suspend fun getRequestByIdForClient(requestId: Int, clientId: Int) =
        requestDao.getRequestByIdForClient(requestId, clientId)

    // Lấy danh sách đơn hàng được gán cho tài xế
    fun getRequestsForDelivery(deliveryId: Int) = requestDao.getRequestsByDelivery(deliveryId)

    // Đếm tổng số lượng đơn hàng
    fun getTotalCount() = requestDao.getTotalCount()

    // Đếm số lượng đơn hàng đang chờ nhận
    fun getPendingCount() = requestDao.getPendingCount()

    // Đếm số đơn đang hoạt động của khách
    fun getActiveCountForClient(clientId: Int) = requestDao.getActiveCountForClient(clientId)

    // Đếm số đơn đã giao thành công của khách
    fun getDeliveredCountForClient(clientId: Int) = requestDao.getDeliveredCountForClient(clientId)

    // Đếm số đơn tài xế đã giao trong ngày hôm nay
    fun getDeliveredTodayCountForDriver(deliveryId: Int, startOfDay: Long) =
        requestDao.getDeliveredTodayCountForDriver(deliveryId, startOfDay)

    // Flow quy tắc tính phí đang áp dụng
    fun getActiveFeeRule(): Flow<FeeRuleEntity?> = feeRuleDao.getActiveFeeRule()

    // Lấy quy tắc tính phí đang áp dụng (đồng bộ)
    suspend fun getActiveFeeRuleSync(): FeeRuleEntity? = feeRuleDao.getActiveFeeRuleSync()

    // Flow toàn bộ lịch sử quy tắc tính phí
    fun getAllFeeRules(): Flow<List<FeeRuleEntity>> = feeRuleDao.getAllFeeRules()

    // Tính toán phí giao hàng dự kiến dựa trên FeeRule đang kích hoạt hoặc bảng giá mặc định
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

    // Tạo đơn hàng mới nguyên tử (Request, Packages, StatusHistory) trong Room Transaction
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

        val activeRule = feeRuleDao.getActiveFeeRuleSync()
        val baseFee = activeRule?.baseFee ?: 15_000.0
        val distanceFee = distanceKm * (activeRule?.pricePerKm ?: 5_000.0)
        val weightFee = totalWeight * (activeRule?.pricePerKg ?: 3_000.0)
        // This existing field stores the combined optional-service charge.
        val fragileCharge = (if (hasFragile) (activeRule?.fragileFee ?: 5_000.0) else 0.0) + (if (hasExpress) 10_000.0 else 0.0)
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

    // Cập nhật trạng thái giao hàng của tài xế và ghi lịch sử
    suspend fun updateRequestStatus(
        requestId: Int,
        newStatus: DeliveryStatus,
        updatedBy: Int,              // bỏ "?" và "= null"
        note: String? = null
    ): Boolean = db.withTransaction {
        val currentRequest = requestDao.getRequestById(requestId) ?: return@withTransaction false
        val currentStatus = currentRequest.status

        if (!isValidTransition(currentStatus, newStatus)) return@withTransaction false

        val ownerId = currentRequest.deliveryPersonId ?: return@withTransaction false
        if (ownerId != updatedBy) return@withTransaction false   // bỏ check "updatedBy == null"

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

    // Tài xế nhận đơn hàng (chống race condition)
    suspend fun acceptRequest(requestId: Int, deliveryPersonId: Int): AcceptResult = db.withTransaction {
        val currentRequest = requestDao.getRequestById(requestId)
            ?: return@withTransaction AcceptResult.NotFound

        if (currentRequest.status != DeliveryStatus.CHO_TIEP_NHAN) {
            return@withTransaction AcceptResult.InvalidStatus
        }

        // Atomic update: WHERE ... AND deliveryPersonId IS NULL AND status = 'CHO_TIEP_NHAN'
        // Nếu rowsAffected == 0 → đơn đã bị driver khác nhận trước.
        val rowsAffected = requestDao.assignToDelivery(requestId, deliveryPersonId, DeliveryStatus.DA_CHAP_NHAN)
        if (rowsAffected == 0) {
            return@withTransaction AcceptResult.AlreadyTaken
        }

        historyDao.insert(
            StatusHistoryEntity(
                deliveryRequestId = requestId,
                fromStatus = DeliveryStatus.CHO_TIEP_NHAN,
                toStatus = DeliveryStatus.DA_CHAP_NHAN,
                updatedBy = deliveryPersonId,
                note = "Tài xế đã nhận đơn"
            )
        )
        AcceptResult.Success
    }

    // Khách hàng hủy đơn hàng của chính mình
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

    // Kiểm tra tính hợp lệ của bước chuyển đổi trạng thái đơn
    private fun isValidTransition(from: DeliveryStatus, to: DeliveryStatus): Boolean {
        return when (from) {
            DeliveryStatus.CHO_TIEP_NHAN -> to == DeliveryStatus.DA_CHAP_NHAN || to == DeliveryStatus.DA_HUY
            DeliveryStatus.DA_CHAP_NHAN -> to == DeliveryStatus.DA_DEN_NHA_HANG || to == DeliveryStatus.DA_HUY
            DeliveryStatus.DA_DEN_NHA_HANG -> to == DeliveryStatus.DA_LAY_HANG
            DeliveryStatus.DA_LAY_HANG -> to == DeliveryStatus.DANG_VAN_CHUYEN
            DeliveryStatus.DANG_VAN_CHUYEN -> to == DeliveryStatus.DA_DEN_KHACH_HANG
            DeliveryStatus.DA_DEN_KHACH_HANG -> to == DeliveryStatus.DA_GIAO
            else -> false // DA_GIAO and DA_HUY are terminal states
        }
    }

    // Đếm số đơn hoàn thành của tài xế (Flow)
    fun getCompletedCountForDriver(driverId: Int) = requestDao.getCompletedCountForDriverFlow(driverId)

    // Đếm số đơn bị hủy của tài xế (Flow)
    fun getCancelledCountForDriver(driverId: Int) = requestDao.getCancelledCountForDriverFlow(driverId)

    // Lấy danh sách lịch sử trạng thái của đơn
    suspend fun getRequestHistory(requestId: Int) = historyDao.getHistoryForRequest(requestId)

    // Lấy danh sách kiện hàng của đơn
    suspend fun getRequestPackages(requestId: Int) = packageDao.getPackagesForRequest(requestId)

    // Lấy thông tin đơn hàng theo ID
    suspend fun getRequestById(requestId: Int) = requestDao.getRequestById(requestId)
}

// Kết quả của thao tác khách hàng hủy đơn
sealed class CancelResult {
    data object Success : CancelResult()
    data object NotOwnerOrNotFound : CancelResult()
    data object StatusChanged : CancelResult()
}
