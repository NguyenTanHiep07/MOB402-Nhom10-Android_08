package com.mob10.deliveryapp.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mob10.deliveryapp.data.model.Role
import com.mob10.deliveryapp.data.repository.DeliveryRepository
import com.mob10.deliveryapp.data.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class AdminViewModel(
    private val deliveryRepository: DeliveryRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    /** Tổng số đơn hàng */
    val totalRequestCount: StateFlow<Int> = deliveryRepository
        .getTotalCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /** Số đơn đang chờ phân công */
    val pendingRequestCount: StateFlow<Int> = deliveryRepository
        .getPendingCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /** Tổng số người dùng */
    val totalUserCount: StateFlow<Int> = userRepository
        .getTotalUserCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /** Số tài xế hiện có */
    val driverCount: StateFlow<Int> = userRepository
        .getCountByRole(Role.DELIVERY)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
}
