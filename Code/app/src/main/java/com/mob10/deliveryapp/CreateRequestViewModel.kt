package com.mob10.deliveryapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mob10.deliveryapp.data.local.entity.FeeRuleEntity
import com.mob10.deliveryapp.data.repository.CalculatedFeeResult
import com.mob10.deliveryapp.data.repository.DeliveryRepository
import com.mob10.deliveryapp.data.repository.NewPackageInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateRequestUiState(
    val senderName: String = "",
    val senderPhone: String = "",
    val pickupAddress: String = "",
    val receiverName: String = "",
    val receiverPhone: String = "",
    val deliveryAddress: String = "",
    val packageName: String = "Kiện hàng",
    val weight: String = "",
    val distanceKm: Double = 5.0,
    val selectedService: String = "Tiêu chuẩn",
    val isFragile: Boolean = false,
    val note: String = "",
    val feeResult: CalculatedFeeResult = CalculatedFeeResult(0.0, 0.0, 0.0, 0.0, 0.0),
    val calculatedFee: Double = 0.0,
    val isFormValid: Boolean = false,
    val senderPhoneError: String? = null,
    val receiverPhoneError: String? = null,
    val weightError: String? = null,
    val isSubmitting: Boolean = false,
    val submissionResult: Result<Long>? = null
)

class CreateRequestViewModel(
    private val repository: DeliveryRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateRequestUiState())
    val uiState: StateFlow<CreateRequestUiState> = _uiState.asStateFlow()

    private var activeFeeRule: FeeRuleEntity? = null

    init {
        viewModelScope.launch {
            repository?.getActiveFeeRule()?.collect { rule ->
                activeFeeRule = rule
                calculateFee()
            }
        }
    }

    fun onSenderNameChanged(value: String) {
        _uiState.update { it.copy(senderName = value) }
        validateForm()
    }

    fun onSenderPhoneChanged(value: String) {
        val error = if (value.isNotBlank() && !FeeCalculatorEngine.isValidPhone(value)) {
            "Số điện thoại không hợp lệ (VD: 0901234567)"
        } else null
        _uiState.update { it.copy(senderPhone = value, senderPhoneError = error) }
        validateForm()
    }

    fun onPickupAddressChanged(value: String) {
        _uiState.update { it.copy(pickupAddress = value) }
        validateForm()
    }

    fun onReceiverNameChanged(value: String) {
        _uiState.update { it.copy(receiverName = value) }
        validateForm()
    }

    fun onReceiverPhoneChanged(value: String) {
        val error = if (value.isNotBlank() && !FeeCalculatorEngine.isValidPhone(value)) {
            "Số điện thoại không hợp lệ (VD: 0987654321)"
        } else null
        _uiState.update { it.copy(receiverPhone = value, receiverPhoneError = error) }
        validateForm()
    }

    fun onDeliveryAddressChanged(value: String) {
        _uiState.update { it.copy(deliveryAddress = value) }
        validateForm()
    }

    fun onPackageNameChanged(value: String) {
        _uiState.update { it.copy(packageName = value) }
        validateForm()
    }

    fun onWeightChanged(value: String) {
        val w = value.toDoubleOrNull()
        val error = if (value.isNotBlank() && (w == null || w <= 0.0)) {
            "Khối lượng phải lớn hơn 0 kg"
        } else null
        _uiState.update { it.copy(weight = value, weightError = error) }
        calculateFee()
        validateForm()
    }

    fun onDistanceChanged(distance: Double) {
        _uiState.update { it.copy(distanceKm = distance) }
        calculateFee()
        validateForm()
    }

    fun onServiceSelected(service: String) {
        val isFragile = service == "Hàng dễ vỡ"
        _uiState.update { it.copy(selectedService = service, isFragile = isFragile) }
        calculateFee()
    }

    fun onNoteChanged(value: String) {
        _uiState.update { it.copy(note = value) }
    }

    private fun calculateFee() {
        val state = _uiState.value
        val w = state.weight.toDoubleOrNull() ?: 0.0
        if (w > 0.0) {
            val feeResult = FeeCalculatorEngine.calculateDetailedFee(
                weightKg = w,
                distanceKm = state.distanceKm,
                isFragile = state.isFragile,
                feeRule = activeFeeRule
            )
            _uiState.update {
                it.copy(
                    feeResult = feeResult,
                    calculatedFee = feeResult.totalCost
                )
            }
        } else {
            val emptyResult = CalculatedFeeResult(0.0, 0.0, 0.0, 0.0, 0.0)
            _uiState.update { it.copy(feeResult = emptyResult, calculatedFee = 0.0) }
        }
    }

    private fun validateForm() {
        val state = _uiState.value
        val w = state.weight.toDoubleOrNull() ?: 0.0
        val isValid = FeeCalculatorEngine.isValidName(state.senderName) &&
                FeeCalculatorEngine.isValidPhone(state.senderPhone) &&
                FeeCalculatorEngine.isValidAddress(state.pickupAddress) &&
                FeeCalculatorEngine.isValidName(state.receiverName) &&
                FeeCalculatorEngine.isValidPhone(state.receiverPhone) &&
                FeeCalculatorEngine.isValidAddress(state.deliveryAddress) &&
                FeeCalculatorEngine.isValidWeight(w)

        _uiState.update { it.copy(isFormValid = isValid) }
    }

    fun submitDeliveryRequest(
        clientId: Int,
        onSuccess: (Long) -> Unit,
        onError: (String) -> Unit
    ) {
        val state = _uiState.value
        if (!state.isFormValid) {
            onError("Vui lòng kiểm tra lại thông tin nhập")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            try {
                val repo = repository
                if (repo != null) {
                    val weightKg = state.weight.toDoubleOrNull() ?: 1.0
                    val packages = listOf(
                        NewPackageInfo(
                            name = state.packageName.ifBlank { "Kiện hàng" },
                            packageType = state.selectedService,
                            weightKg = weightKg,
                            quantity = 1,
                            notes = state.note.ifBlank { null },
                            isFragile = state.isFragile
                        )
                    )
                    val requestId = repo.createRequest(
                        clientId = clientId,
                        pickupAddress = state.pickupAddress,
                        deliveryAddress = state.deliveryAddress,
                        senderName = state.senderName,
                        senderPhone = state.senderPhone,
                        recipientName = state.receiverName,
                        recipientPhone = state.receiverPhone,
                        distanceKm = state.distanceKm,
                        packages = packages,
                        pricingRuleId = activeFeeRule?.id,
                        note = state.note.ifBlank { null }
                    )
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            submissionResult = Result.success(requestId)
                        )
                    }
                    onSuccess(requestId)
                } else {
                    _uiState.update { it.copy(isSubmitting = false) }
                    onSuccess(1L)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        submissionResult = Result.failure(e)
                    )
                }
                onError(e.message ?: "Lỗi tạo đơn hàng")
            }
        }
    }
}