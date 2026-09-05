package com.mob10.deliveryapp.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mob10.deliveryapp.data.remote.ApiClient
import com.mob10.deliveryapp.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminUiState(
    val isLoading: Boolean = true,
    val totalRequestCount: Int = 0,
    val pendingRequestCount: Int = 0,
    val totalUserCount: Int = 0,
    val clientCount: Int = 0,
    val driverCount: Int = 0,
    val errorMessage: String? = null
)

class AdminViewModel(
    private val repository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val totalRequests = repository.getTotalRequestCount()
            val pendingRequests = repository.getPendingRequestCount()
            val totalUsers = repository.getTotalUserCount()
            val clients = repository.getClientCount()
            val drivers = repository.getDriverCount()

            val hasError = listOf(totalRequests, pendingRequests, totalUsers, clients, drivers).any { it == null }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                totalRequestCount = totalRequests ?: _uiState.value.totalRequestCount,
                pendingRequestCount = pendingRequests ?: _uiState.value.pendingRequestCount,
                totalUserCount = totalUsers ?: _uiState.value.totalUserCount,
                clientCount = clients ?: _uiState.value.clientCount,
                driverCount = drivers ?: _uiState.value.driverCount,
                errorMessage = if (hasError) "Không thể tải đầy đủ dữ liệu từ máy chủ." else null
            )
        }
    }
}

class AdminViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminViewModel::class.java)) {
            val repository = AdminRepository(ApiClient.adminApiService)
            return AdminViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}