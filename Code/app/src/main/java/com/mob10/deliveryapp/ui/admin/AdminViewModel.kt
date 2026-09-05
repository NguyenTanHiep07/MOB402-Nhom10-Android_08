package com.mob10.deliveryapp.ui.admin

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mob10.deliveryapp.data.model.AdminDriver
import com.mob10.deliveryapp.data.model.AdminUser
import com.mob10.deliveryapp.data.model.DeliveryStatus
import com.mob10.deliveryapp.data.model.Order
import com.mob10.deliveryapp.data.remote.RetrofitClient
import com.mob10.deliveryapp.data.repository.AdminRepository
import com.mob10.deliveryapp.data.util.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * AdminViewModel — quản lý dashboard admin qua REST API.
 *
 * Thay đổi từ phiên bản cũ:
 * - Dùng AdminRepository (REST) thay cho DeliveryRepository + UserRepository (Room)
 * - Data dùng domain model AdminUser, AdminDriver, Order thay vì entity
 * - Load data bằng API call thay vì Room Flow
 * - Hiển thị danh sách chi tiết thay vì chỉ count
 */
class AdminViewModel(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _users = MutableStateFlow<List<AdminUser>>(emptyList())
    private val _drivers = MutableStateFlow<List<AdminDriver>>(emptyList())
    private val _driverAlerts = MutableStateFlow<List<AdminDriver>>(emptyList())
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    val users = _users.asStateFlow()
    val drivers = _drivers.asStateFlow()
    val driverAlerts = _driverAlerts.asStateFlow()
    val orders = _orders.asStateFlow()

    /** Tổng số đơn hàng */
    val totalRequestCount: StateFlow<Int> = _orders
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /** Số đơn đang chờ phân công */
    val pendingRequestCount: StateFlow<Int> = _orders
        .map { list -> list.count { it.status == DeliveryStatus.CHO_TIEP_NHAN } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /** Tổng số người dùng */
    val totalUserCount: StateFlow<Int> = _users
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /** Chỉ đếm khách hàng, không gộp tài xế và admin. */
    val clientCount: StateFlow<Int> = _users
        .map { list -> list.count { it.role == "CLIENT" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /** Số tài xế hiện có */
    val driverCount: StateFlow<Int> = _drivers
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    init {
        loadDashboardData()
    }

    /** Load toàn bộ dữ liệu dashboard từ REST API. */
    fun loadDashboardData() {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            // Load song song tất cả data
            loadUsers()
            loadDrivers()
            loadOrders()
            when (val result = adminRepository.getDriverAlerts()) {
                is NetworkResult.Success -> _driverAlerts.value = result.data
                is NetworkResult.Empty -> _driverAlerts.value = emptyList()
                is NetworkResult.Error -> _errorMessage.value = result.message
                else -> Unit
            }

            _isLoading.value = false
        }
    }

    private suspend fun loadUsers() {
        when (val result = adminRepository.getUsers()) {
            is NetworkResult.Success -> _users.value = result.data
            is NetworkResult.Empty -> _users.value = emptyList()
            is NetworkResult.Error -> _errorMessage.value = result.message
            is NetworkResult.Loading -> Unit
        }
    }

    private suspend fun loadDrivers() {
        when (val result = adminRepository.getDrivers()) {
            is NetworkResult.Success -> _drivers.value = result.data
            is NetworkResult.Empty -> _drivers.value = emptyList()
            is NetworkResult.Error -> _errorMessage.value = result.message
            is NetworkResult.Loading -> Unit
        }
    }

    private suspend fun loadOrders() {
        when (val result = adminRepository.getAllOrders()) {
            is NetworkResult.Success -> _orders.value = result.data
            is NetworkResult.Empty -> _orders.value = emptyList()
            is NetworkResult.Error -> _errorMessage.value = result.message
            is NetworkResult.Loading -> Unit
        }
    }
}

/**
 * Factory tạo AdminViewModel với AdminRepository dùng REST API thật.
 */
class AdminViewModelFactory(context: Context) : ViewModelProvider.Factory {
    private val applicationContext = context.applicationContext

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminViewModel::class.java)) {
            RetrofitClient.init(applicationContext)
            return AdminViewModel(
                adminRepository = AdminRepository(RetrofitClient.adminApi)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
