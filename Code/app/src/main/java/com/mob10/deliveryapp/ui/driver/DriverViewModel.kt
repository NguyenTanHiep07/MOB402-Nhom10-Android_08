package com.mob10.deliveryapp.ui.driver

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mob10.deliveryapp.data.local.AppDatabase
import com.mob10.deliveryapp.data.local.entity.DeliveryRequestEntity
import com.mob10.deliveryapp.data.local.entity.PackageEntity
import com.mob10.deliveryapp.data.local.entity.StatusHistoryEntity
import com.mob10.deliveryapp.data.model.DeliveryStatus
import com.mob10.deliveryapp.data.repository.DeliveryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar

data class DriverUiState(
    val newOrders: List<DeliveryRequestEntity> = emptyList(),
    val activeOrders: List<DeliveryRequestEntity> = emptyList(),
    val historyOrders: List<DeliveryRequestEntity> = emptyList(),
    val packagesByOrder: Map<Int, List<PackageEntity>> = emptyMap(),
    val historiesByOrder: Map<Int, List<StatusHistoryEntity>> = emptyMap(),
    val pendingCount: Int = 0,
    val deliveredTodayCount: Int = 0,
    val totalEarnings: Double = 0.0,
    val todayEarnings: Double = 0.0,
    val completedCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

class DriverViewModel(private val repository: DeliveryRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(DriverUiState(isLoading = true))
    val uiState: StateFlow<DriverUiState> = _uiState.asStateFlow()

    // Lưu driverId để dùng cho updateOrderStatus
    private var currentDriverId: Int? = null

    fun loadDriverData(driverId: Int) {
        currentDriverId = driverId

        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        viewModelScope.launch {
            combine(
                repository.allRequests,
                repository.getPendingCount(),
                repository.getDeliveredTodayCountForDriver(driverId, startOfDay)
            ) { allRequests, pendingCount, deliveredTodayCount ->
                Triple(allRequests, pendingCount, deliveredTodayCount)
            }.collect { (allRequests, pendingCount, deliveredTodayCount) ->
                val newOrders = allRequests.filter { it.status == DeliveryStatus.CHO_TIEP_NHAN }
                val activeOrders = allRequests.filter {
                    it.deliveryPersonId == driverId && it.status in listOf(
                        DeliveryStatus.DA_CHAP_NHAN,
                        DeliveryStatus.DA_DEN_NHA_HANG,
                        DeliveryStatus.DA_LAY_HANG,
                        DeliveryStatus.DA_DEN_KHACH_HANG
                    )
                }
                val historyOrders = allRequests.filter {
                    it.deliveryPersonId == driverId && it.status in listOf(
                        DeliveryStatus.DA_GIAO,
                        DeliveryStatus.DA_HUY
                    )
                }

                val completedOrders = historyOrders.filter { it.status == DeliveryStatus.DA_GIAO }
                val totalEarnings = completedOrders.sumOf { it.totalCost }
                val todayCompletedOrders = completedOrders.filter {
                    (it.actualDeliveryTime ?: it.createdAt) >= startOfDay
                }
                val todayEarnings = todayCompletedOrders.sumOf { it.totalCost }
                val completedCount = completedOrders.size

                // Load packages and histories for all relevant orders
                val packagesMap = mutableMapOf<Int, List<PackageEntity>>()
                val historiesMap = mutableMapOf<Int, List<StatusHistoryEntity>>()
                val relevantOrders = newOrders + activeOrders + historyOrders
                for (order in relevantOrders) {
                    val packages = repository.getRequestPackages(order.id)
                    packagesMap[order.id] = packages
                    val histories = repository.getRequestHistory(order.id)
                    historiesMap[order.id] = histories
                }

                _uiState.value = _uiState.value.copy(
                    newOrders = newOrders,
                    activeOrders = activeOrders,
                    historyOrders = historyOrders,
                    packagesByOrder = packagesMap,
                    historiesByOrder = historiesMap,
                    pendingCount = pendingCount,
                    deliveredTodayCount = deliveredTodayCount,
                    totalEarnings = totalEarnings,
                    todayEarnings = todayEarnings,
                    completedCount = completedCount,
                    isLoading = false
                )
            }
        }
    }

    fun acceptOrder(orderId: Int, driverId: Int) {
        viewModelScope.launch {
            repository.acceptRequest(orderId, driverId)
        }
    }

    fun rejectOrder(orderId: Int) {
        // Simple implementation: for now we don't do anything because "rejecting" just means the driver ignores it
        // and it remains in the pool for others.
    }

    fun updateOrderStatus(orderId: Int, newStatus: DeliveryStatus, driverId: Int? = currentDriverId, note: String = "") {
        val finalNote = note.ifBlank {
            when (newStatus) {
                DeliveryStatus.DA_CHAP_NHAN -> "Tài xế đã nhận đơn"
                DeliveryStatus.DA_DEN_NHA_HANG -> "Tài xế tới điểm lấy hàng"
                DeliveryStatus.DA_LAY_HANG -> "Đã lấy hàng thành công"
                DeliveryStatus.DA_DEN_KHACH_HANG -> "Đã tới điểm giao cho khách"
                DeliveryStatus.DA_GIAO -> "Giao hàng thành công cho khách"
                DeliveryStatus.DA_HUY -> "Đã hủy đơn hàng"
                else -> "Cập nhật trạng thái"
            }
        }
        viewModelScope.launch {
            repository.updateRequestStatus(orderId, newStatus, updatedBy = driverId, note = finalNote)
        }
    }
}

class DriverViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DriverViewModel::class.java)) {
            val db = AppDatabase.getDatabase(context)
            val repository = DeliveryRepository(
                db,
                db.deliveryRequestDao(),
                db.packageDao(),
                db.statusHistoryDao()
            )
            return DriverViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

