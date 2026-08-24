package com.mob10.deliveryapp.ui.admin

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mob10.deliveryapp.data.local.AppDatabase
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

    /** Chỉ đếm khách hàng, không gộp tài xế và admin. */
    val clientCount: StateFlow<Int> = userRepository
        .getCountByRole(Role.CLIENT)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /** Số tài xế hiện có */
    val driverCount: StateFlow<Int> = userRepository
        .getCountByRole(Role.DELIVERY)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
}

class AdminViewModelFactory(context: Context) : ViewModelProvider.Factory {
    private val applicationContext = context.applicationContext

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminViewModel::class.java)) {
            val database = AppDatabase.getDatabase(applicationContext)
            val deliveryRepository = DeliveryRepository(
                database,
                database.deliveryRequestDao(),
                database.packageDao(),
                database.statusHistoryDao()
            )
            return AdminViewModel(
                deliveryRepository = deliveryRepository,
                userRepository = UserRepository(database.userDao())
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
