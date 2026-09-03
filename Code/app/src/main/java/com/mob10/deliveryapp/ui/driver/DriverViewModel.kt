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
    /**
     * Load toàn bộ dữ liệu driver: Open Pool + My Orders + Statistics + Rejection Reasons.
     * Gọi khi DriverHomeScreen mở lần đầu hoặc khi pull-to-refresh.
     */
    fun loadDriverData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Nạp đầy đủ dữ liệu cần cho các tab tài xế.
            loadOpenOrders()
            loadMyOrders()
            loadStatistics()
            loadRejectionReasons()

            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    /** Refresh data (ví dụ: pull-to-refresh). */
    fun refreshData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            loadOpenOrders()
            loadMyOrders()
            loadStatistics()
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }

    // ── Open Pool ──────────────────────────────────────────────

    private suspend fun loadOpenOrders() {
        when (val result = repository.getOpenOrders()) {
            is NetworkResult.Success -> {
                _uiState.value = _uiState.value.copy(
                    newOrders = result.data,
                    pendingCount = result.data.size
                )
            }
            is NetworkResult.Empty -> {
                _uiState.value = _uiState.value.copy(
                    newOrders = emptyList(),
                    pendingCount = 0
                )
            }
            is NetworkResult.Error -> handleError(result)
            is NetworkResult.Loading -> { /* ignored */ }
        }
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
                        DeliveryStatus.DA_LAY_HANG,
                        DeliveryStatus.DA_DEN_KHACH_HANG
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
                    historiesByOrder = emptyMap()
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
                    rejectedCount = stats.totalRejected
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
                    // Refresh data để cập nhật danh sách
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

            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    // ── Reject Order ──────────────────────────────────────────────

    fun rejectOrder(orderId: Int, reason: String = "OTHER", note: String = "") {
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
                    _uiState.value = _uiState.value.copy(userMessage = detailMessage)

                    // Cập nhật statistics nếu có
                    rejectInfo.statistics?.let { stats ->
                        _uiState.value = _uiState.value.copy(
                            statistics = stats,
                            reliabilityScore = stats.reliabilityScore,
                            rejectedCount = stats.totalRejected
                        )
                    }

                    // Refresh open orders
                    loadOpenOrders()
                }
                is NetworkResult.Error -> handleError(result)
                else -> { /* ignored */ }
            }
        }
    }

    // ── Update Order Status ──────────────────────────────────────────────

    fun updateOrderStatus(orderId: Int, newStatus: DeliveryStatus, note: String = "") {
        val finalNote = note.ifBlank {
            when (newStatus) {
                DeliveryStatus.DA_CHAP_NHAN -> "Tài xế đã nhận đơn"
                DeliveryStatus.DA_DEN_NHA_HANG -> "Tài xế tới điểm lấy hàng"
                DeliveryStatus.DA_LAY_HANG -> "Đã lấy hàng thành công"
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
                    // Refresh data
                    loadMyOrders()
                    if (newStatus == DeliveryStatus.DA_GIAO) {
                        loadStatistics()
                    }
                }
                is NetworkResult.Error -> handleError(result)
                else -> { /* ignored */ }
            }
        }
    }

    // ── Working Status (Availability) ──────────────────────────────────────────────

    fun setWorkingStatus(status: DriverWorkingStatus) {
        viewModelScope.launch {
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
        orders.forEach { order ->
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
    return this.startsWith(todayStr)
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
