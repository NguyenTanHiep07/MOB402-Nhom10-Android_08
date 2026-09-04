package com.mob10.deliveryapp

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mob10.deliveryapp.data.model.Order
import com.mob10.deliveryapp.data.model.StatusHistory
import com.mob10.deliveryapp.data.remote.RetrofitClient
import com.mob10.deliveryapp.data.remote.dto.CreateOrderRequestDto
import com.mob10.deliveryapp.data.remote.dto.PackageInputDto
import com.mob10.deliveryapp.data.repository.ClientOrderRepository
import com.mob10.deliveryapp.data.util.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

fun formatMoney(amount: Long): String = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN")).format(amount)

data class PendingOrderData(
    val senderName: String = "",
    val senderPhone: String = "",
    val senderAddress: String = "",
    val receiverName: String = "",
    val receiverPhone: String = "",
    val receiverAddress: String = "",
    val weightKg: Double = 0.0,
    val distanceKm: Double = 0.0,
    val packageType: PackageType = PackageType.STANDARD,
    val feeQuote: FeeQuote = FeeQuote()
)

data class OrderSubmissionState(
    val isSubmitting: Boolean = false,
    val createdRequestId: Long? = null,
    val errorMessage: String? = null
)

/**
 * OrderViewModel — quản lý luồng tạo đơn và xem đơn hàng của Client qua REST API.
 *
 * Thay đổi từ phiên bản cũ:
 * - Dùng ClientOrderRepository (REST) thay cho DeliveryRepository (Room)
 * - Data dùng domain model `Order` thay vì `DeliveryRequestEntity`
 * - Load data bằng API call thay vì Room Flow
 * - Xử lý NetworkResult (Success/Error/Empty) ở mọi thao tác
 */
class OrderViewModel(
    private val repository: ClientOrderRepository,
    val clientId: Int
) : ViewModel() {
    private val _pendingOrder = MutableStateFlow(PendingOrderData())
    val pendingOrder: StateFlow<PendingOrderData> = _pendingOrder.asStateFlow()

    private val _orderHistory = MutableStateFlow<List<Order>>(emptyList())
    val orderHistory: StateFlow<List<Order>> = _orderHistory.asStateFlow()

    private val _submissionState = MutableStateFlow(OrderSubmissionState())
    val submissionState: StateFlow<OrderSubmissionState> = _submissionState.asStateFlow()

    private val _selectedOrder = MutableStateFlow<Order?>(null)
    val selectedOrder: StateFlow<Order?> = _selectedOrder.asStateFlow()

    private val _selectedHistory = MutableStateFlow<List<StatusHistory>>(emptyList())
    val selectedHistory: StateFlow<List<StatusHistory>> = _selectedHistory.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadOrders()
    }

    /** Load danh sách đơn hàng từ API. */
    fun loadOrders() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = repository.getMyOrders()) {
                is NetworkResult.Success -> _orderHistory.value = result.data
                is NetworkResult.Empty -> _orderHistory.value = emptyList()
                is NetworkResult.Error -> {
                    // Giữ dữ liệu cũ, chỉ log lỗi
                }
                is NetworkResult.Loading -> Unit
            }
            _isLoading.value = false
        }
    }

    fun saveDraftOrder(form: CreateRequestUiState) {
        val packageType = PackageType.entries.firstOrNull { it.displayName == form.selectedService }
            ?: PackageType.STANDARD
        _pendingOrder.value = PendingOrderData(
            senderName = form.senderName,
            senderPhone = form.senderPhone,
            senderAddress = form.pickupAddress,
            receiverName = form.receiverName,
            receiverPhone = form.receiverPhone,
            receiverAddress = form.deliveryAddress,
            weightKg = form.weight.toDoubleOrNull() ?: 0.0,
            distanceKm = form.distanceKm.toDoubleOrNull() ?: 0.0,
            packageType = packageType,
            feeQuote = form.feeQuote
        )
        _submissionState.value = OrderSubmissionState()
    }

    /**
     * Xác nhận đặt đơn — gọi REST API tạo đơn trên backend.
     *
     * Server sẽ tính lại quãng đường và phí từ tọa độ,
     * không tin giá trị distanceKm/fee do client gửi.
     */
    fun confirmOrder() {
        val draft = _pendingOrder.value
        if (draft.weightKg <= 0 || draft.distanceKm <= 0) {
            _submissionState.value = OrderSubmissionState(errorMessage = "Thông tin đơn hàng chưa hợp lệ.")
            return
        }
        viewModelScope.launch {
            _submissionState.value = OrderSubmissionState(isSubmitting = true)

            val request = CreateOrderRequestDto(
                pickupAddress = draft.senderAddress,
                deliveryAddress = draft.receiverAddress,
                pickupLatitude = 0.0,
                pickupLongitude = 0.0,
                deliveryLatitude = 0.0,
                deliveryLongitude = 0.0,
                senderName = draft.senderName,
                senderPhone = draft.senderPhone,
                recipientName = draft.receiverName,
                recipientPhone = draft.receiverPhone,
                distanceKm = draft.distanceKm,
                packages = listOf(
                    PackageInputDto(
                        name = "Kiện hàng",
                        packageType = draft.packageType.displayName,
                        weightKg = draft.weightKg,
                        fragile = draft.packageType == PackageType.FRAGILE,
                        express = draft.packageType == PackageType.EXPRESS
                    )
                )
            )

            when (val result = repository.createOrder(request)) {
                is NetworkResult.Success -> {
                    _submissionState.value = OrderSubmissionState(createdRequestId = result.data.id)
                    // Tự động refresh danh sách đơn
                    loadOrders()
                }
                is NetworkResult.Error -> {
                    _submissionState.value = OrderSubmissionState(errorMessage = result.message)
                }
                is NetworkResult.Empty -> {
                    _submissionState.value = OrderSubmissionState(
                        errorMessage = "Máy chủ không trả về thông tin đơn hàng."
                    )
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun acknowledgeSubmission() { _submissionState.value = OrderSubmissionState() }

    /** Chọn đơn hàng để xem chi tiết — load lịch sử trạng thái từ API. */
    fun selectOrder(order: Order) {
        _selectedOrder.value = order
        viewModelScope.launch {
            when (val result = repository.getOrderHistory(order.id)) {
                is NetworkResult.Success -> _selectedHistory.value = result.data
                is NetworkResult.Empty -> _selectedHistory.value = emptyList()
                is NetworkResult.Error -> _selectedHistory.value = emptyList()
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun clearSelectedOrder() {
        _selectedOrder.value = null
        _selectedHistory.value = emptyList()
    }
}

/**
 * Factory tạo OrderViewModel với ClientOrderRepository dùng REST API thật.
 */
class OrderViewModelFactory(context: Context, private val clientId: Int) : ViewModelProvider.Factory {
    private val appContext = context.applicationContext

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        RetrofitClient.init(appContext)
        return OrderViewModel(
            repository = ClientOrderRepository(RetrofitClient.orderApi),
            clientId = clientId
        ) as T
    }
}
