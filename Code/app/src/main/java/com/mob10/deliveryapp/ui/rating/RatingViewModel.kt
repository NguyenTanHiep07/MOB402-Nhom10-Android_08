package com.mob10.deliveryapp.ui.rating

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mob10.deliveryapp.data.remote.RetrofitClient
import com.mob10.deliveryapp.data.repository.RatingRepository
import com.mob10.deliveryapp.data.util.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RatingUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val alreadyRated: Boolean = false,
    val submitSuccess: Boolean = false,
    val errorMessage: String? = null
)

class RatingViewModel(private val repository: RatingRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(RatingUiState())
    val uiState: StateFlow<RatingUiState> = _uiState.asStateFlow()

    fun checkExistingRating(deliveryRequestId: Long) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = repository.getExistingRating(deliveryRequestId)) {
                is NetworkResult.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    alreadyRated = true
                )
                is NetworkResult.Error -> {
                    // 404 RATING_NOT_FOUND = chưa đánh giá, đây là trạng thái hợp lệ
                    if (result.code == "RATING_NOT_FOUND" || result.httpCode == 404) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            alreadyRated = false
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
                is NetworkResult.Empty -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    alreadyRated = false
                )
                is NetworkResult.Loading -> { /* no-op */ }
            }
        }
    }

    fun submitRating(
        deliveryRequestId: Long,
        clientId: Long,
        driverId: Long,
        stars: Int,
        comment: String?
    ) {
        if (stars !in 1..5) {
            _uiState.value = _uiState.value.copy(errorMessage = "Vui lòng chọn từ 1 đến 5 sao.")
            return
        }
        _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = repository.submitRating(deliveryRequestId, clientId, driverId, stars, comment)) {
                is NetworkResult.Success -> _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    submitSuccess = true,
                    alreadyRated = true
                )
                is NetworkResult.Error -> _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    errorMessage = result.message
                )
                is NetworkResult.Empty -> _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    errorMessage = "Không nhận được phản hồi từ máy chủ."
                )
                is NetworkResult.Loading -> { /* no-op */ }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

/**
 * Factory sử dụng RetrofitClient chung — không cần ApiClient riêng.
 * Yêu cầu: RetrofitClient.init(context) phải được gọi trước khi tạo ViewModel.
 */
class RatingViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RatingViewModel::class.java)) {
            val repository = RatingRepository(RetrofitClient.ratingApi)
            return RatingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}