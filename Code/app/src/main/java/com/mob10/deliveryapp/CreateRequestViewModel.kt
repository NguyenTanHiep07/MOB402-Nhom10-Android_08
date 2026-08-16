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
    val selectedService: String = "Tiêu chuẩn",
    val calculatedFee: Double = 0.0,
    val isFormValid: Boolean = false
)

class CreateRequestViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CreateRequestUiState())
    val uiState: StateFlow<CreateRequestUiState> = _uiState.asStateFlow()

    fun onSenderNameChanged(value: String) {
        _uiState.update { it.copy(senderName = value) }
        validateForm()
    }

    fun onSenderPhoneChanged(value: String) {
        _uiState.update { it.copy(senderPhone = value) }
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
        _uiState.update { it.copy(receiverPhone = value) }
        validateForm()
    }

    fun onDeliveryAddressChanged(value: String) {
        _uiState.update { it.copy(deliveryAddress = value) }
        validateForm()
    }

    fun onWeightChanged(value: String) {
        _uiState.update { it.copy(weight = value) }
        calculateFee()
        validateForm()
    }

    fun onServiceSelected(service: String) {
        _uiState.update { it.copy(selectedService = service) }
        calculateFee()
    }

    private fun calculateFee() {
        val state = _uiState.value
        val w = state.weight.toDoubleOrNull() ?: 0.0
        if (w > 0) {
            val pkg = when (state.selectedService) {
                "Hàng dễ vỡ" -> PackageType.FRAGILE
                "Hỏa tốc" -> PackageType.EXPRESS
                else -> PackageType.STANDARD
            }
            val fee = FeeCalculatorEngine.calculateFee(
                weightKg = w,
                distanceKm = 10.0,
                packageType = pkg
            )
            _uiState.update { it.copy(calculatedFee = fee) }
        } else {
            _uiState.update { it.copy(calculatedFee = 0.0) }
        }
    }

    private fun validateForm() {
        val state = _uiState.value
        val isValid = state.senderName.isNotBlank() &&
                state.senderPhone.isNotBlank() &&
                state.pickupAddress.isNotBlank() &&
                state.receiverName.isNotBlank() &&
                state.receiverPhone.isNotBlank() &&
                state.deliveryAddress.isNotBlank() &&
                (state.weight.toDoubleOrNull() ?: 0.0) > 0

        _uiState.update { it.copy(isFormValid = isValid) }
    }

    fun createDeliveryRequest() {
        // ViewModel xử lý xong
    }
}