package com.mob10.deliveryapp.ui.driver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mob10.deliveryapp.data.local.entity.DeliveryRequestEntity
import com.mob10.deliveryapp.data.model.DeliveryStatus
import com.mob10.deliveryapp.data.repository.DeliveryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

class DriverViewModel(
    private val deliveryRepository: DeliveryRepository,
    val driverId: Int
) : ViewModel() {

    private val startOfToday: Long
        get() {
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }

    /** Danh sách đơn chờ được nhận (toàn hệ thống PENDING) */
    val pendingRequests: StateFlow<List<DeliveryRequestEntity>> = deliveryRepository
        .pendingRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Đơn tài xế đang thực hiện (ACCEPTED hoặc PICKED_UP hoặc IN_TRANSIT) */
    val myActiveRequests: StateFlow<List<DeliveryRequestEntity>> = deliveryRepository
        .getRequestsForDelivery(driverId)
        .map { list ->
            list.filter { it.status in listOf(DeliveryStatus.ACCEPTED, DeliveryStatus.PICKED_UP, DeliveryStatus.IN_TRANSIT) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Đơn đang giao gần nhất (active delivery) */
    val activeDelivery: StateFlow<DeliveryRequestEntity?> = myActiveRequests
        .map { it.firstOrNull { req -> req.status == DeliveryStatus.IN_TRANSIT }
            ?: it.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Số đơn đã giao hôm nay */
    val deliveredTodayCount: StateFlow<Int> = deliveryRepository
        .getDeliveredTodayCountForDriver(driverId, startOfToday)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
}
