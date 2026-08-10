package com.mob10.deliveryapp.data.repository

import com.mob10.deliveryapp.data.local.dao.DeliveryRequestDao
import com.mob10.deliveryapp.data.local.dao.PackageDao
import com.mob10.deliveryapp.data.local.dao.StatusHistoryDao
import com.mob10.deliveryapp.data.local.entity.DeliveryRequestEntity
import com.mob10.deliveryapp.data.local.entity.PackageEntity
import com.mob10.deliveryapp.data.local.entity.StatusHistoryEntity
import com.mob10.deliveryapp.data.model.DeliveryStatus
import kotlinx.coroutines.flow.Flow

class DeliveryRepository(
    private val requestDao: DeliveryRequestDao,
    private val packageDao: PackageDao,
    private val historyDao: StatusHistoryDao
) {
    val allRequests: Flow<List<DeliveryRequestEntity>> = requestDao.getAllRequests()

    fun getRequestsForClient(clientId: Int) = requestDao.getRequestsByClient(clientId)
    fun getRequestsForDelivery(deliveryId: Int) = requestDao.getRequestsByDelivery(deliveryId)

    suspend fun createRequest(
        clientId: Int,
        distanceKm: Double,
        packages: List<Pair<String, Double>>
    ): Long {
        val totalWeight = packages.sumOf { it.second }
        
        val baseFee = 10000.0
        val distanceFee = distanceKm * 5000.0
        val weightFee = totalWeight * 3000.0
        val totalCost = baseFee + distanceFee + weightFee

        val request = DeliveryRequestEntity(
            clientId = clientId,
            distanceKm = distanceKm,
            baseFee = baseFee,
            distanceFee = distanceFee,
            weightFee = weightFee,
            totalCost = totalCost,
            status = DeliveryStatus.PENDING
        )
        
        val requestId = requestDao.insert(request).toInt()
        
        // Insert packages
        packages.forEach { (name, weight) ->
            packageDao.insert(PackageEntity(deliveryRequestId = requestId, name = name, weightKg = weight))
        }
        
        // Initial history
        historyDao.insert(StatusHistoryEntity(deliveryRequestId = requestId, status = DeliveryStatus.PENDING, note = "Đơn hàng được tạo"))
        
        return requestId.toLong()
    }

    suspend fun updateRequestStatus(requestId: Int, newStatus: DeliveryStatus, note: String? = null): Boolean {
        val currentRequest = requestDao.getRequestById(requestId) ?: return false
        val currentStatus = currentRequest.status
        
        if (isValidTransition(currentStatus, newStatus)) {
            requestDao.updateStatus(requestId, newStatus)
            historyDao.insert(StatusHistoryEntity(deliveryRequestId = requestId, status = newStatus, note = note))
            return true
        }
        return false
    }

    suspend fun acceptRequest(requestId: Int, deliveryId: Int): Boolean {
        val currentRequest = requestDao.getRequestById(requestId) ?: return false
        if (currentRequest.status == DeliveryStatus.PENDING) {
            requestDao.assignToDelivery(requestId, deliveryId, DeliveryStatus.ACCEPTED)
            historyDao.insert(StatusHistoryEntity(deliveryRequestId = requestId, status = DeliveryStatus.ACCEPTED, note = "Nhân viên đã nhận đơn"))
            return true
        }
        return false
    }

    private fun isValidTransition(from: DeliveryStatus, to: DeliveryStatus): Boolean {
        return when (from) {
            DeliveryStatus.PENDING -> to == DeliveryStatus.ACCEPTED || to == DeliveryStatus.CANCELLED
            DeliveryStatus.ACCEPTED -> to == DeliveryStatus.PICKED_UP || to == DeliveryStatus.CANCELLED
            DeliveryStatus.PICKED_UP -> to == DeliveryStatus.IN_TRANSIT
            DeliveryStatus.IN_TRANSIT -> to == DeliveryStatus.DELIVERED
            else -> false // DELIVERED and CANCELLED are terminal states
        }
    }

    suspend fun getRequestHistory(requestId: Int) = historyDao.getHistoryForRequest(requestId)
    suspend fun getRequestPackages(requestId: Int) = packageDao.getPackagesForRequest(requestId)
}
