package com.mob10.deliveryapp

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.mob10.deliveryapp.data.model.AddressSuggestion
import com.mob10.deliveryapp.data.remote.RetrofitClient
import com.mob10.deliveryapp.data.repository.LocationRepository
import com.mob10.deliveryapp.data.util.NetworkResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToLong

data class CreateRequestUiState(
    val senderName: String = "", val senderPhone: String = "", val pickupAddress: String = "",
    val receiverName: String = "", val receiverPhone: String = "", val deliveryAddress: String = "",
    val packageName: String = "", val weight: String = "", val distanceKm: String = "",
    val selectedService: String = PackageType.STANDARD.displayName,
    val pickup: AddressSuggestion? = null, val delivery: AddressSuggestion? = null,
    val pickupSuggestions: List<AddressSuggestion> = emptyList(),
    val deliverySuggestions: List<AddressSuggestion> = emptyList(),
    val searchingPickup: Boolean = false, val searchingDelivery: Boolean = false,
    val pickupError: String? = null, val deliveryError: String? = null,
    val isEstimating: Boolean = false, val routeError: String? = null,
    val durationMinutes: Int? = null, val feeQuote: FeeQuote = FeeQuote(),
    val isFormValid: Boolean = false, val formError: String? = null
)

class CreateRequestViewModel(
    private val locations: LocationRepository? = null,
    private val savedState: SavedStateHandle = SavedStateHandle()
) : ViewModel() {
    private val gson = Gson()
    private val _uiState = MutableStateFlow(runCatching {
        gson.fromJson(savedState.get<String>("form"), CreateRequestUiState::class.java)
    }.getOrNull() ?: CreateRequestUiState())
    val uiState = _uiState.asStateFlow()
    private var pickupJob: Job? = null
    private var deliveryJob: Job? = null
    private var quoteJob: Job? = null
    private fun edit(change: CreateRequestUiState.() -> CreateRequestUiState) {
        _uiState.update { it.change().copy(isFormValid = false, formError = null) }; persist()
    }
    private fun persist() {
        savedState["form"] = gson.toJson(_uiState.value.copy(searchingPickup = false, searchingDelivery = false, isEstimating = false))
    }
    fun onSenderNameChanged(value: String) = edit { copy(senderName = value.take(120)) }
    fun onSenderPhoneChanged(value: String) = edit { copy(senderPhone = value.take(15)) }
    fun onReceiverNameChanged(value: String) = edit { copy(receiverName = value.take(120)) }
    fun onReceiverPhoneChanged(value: String) = edit { copy(receiverPhone = value.take(15)) }
    fun onPackageNameChanged(value: String) = edit { copy(packageName = value.take(150)) }
    fun onWeightChanged(value: String) { edit { copy(weight = value.take(12)) }; estimate() }
    fun onServiceSelected(value: String) { edit { copy(selectedService = value) }; estimate() }
    fun onPickupAddressChanged(value: String) {
        edit { copy(pickupAddress = value.take(500), pickup = null, pickupSuggestions = emptyList()) }; estimate(); search(true)
    }
    fun onDeliveryAddressChanged(value: String) {
        edit { copy(deliveryAddress = value.take(500), delivery = null, deliverySuggestions = emptyList()) }; estimate(); search(false)
    }
    fun retryAddress(pickup: Boolean) = search(pickup)
    private fun search(isPickup: Boolean) {
        if (isPickup) pickupJob?.cancel() else deliveryJob?.cancel()
        val query = if (isPickup) _uiState.value.pickupAddress else _uiState.value.deliveryAddress
        edit { if (isPickup) copy(searchingPickup = query.trim().length >= 3, pickupError = null)
            else copy(searchingDelivery = query.trim().length >= 3, deliveryError = null) }
        if (query.trim().length < 3 || locations == null) return
        val job = viewModelScope.launch {
            delay(450)
            val result = locations.autocomplete(query)
            val suggestions = (result as? NetworkResult.Success)?.data.orEmpty()
            val error = when (result) {
                is NetworkResult.Error -> result.message
                is NetworkResult.Empty -> "Không tìm thấy địa chỉ. Thử thêm tên quận hoặc thành phố."
                is NetworkResult.Success -> if (suggestions.isEmpty()) "Không tìm thấy địa chỉ. Thử thêm tên quận hoặc thành phố." else null
                else -> null
            }
            edit { if (isPickup) copy(searchingPickup = false, pickupSuggestions = suggestions, pickupError = error)
                else copy(searchingDelivery = false, deliverySuggestions = suggestions, deliveryError = error) }
        }
        if (isPickup) pickupJob = job else deliveryJob = job
    }
    fun selectAddress(isPickup: Boolean, address: AddressSuggestion) {
        if (isPickup) pickupJob?.cancel() else deliveryJob?.cancel()
        edit { if (isPickup) copy(pickup = address, pickupAddress = address.formattedAddress,
            pickupSuggestions = emptyList(), searchingPickup = false, pickupError = null)
        else copy(delivery = address, deliveryAddress = address.formattedAddress,
            deliverySuggestions = emptyList(), searchingDelivery = false, deliveryError = null) }
        estimate()
    }
    fun estimate() {
        quoteJob?.cancel()
        edit { copy(feeQuote = FeeQuote(), distanceKm = "", durationMinutes = null, routeError = null, isEstimating = false) }
        val form = _uiState.value
        val pickup = form.pickup ?: return
        val delivery = form.delivery ?: return
        val weight = form.weight.toDoubleOrNull()?.takeIf { validWeight(form.weight) } ?: return
        val repository = locations ?: return
        edit { copy(isEstimating = true) }
        quoteJob = viewModelScope.launch {
            delay(450)
            when (val result = repository.estimateRoute(pickup.latitude, pickup.longitude,
                delivery.latitude, delivery.longitude, weight,
                form.selectedService == PackageType.FRAGILE.displayName,
                form.selectedService == PackageType.EXPRESS.displayName)) {
                is NetworkResult.Success -> edit {
                    val q = result.data
                    copy(isEstimating = false, distanceKm = q.distanceKm.toString(), durationMinutes = q.estimatedDurationMinutes,
                        feeQuote = FeeQuote(q.baseFee.roundToLong(), q.distanceFee.roundToLong(), q.weightFee.roundToLong(), q.serviceFee.roundToLong()))
                }
                is NetworkResult.Error -> edit { copy(isEstimating = false, routeError = result.message) }
                else -> edit { copy(isEstimating = false, routeError = "Không nhận được báo giá. Vui lòng thử lại.") }
            }
        }
    }
    fun validateForm(): Boolean {
        val s = _uiState.value
        val weight = s.weight.toDoubleOrNull()
        val error = when {
            s.senderName.isBlank() || s.receiverName.isBlank() -> "Vui lòng nhập tên người gửi và người nhận."
            !FeeCalculatorEngine.isValidPhone(s.senderPhone) || !FeeCalculatorEngine.isValidPhone(s.receiverPhone) -> "Số điện thoại chưa hợp lệ."
            s.pickup == null || s.delivery == null -> "Hãy chọn địa chỉ lấy và giao hàng trong danh sách gợi ý."
            s.packageName.isBlank() -> "Vui lòng mô tả hàng hóa cần giao."
            !validWeight(s.weight) -> "Trọng lượng từ 0,01 đến 99.999.999,99 kg, tối đa hai chữ số thập phân."
            s.isEstimating || s.feeQuote.totalFee <= 0 || s.routeError != null -> "Cần lấy báo giá thành công trước khi xác nhận."
            else -> null
        }
        _uiState.update { it.copy(isFormValid = error == null, formError = error) }; persist()
        return error == null
    }
    fun reset() {
        pickupJob?.cancel(); deliveryJob?.cancel(); quoteJob?.cancel()
        _uiState.value = CreateRequestUiState(); persist()
    }

    private fun validWeight(value: String): Boolean = runCatching {
        val weight = value.toBigDecimal().stripTrailingZeros()
        weight >= java.math.BigDecimal("0.01") && weight <= java.math.BigDecimal("99999999.99") && weight.scale() <= 2
    }.getOrDefault(false)
}

class CreateRequestViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
        CreateRequestViewModel(LocationRepository(RetrofitClient.locationApi), extras.createSavedStateHandle()) as T
}
