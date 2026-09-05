package com.mob10.deliveryapp.ui.customer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mob10.deliveryapp.data.model.DeliveryStatus
import com.mob10.deliveryapp.data.model.Order
import com.mob10.deliveryapp.data.remote.RetrofitClient
import com.mob10.deliveryapp.data.repository.ClientOrderRepository
import com.mob10.deliveryapp.data.util.NetworkResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * CustomerViewModel — quản lý danh sách đơn và hủy đơn của Client qua REST API.
 *
 * Thay đổi từ phiên bản cũ:
 * - Dùng ClientOrderRepository (REST) thay cho DeliveryRepository (Room)
 * - Data dùng domain model `Order` thay vì `DeliveryRequestEntity`
 * - Load data bằng API call thay vì Room Flow reactive
 * - Xử lý NetworkResult (Success/Error/Empty) ở mọi thao tác
 */
class CustomerViewModel(
    private val repository: ClientOrderRepository
) : ViewModel() {

    private val _myRequests = MutableStateFlow<List<Order>>(emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /** Số đơn đang xử lý (không phải DELIVERED hay CANCELLED) */
    val activeOrderCount: StateFlow<Int> = _myRequests
        .map { list ->
            list.count { it.status !in listOf(DeliveryStatus.DA_GIAO, DeliveryStatus.DA_HUY) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /** Số đơn đã hoàn tất */
    val completedOrderCount: StateFlow<Int> = _myRequests
        .map { list -> list.count { it.status == DeliveryStatus.DA_GIAO } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /** Đơn gần đây nhất */
    val recentOrder: StateFlow<Order?> = _myRequests
        .map { it.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Danh sách đơn đang hoạt động (để hiển thị danh sách) */
    val activeOrders: StateFlow<List<Order>> = _myRequests
        .map { list ->
            list.filter { it.status !in listOf(DeliveryStatus.DA_GIAO, DeliveryStatus.DA_HUY) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Toàn bộ đơn (cho màn danh sách đầy đủ, không lọc trạng thái) */
    val allMyOrders: StateFlow<List<Order>> = _myRequests

    init {
        loadOrders()
    }

    /** Load danh sách đơn từ REST API. */
    fun loadOrders() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = repository.getMyOrders()) {
                is NetworkResult.Success -> _myRequests.value = result.data
                is NetworkResult.Empty -> _myRequests.value = emptyList()
                is NetworkResult.Error -> {
                    // Giữ dữ liệu cũ nếu có, hiển thị lỗi qua cancelResult
                }
                is NetworkResult.Loading -> Unit
            }
            _isLoading.value = false
        }
    }

    /** Lấy 1 đơn theo id từ REST API — dùng cho màn Detail/Current Status */
    suspend fun getOrderDetail(orderId: Long): Order? {
        return when (val result = repository.getOrderById(orderId)) {
            is NetworkResult.Success -> result.data
            else -> null
        }
    }

    private val _cancelResult = MutableSharedFlow<CancelUiResult>()
    val cancelResult: SharedFlow<CancelUiResult> = _cancelResult

    /** Client huỷ đơn qua REST API — backend kiểm tra ownership + status */
    fun cancelOrder(orderId: Long) {
        viewModelScope.launch {
            when (val result = repository.cancelOrder(orderId)) {
                is NetworkResult.Success -> {
                    _cancelResult.emit(CancelUiResult.Success)
                    // Làm mới danh sách đơn sau khi hủy thành công
                    loadOrders()
                }
                is NetworkResult.Error -> {
                    _cancelResult.emit(CancelUiResult.Error(result.message))
                }
                is NetworkResult.Empty -> {
                    _cancelResult.emit(CancelUiResult.Error("Máy chủ không phản hồi."))
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    sealed class CancelUiResult {
        data object Success : CancelUiResult()
        data class Error(val message: String) : CancelUiResult()
    }

    class CustomerViewModelFactory(
        context: Context
    ) : ViewModelProvider.Factory {
        private val applicationContext = context.applicationContext

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CustomerViewModel::class.java)) {
                RetrofitClient.init(applicationContext)
                val repository = ClientOrderRepository(RetrofitClient.orderApi)
                return CustomerViewModel(
                    repository = repository
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
