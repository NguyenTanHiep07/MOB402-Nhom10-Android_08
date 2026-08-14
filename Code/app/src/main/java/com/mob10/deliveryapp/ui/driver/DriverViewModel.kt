package com.mob10.deliveryapp.ui.driver

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mob10.deliveryapp.data.local.AppDatabase
import com.mob10.deliveryapp.data.local.entity.DeliveryRequestEntity
import com.mob10.deliveryapp.data.local.entity.PackageEntity
import com.mob10.deliveryapp.data.model.DeliveryStatus
import com.mob10.deliveryapp.data.repository.DeliveryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DriverUiState(
    val newOrders: List<DeliveryRequestEntity> = emptyList(),
    val activeOrders: List<DeliveryRequestEntity> = emptyList(),
    val packagesByOrder: Map<Int, List<PackageEntity>> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class DriverViewModel(private val repository: DeliveryRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(DriverUiState(isLoading = true))
    val uiState: StateFlow<DriverUiState> = _uiState.asStateFlow()

    fun loadDriverData(driverId: Int) {
        viewModelScope.launch {
            repository.allRequests.collect { allRequests ->
                val newOrders = allRequests.filter { it.status == DeliveryStatus.CHO_TIEP_NHAN }
                val activeOrders = allRequests.filter {
                    it.deliveryPersonId == driverId && it.status in listOf(
                        DeliveryStatus.DA_CHAP_NHAN,
                        DeliveryStatus.DA_DEN_NHA_HANG,
                        DeliveryStatus.DA_LAY_HANG,
                        DeliveryStatus.DA_DEN_KHACH_HANG
                    )
                }

                // Load packages for these orders
                val packagesMap = mutableMapOf<Int, List<PackageEntity>>()
                val relevantOrders = newOrders + activeOrders
                for (order in relevantOrders) {
                    val packages = repository.getRequestPackages(order.id)
                    packagesMap[order.id] = packages
                }

                _uiState.value = _uiState.value.copy(
                    newOrders = newOrders,
                    activeOrders = activeOrders,
                    packagesByOrder = packagesMap,
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

    fun updateOrderStatus(orderId: Int, newStatus: DeliveryStatus, note: String = "") {
        viewModelScope.launch {
            repository.updateRequestStatus(orderId, newStatus, note = note)
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
