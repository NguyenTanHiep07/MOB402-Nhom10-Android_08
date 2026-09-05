package com.mob10.deliveryapp.ui.rating

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mob10.deliveryapp.data.remote.ApiClient
import com.mob10.deliveryapp.data.repository.RatingFetchResult
import com.mob10.deliveryapp.data.repository.RatingRepository
import com.mob10.deliveryapp.data.repository.RatingSubmitResult
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

    fun checkExistingRating(deliveryRequestId: Int) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = repository.getExistingRating(deliveryRequestId)) {
                is RatingFetchResult.Found -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    alreadyRated = true
                )
                is RatingFetchResult.NotRatedYet -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    alreadyRated = false
                )
                is RatingFetchResult.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.message
                )
            }
        }
    }

    fun submitRating(
        deliveryRequestId: Int,
        clientId: Int,
        driverId: Int,
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
                is RatingSubmitResult.Success -> _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    submitSuccess = true,
                    alreadyRated = true
                )
                is RatingSubmitResult.Error -> _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    errorMessage = result.message
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

class RatingViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RatingViewModel::class.java)) {
            val repository = RatingRepository(ApiClient.ratingApiService)
            return RatingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}