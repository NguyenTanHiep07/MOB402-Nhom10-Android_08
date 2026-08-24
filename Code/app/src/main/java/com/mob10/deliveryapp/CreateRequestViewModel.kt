package com.mob10.deliveryapp

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CreateRequestUiState(
    val senderName: String = "",
    val senderPhone: String = "",
    val pickupAddress: String = "",
    val receiverName: String = "",
    val receiverPhone: String = "",
    val deliveryAddress: String = "",
    val weight: String = "",
    val distanceKm: String = "",
    val selectedService: String = PackageType.STANDARD.displayName,
    val feeQuote: FeeQuote = FeeQuote(),
    val isFormValid: Boolean = false,
    val formError: String? = null
)

class CreateRequestViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CreateRequestUiState())
    val uiState: StateFlow<CreateRequestUiState> = _uiState.asStateFlow()

    fun onSenderNameChanged(value: String) = update { copy(senderName = value) }
    fun onSenderPhoneChanged(value: String) = update { copy(senderPhone = value) }
    fun onPickupAddressChanged(value: String) = update { copy(pickupAddress = value) }
    fun onReceiverNameChanged(value: String) = update { copy(receiverName = value) }
    fun onReceiverPhoneChanged(value: String) = update { copy(receiverPhone = value) }
    fun onDeliveryAddressChanged(value: String) = update { copy(deliveryAddress = value) }
    fun onWeightChanged(value: String) = update { copy(weight = value) }
    fun onDistanceChanged(value: String) = update { copy(distanceKm = value) }
    fun onServiceSelected(value: String) = update { copy(selectedService = value) }

    fun validateForm(): Boolean {
        val state = _uiState.value
        val weight = state.weight.toDoubleOrNull()
        val distance = state.distanceKm.toDoubleOrNull()
        val error = when {
            state.senderName.isBlank() || state.receiverName.isBlank() -> "Vui lòng nhập họ tên người gửi và người nhận."
            !FeeCalculatorEngine.isValidPhone(state.senderPhone) || !FeeCalculatorEngine.isValidPhone(state.receiverPhone) -> "Số điện thoại chưa hợp lệ."
            state.pickupAddress.isBlank() || state.deliveryAddress.isBlank() -> "Vui lòng nhập đủ địa chỉ lấy và giao hàng."
            weight == null || !weight.isFinite() || weight <= 0 -> "Trọng lượng phải lớn hơn 0."
            distance == null || !distance.isFinite() || distance <= 0 -> "Khoảng cách phải lớn hơn 0."
            else -> null
        }
        _uiState.update { it.copy(isFormValid = error == null, formError = error) }
        return error == null
    }

    fun reset() { _uiState.value = CreateRequestUiState() }

    private fun update(change: CreateRequestUiState.() -> CreateRequestUiState) {
        _uiState.update { current ->
            val changed = current.change()
            val weight = changed.weight.toDoubleOrNull() ?: 0.0
            val distance = changed.distanceKm.toDoubleOrNull() ?: 0.0
            val packageType = PackageType.entries.firstOrNull { it.displayName == changed.selectedService }
                ?: PackageType.STANDARD
            changed.copy(
                feeQuote = FeeCalculatorEngine.quote(weight, distance, packageType),
                isFormValid = false,
                formError = null
            )
        }
    }
}
