package com.mob10.deliveryapp

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.gson.Gson
import com.mob10.deliveryapp.data.model.AddressSuggestion
import com.mob10.deliveryapp.data.model.DeliveryStatus
import kotlinx.coroutines.Job
import com.mob10.deliveryapp.data.model.Order
import com.mob10.deliveryapp.data.model.StatusHistory
import com.mob10.deliveryapp.data.remote.RetrofitClient
import com.mob10.deliveryapp.data.remote.dto.CreateOrderRequestDto
import com.mob10.deliveryapp.data.remote.dto.PackageInputDto
import com.mob10.deliveryapp.data.repository.ClientOrderRepository
import com.mob10.deliveryapp.data.util.NetworkResult
import com.mob10.deliveryapp.ui.components.InAppNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun formatMoney(amount: Long): String = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN")).format(amount)

/** Chuyển mốc ISO/epoch từ backend sang thời gian dễ đọc theo múi giờ của thiết bị. */
fun formatServerTimestamp(value: String?, fallback: String = "Chưa cập nhật"): String {
    if (value.isNullOrBlank()) return fallback
    val normalized = value.replace(Regex("(\\.\\d{3})\\d+(?=Z|[+-]\\d{2}:\\d{2}$)"), "$1")
    val parsed = listOf("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", "yyyy-MM-dd'T'HH:mm:ssXXX")
        .firstNotNullOfOrNull { pattern ->
            runCatching {
                SimpleDateFormat(pattern, Locale.US).apply {
                    isLenient = false
                    timeZone = TimeZone.getTimeZone("UTC")
                }.parse(normalized)
            }.getOrNull()
        }
        ?: value.toLongOrNull()?.let(::Date)
        ?: return fallback
    return SimpleDateFormat("HH:mm • dd/MM/yyyy", Locale.forLanguageTag("vi-VN")).format(parsed)
}

data class PendingOrderData(
    val senderName: String = "",
    val senderPhone: String = "",
    val senderAddress: String = "",
    val receiverName: String = "",
    val receiverPhone: String = "",
    val receiverAddress: String = "",
    val pickup: AddressSuggestion? = null,
    val delivery: AddressSuggestion? = null,
    val packageName: String = "",
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
    val clientId: Int,
    private val savedState: SavedStateHandle = SavedStateHandle()
) : ViewModel() {
    private val _pendingOrder = MutableStateFlow(runCatching {
        Gson().fromJson(savedState.get<String>("pending"), PendingOrderData::class.java)
    }.getOrNull() ?: PendingOrderData())
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
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()
    private val _detailError = MutableStateFlow<String?>(null)
    val detailError = _detailError.asStateFlow()
    private val _detailLoading = MutableStateFlow(false)
    val detailLoading = _detailLoading.asStateFlow()
    private val _isCancelling = MutableStateFlow(false)
    val isCancelling = _isCancelling.asStateFlow()
    private val _notifications = MutableStateFlow<List<InAppNotification>>(emptyList())
    val notifications = _notifications.asStateFlow()
    private var knownOrderStatuses: Map<Long, DeliveryStatus>? = null
    private var loadJob: Job? = null
    private var detailJob: Job? = null

    init {
        loadOrders()
    }

    /** Load danh sách đơn hàng từ API. */
    fun loadOrders(forceRefresh: Boolean = false) {
        if (loadJob?.isActive == true) {
            if (!forceRefresh) return
            loadJob?.cancel()
        }
        loadJob = viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            when (val result = repository.getMyOrders()) {
                is NetworkResult.Success -> {
                    detectClientNotifications(result.data)
                    _orderHistory.value = result.data
                    _selectedOrder.value?.id?.let { id ->
                        result.data.firstOrNull { it.id == id }?.let { updated ->
                            if (updated.status != _selectedOrder.value?.status) selectOrder(updated)
                        }
                    }
                }
                is NetworkResult.Empty -> {
                    if (knownOrderStatuses == null) knownOrderStatuses = emptyMap()
                    _orderHistory.value = emptyList()
                }
                is NetworkResult.Error -> {
                    _errorMessage.value = result.message
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
            pickup = form.pickup,
            delivery = form.delivery,
            packageName = form.packageName,
            weightKg = form.weight.toDoubleOrNull() ?: 0.0,
            distanceKm = form.distanceKm.toDoubleOrNull() ?: 0.0,
            packageType = packageType,
            feeQuote = form.feeQuote
        )
        savedState["pending"] = Gson().toJson(_pendingOrder.value)
        _submissionState.value = OrderSubmissionState()
    }

    /**
     * Xác nhận đặt đơn — gọi REST API tạo đơn trên backend.
     *
     * Server sẽ tính lại quãng đường và phí từ tọa độ,
     * không tin giá trị distanceKm/fee do client gửi.
     */
    fun confirmOrder() {
        if (_submissionState.value.isSubmitting) return
        val draft = _pendingOrder.value
        if (!draft.weightKg.isFinite() || draft.weightKg < 0.01 || !draft.distanceKm.isFinite() || draft.distanceKm <= 0
            || draft.pickup == null || draft.delivery == null || draft.packageName.isBlank()) {
            _submissionState.value = OrderSubmissionState(errorMessage = "Thông tin đơn hàng chưa hợp lệ.")
            return
        }
        viewModelScope.launch {
            _submissionState.value = OrderSubmissionState(isSubmitting = true)

            val request = CreateOrderRequestDto(
                pickupAddress = draft.senderAddress,
                deliveryAddress = draft.receiverAddress,
                pickupLatitude = draft.pickup.latitude,
                pickupLongitude = draft.pickup.longitude,
                deliveryLatitude = draft.delivery.latitude,
                deliveryLongitude = draft.delivery.longitude,
                senderName = draft.senderName,
                senderPhone = draft.senderPhone,
                recipientName = draft.receiverName,
                recipientPhone = draft.receiverPhone,
                distanceKm = draft.distanceKm,
                packages = listOf(
                    PackageInputDto(
                        name = draft.packageName,
                        packageType = draft.packageType.displayName,
                        weightKg = draft.weightKg,
                        fragile = draft.packageType == PackageType.FRAGILE,
                        express = draft.packageType == PackageType.EXPRESS
                    )
                )
            )

            when (val result = repository.createOrder(request)) {
                is NetworkResult.Success -> {
                    savedState.remove<String>("pending")
                    _submissionState.value = OrderSubmissionState(createdRequestId = result.data.id)
                    // Tự động refresh danh sách đơn
                    loadOrders(forceRefresh = true)
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

    fun markNotificationsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }

    fun openNotification(notification: InAppNotification) {
        _notifications.value = _notifications.value.map { if (it.id == notification.id) it.copy(isRead = true) else it }
        _orderHistory.value.firstOrNull { it.id == notification.orderId }?.let(::selectOrder)
    }

    private fun detectClientNotifications(currentOrders: List<Order>) {
        val previous = knownOrderStatuses
        knownOrderStatuses = currentOrders.associate { it.id to it.status }
        if (previous == null) return
        val additions = currentOrders.mapNotNull { order ->
            val oldStatus = previous[order.id] ?: return@mapNotNull null
            if (oldStatus == order.status) return@mapNotNull null
            val message = when (order.status) {
                DeliveryStatus.DA_CHAP_NHAN -> "Tài xế ${order.deliveryPerson?.fullName ?: ""} đã nhận đơn #GD-${order.id}."
                DeliveryStatus.DA_DEN_NHA_HANG -> "Tài xế đã đến điểm lấy của đơn #GD-${order.id}."
                DeliveryStatus.DA_LAY_HANG -> "Kiện hàng của đơn #GD-${order.id} đã được lấy."
                DeliveryStatus.DANG_VAN_CHUYEN -> "Đơn #GD-${order.id} đang được vận chuyển."
                DeliveryStatus.DA_DEN_KHACH_HANG -> "Tài xế đã đến điểm giao đơn #GD-${order.id}."
                DeliveryStatus.DA_GIAO -> "Đơn #GD-${order.id} đã giao thành công. Bạn có thể đánh giá tài xế."
                DeliveryStatus.DA_HUY -> "Đơn #GD-${order.id} đã được hủy."
                DeliveryStatus.CHO_TIEP_NHAN -> "Đơn #GD-${order.id} đang chờ tài xế tiếp nhận."
            }
            InAppNotification(
                id = "client-${order.id}-${order.status}-${order.updatedAt}",
                title = order.status.label(),
                message = message,
                orderId = order.id
            )
        }
        if (additions.isNotEmpty()) {
            val existingIds = _notifications.value.mapTo(mutableSetOf()) { it.id }
            _notifications.value = (additions.filterNot { it.id in existingIds } + _notifications.value).take(50)
        }
    }

    /** Chọn đơn hàng để xem chi tiết — load lịch sử trạng thái từ API. */
    fun selectOrder(order: Order) {
        detailJob?.cancel()
        _selectedOrder.value = order
        _selectedHistory.value = emptyList()
        _detailError.value = null
        _detailLoading.value = true
        detailJob = viewModelScope.launch {
            when (val detail = repository.getOrderById(order.id)) {
                is NetworkResult.Success -> _selectedOrder.value = detail.data
                is NetworkResult.Error -> _detailError.value = detail.message
                else -> Unit
            }
            when (val result = repository.getOrderHistory(order.id)) {
                is NetworkResult.Success -> _selectedHistory.value = result.data
                is NetworkResult.Empty -> _selectedHistory.value = emptyList()
                is NetworkResult.Error -> _detailError.value = result.message
                is NetworkResult.Loading -> Unit
            }
            _detailLoading.value = false
        }
    }

    fun cancelSelectedOrder() {
        val order = _selectedOrder.value ?: return
        if (_isCancelling.value) return
        _isCancelling.value = true
        viewModelScope.launch {
            try {
                when (val result = repository.cancelOrder(order.id)) {
                    is NetworkResult.Success -> {
                        if (_selectedOrder.value?.id == order.id) selectOrder(result.data)
                        loadOrders(forceRefresh = true)
                    }
                    is NetworkResult.Error -> if (_selectedOrder.value?.id == order.id) _detailError.value = result.message
                    else -> _detailError.value = "Không nhận được kết quả hủy đơn. Hãy tải lại để kiểm tra."
                }
            } finally { _isCancelling.value = false }
        }
    }

    fun clearSelectedOrder() {
        detailJob?.cancel()
        _detailError.value = null
        _detailLoading.value = false
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
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        RetrofitClient.init(appContext)
        return OrderViewModel(
            repository = ClientOrderRepository(RetrofitClient.orderApi),
            clientId = clientId,
            savedState = extras.createSavedStateHandle()
        ) as T
    }
}
