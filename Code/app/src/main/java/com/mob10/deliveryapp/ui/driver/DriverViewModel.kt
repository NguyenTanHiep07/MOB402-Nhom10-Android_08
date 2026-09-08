package com.mob10.deliveryapp.ui.driver

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mob10.deliveryapp.data.model.DeliveryStatus
import com.mob10.deliveryapp.data.model.DriverStatistics
import com.mob10.deliveryapp.data.model.Order
import com.mob10.deliveryapp.data.model.RejectionReason
import com.mob10.deliveryapp.data.model.RejectInfo
import com.mob10.deliveryapp.data.model.StatusHistory
import com.mob10.deliveryapp.data.remote.RetrofitClient
import com.mob10.deliveryapp.data.repository.ShipperRepository
import com.mob10.deliveryapp.data.util.NetworkResult
import com.mob10.deliveryapp.ui.components.InAppNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class DriverWorkingStatus(val label: String) {
    AVAILABLE("Sẵn sàng nhận đơn"),
    BUSY("Đang bận giao"),
    OFFLINE("Ngoại tuyến")
}

data class DriverUiState(
    val newOrders: List<Order> = emptyList(),
    val activeOrders: List<Order> = emptyList(),
    val historyOrders: List<Order> = emptyList(),
    val historiesByOrder: Map<Int, List<StatusHistory>> = emptyMap(),
    val rejectionReasons: List<RejectionReason> = emptyList(),
    val statistics: DriverStatistics? = null,
    val pendingCount: Int = 0,
    val deliveredTodayCount: Int = 0,
    val totalEarnings: Double = 0.0,
    val todayEarnings: Double = 0.0,
    val completedCount: Int = 0,
    val rejectedCount: Int = 0,
    val reliabilityScore: Double = 100.0,
    val driverStatus: DriverWorkingStatus = DriverWorkingStatus.AVAILABLE,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val actionInProgress: Boolean = false,
    val rejectedOrderId: Int? = null,
    val errorMessage: String? = null,
    val acceptMessage: String? = null,
    val userMessage: String? = null,
    val isConflictError: Boolean = false,      // 409 ORDER_ALREADY_TAKEN
    val isSessionExpired: Boolean = false       // 401 UNAUTHORIZED
)

/**
 * DriverViewModel — chuyển từ Room local sang REST API thật qua ShipperRepository.
 *
 * Thay đổi chính so với phiên bản cũ:
 * - Dùng ShipperRepository thay cho DeliveryRepository (Room)
 * - Data là domain model `Order` thay vì `DeliveryRequestEntity`
 * - Load data bằng API call thay vì Flow/Room observation
 * - Xử lý NetworkResult (Loading/Success/Error) ở mọi thao tác
 * - Hiển thị conflict dialog khi 409 ORDER_ALREADY_TAKEN
 */
class DriverViewModel(private val repository: ShipperRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(DriverUiState(isLoading = true))
    val uiState: StateFlow<DriverUiState> = _uiState.asStateFlow()
    private val _notifications = MutableStateFlow<List<InAppNotification>>(emptyList())
    val notifications: StateFlow<List<InAppNotification>> = _notifications.asStateFlow()
    private var knownOpenOrderIds: Set<Long>? = null
    /**
     * Tải toàn bộ dữ liệu tài xế: đơn chờ, đơn đang giao, thống kê và lý do từ chối.
     * Gọi khi DriverHomeScreen mở lần đầu hoặc khi pull-to-refresh.
     */
    fun loadDriverData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            // Nạp đầy đủ dữ liệu cần cho các tab tài xế.
            loadOpenOrders()
            loadMyOrders()
            loadStatistics()
            loadRejectionReasons()

            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    /** Làm mới dữ liệu. */
    fun refreshData() {
        if (_uiState.value.isLoading || _uiState.value.isRefreshing || _uiState.value.actionInProgress) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, errorMessage = null)
            loadOpenOrders()
            loadMyOrders()
            loadStatistics()
            if (_uiState.value.rejectionReasons.isEmpty()) loadRejectionReasons()
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }

    // ── Danh sách đơn chờ ─────────────────────────────────────

    private suspend fun loadOpenOrders() {
        when (val result = repository.getOpenOrders()) {
            is NetworkResult.Success -> {
                detectNewOpenOrders(result.data)
                _uiState.value = _uiState.value.copy(
                    newOrders = result.data,
                    pendingCount = result.data.size
                )
            }
            is NetworkResult.Empty -> {
                knownOpenOrderIds = emptySet()
                _uiState.value = _uiState.value.copy(
                    newOrders = emptyList(),
                    pendingCount = 0
                )
            }
            is NetworkResult.Error -> handleError(result)
            is NetworkResult.Loading -> { /* ignored */ }
        }
    }

    fun markNotificationsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }
    fun markNotificationRead(id: String) {
        _notifications.value = _notifications.value.map { if (it.id == id) it.copy(isRead = true) else it }
    }

    private fun detectNewOpenOrders(currentOrders: List<Order>) {
        val currentIds = currentOrders.mapTo(mutableSetOf()) { it.id }
        val previous = knownOpenOrderIds
        knownOpenOrderIds = currentIds
        if (previous == null) return
        val newOrders = currentOrders.filter { it.id !in previous }
        if (newOrders.isEmpty()) return
        val additions = newOrders.map { order ->
            InAppNotification(
                id = "driver-open-${order.id}-${order.createdAt}",
                title = "Có đơn giao hàng mới",
                message = "Đơn #GD-${order.id}: ${order.pickupAddress} → ${order.deliveryAddress}.",
                orderId = order.id
            )
        }
        val existingIds = _notifications.value.mapTo(mutableSetOf()) { it.id }
        _notifications.value = (additions.filterNot { it.id in existingIds } + _notifications.value).take(50)
    }

    // ── My Orders ──────────────────────────────────────────────

    private suspend fun loadMyOrders() {
        when (val result = repository.getMyOrders()) {
            is NetworkResult.Success -> {
                val allMyOrders = result.data
                val active = allMyOrders.filter { order ->
                    order.status in listOf(
                        DeliveryStatus.DA_CHAP_NHAN,
                        DeliveryStatus.DA_DEN_NHA_HANG,
                        DeliveryStatus.DA_LAY_HANG, DeliveryStatus.DANG_VAN_CHUYEN, DeliveryStatus.DA_DEN_KHACH_HANG
                    )
                }
                val history = allMyOrders.filter { order ->
                    order.status in listOf(
                        DeliveryStatus.DA_GIAO,
                        DeliveryStatus.DA_HUY
                    )
                }
                val completed = history.filter { it.status == DeliveryStatus.DA_GIAO }
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val completedToday = completed.filter { it.actualDeliveryTime.isToday(todayStr) }
                val historiesByOrder = loadHistories(history)

                _uiState.value = _uiState.value.copy(
                    activeOrders = active,
                    historyOrders = history,
                    historiesByOrder = historiesByOrder,
                    completedCount = completed.size,
                    totalEarnings = completed.sumOf { it.totalCost },
                    deliveredTodayCount = completedToday.size,
                    todayEarnings = completedToday.sumOf { it.totalCost }
                )
            }
            is NetworkResult.Empty -> {
                _uiState.value = _uiState.value.copy(
                    activeOrders = emptyList(),
                    historyOrders = emptyList(),
                    historiesByOrder = emptyMap(),
                    completedCount = 0, totalEarnings = 0.0, deliveredTodayCount = 0, todayEarnings = 0.0
                )
            }
            is NetworkResult.Error -> handleError(result)
            is NetworkResult.Loading -> { /* ignored */ }
        }
    }

    // ── Statistics ──────────────────────────────────────────────

    private suspend fun loadStatistics() {
        when (val result = repository.getMyStatistics()) {
            is NetworkResult.Success -> {
                val stats = result.data
                _uiState.value = _uiState.value.copy(
                    statistics = stats,
                    reliabilityScore = stats.reliabilityScore,
                    rejectedCount = stats.totalRejected,
                    driverStatus = stats.availability?.let { runCatching { DriverWorkingStatus.valueOf(it) }.getOrNull() }
                        ?: _uiState.value.driverStatus
                )
            }
            is NetworkResult.Error -> handleError(result)
            is NetworkResult.Empty -> handleError(
                NetworkResult.Error(message = "Máy chủ chưa có thống kê tài xế.")
            )
            is NetworkResult.Loading -> Unit
        }
    }

    // ── Rejection Reasons ──────────────────────────────────────────────

    private suspend fun loadRejectionReasons() {
        when (val result = repository.getRejectionReasons()) {
            is NetworkResult.Success -> {
                _uiState.value = _uiState.value.copy(rejectionReasons = result.data)
            }
            is NetworkResult.Error -> handleError(result)
            is NetworkResult.Empty -> handleError(
                NetworkResult.Error(message = "Máy chủ chưa cấu hình lý do từ chối.")
            )
            is NetworkResult.Loading -> Unit
        }
    }

    // ── Accept Order ──────────────────────────────────────────────

    fun acceptOrder(orderId: Int) {
        if (_uiState.value.actionInProgress) return
        _uiState.value = _uiState.value.copy(actionInProgress = true)
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            when (val result = repository.acceptOrder(orderId.toLong())) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        acceptMessage = "Nhận đơn #$orderId thành công!",
                        userMessage = "Nhận đơn #$orderId thành công!",
                        isConflictError = false,
                        driverStatus = DriverWorkingStatus.BUSY
                    )
                    // Làm mới dữ liệu để cập nhật danh sách
                    loadOpenOrders()
                    loadMyOrders()
                }
                is NetworkResult.Error -> {
                    if (result.code == "ORDER_ALREADY_TAKEN") {
                        _uiState.value = _uiState.value.copy(
                            acceptMessage = result.message,
                            userMessage = result.message,
                            isConflictError = true
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            acceptMessage = result.message,
                            userMessage = result.message,
                            isConflictError = false
                        )
                    }
                    handleError(result)
                }
                else -> { /* ignored */ }
            }

            _uiState.value = _uiState.value.copy(isLoading = false, actionInProgress = false)
        }
    }

    // ── Reject Order ──────────────────────────────────────────────

    fun rejectOrder(orderId: Int, reason: String = "OTHER", note: String = "") {
        if (_uiState.value.actionInProgress) return
        _uiState.value = _uiState.value.copy(actionInProgress = true, errorMessage = null, rejectedOrderId = null)
        viewModelScope.launch {
            when (val result = repository.rejectOrder(
                orderId = orderId.toLong(),
                reasonCode = reason,
                note = note.ifBlank { null },
            )) {
                is NetworkResult.Success -> {
                    val rejectInfo = result.data
                    val detailMessage = if (rejectInfo.penaltyApplied) {
                        "Đã từ chối đơn #$orderId (có trừ điểm)"
                    } else {
                        "Đã từ chối đơn #$orderId"
                    }
                    _uiState.value = _uiState.value.copy(
                        userMessage = detailMessage,
                        rejectedOrderId = orderId,
                        newOrders = _uiState.value.newOrders.filterNot { it.id.toInt() == orderId }
                    )

                    // Cập nhật statistics nếu có
                    rejectInfo.statistics?.let { stats ->
                        _uiState.value = _uiState.value.copy(
                            statistics = stats,
                            reliabilityScore = stats.reliabilityScore,
                            rejectedCount = stats.totalRejected
                        )
                    }

                    // Làm mới danh sách đơn chờ
                    loadOpenOrders()
                }
                is NetworkResult.Error -> handleError(result)
                else -> { /* ignored */ }
            }
            _uiState.value = _uiState.value.copy(actionInProgress = false)
        }
    }

    // ── Update Order Status ──────────────────────────────────────────────

    fun updateOrderStatus(orderId: Int, newStatus: DeliveryStatus, note: String = "") {
        if (_uiState.value.actionInProgress) return
        _uiState.value = _uiState.value.copy(actionInProgress = true)
        val finalNote = note.ifBlank {
            when (newStatus) {
                DeliveryStatus.DA_CHAP_NHAN -> "Tài xế đã nhận đơn"
                DeliveryStatus.DA_DEN_NHA_HANG -> "Tài xế tới điểm lấy hàng"
                DeliveryStatus.DA_LAY_HANG -> "Đã lấy hàng thành công"
                DeliveryStatus.DANG_VAN_CHUYEN -> "Đang vận chuyển"
                DeliveryStatus.DA_DEN_KHACH_HANG -> "Đã tới điểm giao cho khách"
                DeliveryStatus.DA_GIAO -> "Giao hàng thành công cho khách"
                DeliveryStatus.DA_HUY -> "Đã hủy đơn hàng"
                DeliveryStatus.CHO_TIEP_NHAN -> "Chờ tiếp nhận"
            }
        }

        viewModelScope.launch {
            when (val result = repository.updateOrderStatus(
                orderId = orderId.toLong(),
                newStatus = newStatus,
                note = finalNote
            )) {
                is NetworkResult.Success -> {
                    val statusLabel = when (newStatus) {
                        DeliveryStatus.CHO_TIEP_NHAN -> "Chờ tiếp nhận"
                        DeliveryStatus.DA_CHAP_NHAN -> "Đã nhận đơn"
                        DeliveryStatus.DA_DEN_NHA_HANG -> "Đã đến nhà hàng"
                        DeliveryStatus.DA_LAY_HANG -> "Đã lấy hàng"
                        DeliveryStatus.DANG_VAN_CHUYEN -> "Đang vận chuyển"
                        DeliveryStatus.DA_DEN_KHACH_HANG -> "Đã đến khách hàng"
                        DeliveryStatus.DA_GIAO -> "Đã giao thành công"
                        DeliveryStatus.DA_HUY -> "Đã hủy"
                    }
                    _uiState.value = _uiState.value.copy(
                        userMessage = "Đơn #$orderId: $statusLabel",
                        driverStatus = if (newStatus == DeliveryStatus.DA_GIAO) {
                            DriverWorkingStatus.AVAILABLE
                        } else {
                            _uiState.value.driverStatus
                        }
                    )
                    // Làm mới dữ liệu
                    loadMyOrders()
                    if (newStatus == DeliveryStatus.DA_GIAO) {
                        loadStatistics()
                    }
                }
                is NetworkResult.Error -> handleError(result)
                else -> { /* ignored */ }
            }
            _uiState.value = _uiState.value.copy(actionInProgress = false)
        }
    }

    // ── Working Status (Availability) ──────────────────────────────────────────────

    fun setWorkingStatus(status: DriverWorkingStatus) {
        if (_uiState.value.actionInProgress) return
        _uiState.value = _uiState.value.copy(actionInProgress = true, errorMessage = null)
        viewModelScope.launch {
            try {
            when (val result = repository.updateAvailability(status.name)) {
                is NetworkResult.Success -> {
                    val confirmedStatus = runCatching {
                        DriverWorkingStatus.valueOf(result.data)
                    }.getOrElse {
                        handleError(NetworkResult.Error(message = "Máy chủ trả về trạng thái tài xế không hợp lệ."))
                        return@launch
                    }
                    _uiState.value = _uiState.value.copy(
                        driverStatus = confirmedStatus,
                        userMessage = "Đã chuyển sang: ${confirmedStatus.label}"
                    )
                }
                is NetworkResult.Empty -> handleError(
                    NetworkResult.Error(message = "Máy chủ không xác nhận trạng thái làm việc.")
                )
                is NetworkResult.Error -> handleError(result)
                is NetworkResult.Loading -> { /* ignored */ }
            }
            } finally { _uiState.value = _uiState.value.copy(actionInProgress = false) }
        }
    }

    // ── Clear Messages ──────────────────────────────────────────────

    fun clearAcceptMessage() {
        _uiState.value = _uiState.value.copy(
            acceptMessage = null,
            userMessage = null,
            isConflictError = false
        )
    }

    // ── Error Handling ──────────────────────────────────────────────

    private fun handleError(error: NetworkResult.Error) {
        _uiState.value = _uiState.value.copy(errorMessage = error.message)
        if (error.isUnauthorized) {
            _uiState.value = _uiState.value.copy(
                isSessionExpired = true,
                userMessage = "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại."
            )
        } else {
            _uiState.value = _uiState.value.copy(userMessage = error.message)
        }
    }

    private suspend fun loadHistories(orders: List<Order>): Map<Int, List<StatusHistory>> {
        val result = linkedMapOf<Int, List<StatusHistory>>()
        val previous = _uiState.value
        val previousOrders = previous.historyOrders.associateBy { it.id }
        orders.forEach { order ->
            val cached = previous.historiesByOrder[order.id.toInt()]
            val previousOrder = previousOrders[order.id]
            if (cached != null && previousOrder?.updatedAt == order.updatedAt && previousOrder?.status == order.status) {
                result[order.id.toInt()] = cached
                return@forEach
            }
            when (val historyResult = repository.getOrderHistory(order.id)) {
                is NetworkResult.Success -> result[order.id.toInt()] = historyResult.data
                is NetworkResult.Empty -> result[order.id.toInt()] = emptyList()
                is NetworkResult.Error -> handleError(historyResult)
                is NetworkResult.Loading -> Unit
            }
        }
        return result
    }
}

private fun String?.isToday(todayStr: String): Boolean {
    if (this.isNullOrBlank()) return false
    return runCatching {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.US).apply { isLenient = false }
        val instant = parser.parse(replace(Regex("\\.\\d+(?=Z$)"), "")) ?: return false
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(instant) == todayStr
    }.getOrDefault(false)
}

/**
 * Factory tạo DriverViewModel với ShipperRepository dùng REST API thật.
 */
class DriverViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DriverViewModel::class.java)) {
            RetrofitClient.init(context)
            val repository = ShipperRepository(
                driverApi = RetrofitClient.driverApi,
                orderApi = RetrofitClient.orderApi
            )
            return DriverViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
