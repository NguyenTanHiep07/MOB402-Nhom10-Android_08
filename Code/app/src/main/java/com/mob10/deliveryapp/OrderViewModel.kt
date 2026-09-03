package com.mob10.deliveryapp

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mob10.deliveryapp.data.local.AppDatabase
import com.mob10.deliveryapp.data.local.entity.DeliveryRequestEntity
import com.mob10.deliveryapp.data.local.entity.StatusHistoryEntity
import com.mob10.deliveryapp.data.repository.DeliveryRepository
import com.mob10.deliveryapp.data.repository.NewPackageInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
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

class OrderViewModel(
    private val repository: DeliveryRepository,
    val clientId: Int
) : ViewModel() {
    private val _pendingOrder = MutableStateFlow(PendingOrderData())
    val pendingOrder: StateFlow<PendingOrderData> = _pendingOrder.asStateFlow()

    val orderHistory: StateFlow<List<DeliveryRequestEntity>> = repository.getRequestsForClient(clientId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _submissionState = MutableStateFlow(OrderSubmissionState())
    val submissionState: StateFlow<OrderSubmissionState> = _submissionState.asStateFlow()

    private val _selectedOrder = MutableStateFlow<DeliveryRequestEntity?>(null)
    val selectedOrder: StateFlow<DeliveryRequestEntity?> = _selectedOrder.asStateFlow()

    private val _selectedHistory = MutableStateFlow<List<StatusHistoryEntity>>(emptyList())
    val selectedHistory: StateFlow<List<StatusHistoryEntity>> = _selectedHistory.asStateFlow()

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

    fun confirmOrder() {
        val draft = _pendingOrder.value
        if (draft.weightKg <= 0 || draft.distanceKm <= 0) {
            _submissionState.value = OrderSubmissionState(errorMessage = "Thông tin đơn hàng chưa hợp lệ.")
            return
        }
        viewModelScope.launch {
            _submissionState.value = OrderSubmissionState(isSubmitting = true)
            runCatching {
                repository.createRequest(
                    clientId = clientId,
                    pickupAddress = draft.senderAddress,
                    deliveryAddress = draft.receiverAddress,
                    senderName = draft.senderName,
                    senderPhone = draft.senderPhone,
                    recipientName = draft.receiverName,
                    recipientPhone = draft.receiverPhone,
                    distanceKm = draft.distanceKm,
                    packages = listOf(
                        NewPackageInfo(
                            name = "Kiện hàng",
                            packageType = draft.packageType.displayName,
                            weightKg = draft.weightKg,
                            isFragile = draft.packageType == PackageType.FRAGILE,
                            isExpress = draft.packageType == PackageType.EXPRESS
                        )
                    )
                )
            }.onSuccess { requestId ->
                _submissionState.value = OrderSubmissionState(createdRequestId = requestId)
            }.onFailure {
                _submissionState.value = OrderSubmissionState(errorMessage = "Không thể tạo đơn. Vui lòng thử lại.")
            }
        }
    }

    fun acknowledgeSubmission() { _submissionState.value = OrderSubmissionState() }

    fun selectOrder(order: DeliveryRequestEntity) {
        _selectedOrder.value = order
        viewModelScope.launch { _selectedHistory.value = repository.getRequestHistory(order.id) }
    }

    fun clearSelectedOrder() {
        _selectedOrder.value = null
        _selectedHistory.value = emptyList()
    }
}

class OrderViewModelFactory(context: Context, private val clientId: Int) : ViewModelProvider.Factory {
    private val appContext = context.applicationContext

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val db = AppDatabase.getDatabase(appContext)
        return OrderViewModel(
            repository = DeliveryRepository(db, db.deliveryRequestDao(), db.packageDao(), db.statusHistoryDao()),
            clientId = clientId
        ) as T
    }
}
