package com.mob10.deliveryapp.ui.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.mob10.deliveryapp.data.local.entity.DeliveryRequestEntity
import com.mob10.deliveryapp.data.model.DeliveryStatus
import com.mob10.deliveryapp.data.repository.DeliveryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class CustomerViewModel(
    private val deliveryRepository: DeliveryRepository,
    val clientId: Int
) : ViewModel() {

    /** Tất cả đơn của khách */
    private val myRequests: StateFlow<List<DeliveryRequestEntity>> = deliveryRepository
        .getRequestsForClient(clientId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Số đơn đang xử lý (không phải DELIVERED hay CANCELLED) */
    val activeOrderCount: StateFlow<Int> = myRequests
        .map { list ->
            list.count { it.status !in listOf(DeliveryStatus.DA_GIAO, DeliveryStatus.DA_HUY) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /** Số đơn đã hoàn tất */
    val completedOrderCount: StateFlow<Int> = myRequests
        .map { list -> list.count { it.status == DeliveryStatus.DA_GIAO } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /** Đơn gần đây nhất */
    val recentOrder: StateFlow<DeliveryRequestEntity?> = myRequests
        .map { it.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Danh sách đơn đang hoạt động (để hiển thị danh sách) */
    val activeOrders: StateFlow<List<DeliveryRequestEntity>> = myRequests
        .map { list ->
            list.filter { it.status !in listOf(DeliveryStatus.DA_GIAO, DeliveryStatus.DA_HUY) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Toàn bộ đơn (cho màn danh sách đầy đủ, không lọc trạng thái) */
    val allMyOrders: StateFlow<List<DeliveryRequestEntity>> = myRequests

    /** Lấy 1 đơn theo id, có ownership check — dùng cho màn Detail/Current Status */
    suspend fun getOrderDetail(requestId: Int): DeliveryRequestEntity? {
        return deliveryRepository.getRequestByIdForClient(requestId, clientId)
    }

    private val _cancelResult = kotlinx.coroutines.flow.MutableSharedFlow<CancelUiResult>()
    val cancelResult: kotlinx.coroutines.flow.SharedFlow<CancelUiResult> = _cancelResult

    /** Client huỷ đơn — ownership check + conditional update đã xử lý ở Repository */
    fun cancelOrder(requestId: Int) {
        viewModelScope.launch {
            val result = deliveryRepository.cancelRequestByClient(requestId, clientId)
            _cancelResult.emit(
                when (result) {
                    is com.mob10.deliveryapp.data.repository.CancelResult.Success ->
                        CancelUiResult.Success
                    is com.mob10.deliveryapp.data.repository.CancelResult.NotOwnerOrNotFound ->
                        CancelUiResult.Error("Không tìm thấy đơn hàng hoặc bạn không có quyền huỷ đơn này.")
                    is com.mob10.deliveryapp.data.repository.CancelResult.StatusChanged ->
                        CancelUiResult.Error("Đơn hàng vừa được cập nhật, không thể huỷ vào lúc này.")
                }
            )
        }
    }
    sealed class CancelUiResult {
        data object Success : CancelUiResult()
        data class Error(val message: String) : CancelUiResult()
    }

    class CustomerViewModelFactory(
        context: android.content.Context,
        private val clientId: Int
    ) : androidx.lifecycle.ViewModelProvider.Factory {
        private val applicationContext = context.applicationContext

        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CustomerViewModel::class.java)) {
                val database = com.mob10.deliveryapp.data.local.AppDatabase.getDatabase(applicationContext)
                val deliveryRepository = com.mob10.deliveryapp.data.repository.DeliveryRepository(
                    database,
                    database.deliveryRequestDao(),
                    database.packageDao(),
                    database.statusHistoryDao()
                )
                return CustomerViewModel(
                    deliveryRepository = deliveryRepository,
                    clientId = clientId
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
