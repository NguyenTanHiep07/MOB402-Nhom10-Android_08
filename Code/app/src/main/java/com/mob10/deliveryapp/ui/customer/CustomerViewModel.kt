package com.mob10.deliveryapp.ui.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mob10.deliveryapp.data.local.entity.DeliveryRequestEntity
import com.mob10.deliveryapp.data.model.DeliveryStatus
import com.mob10.deliveryapp.data.repository.DeliveryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class CustomerViewModel(
    private val deliveryRepository: DeliveryRepository,
    val clientId: Int
) : ViewModel() {

    /** Tất cả đơn của khách */
    private val myRequests: StateFlow<List<DeliveryRequestEntity>> = deliveryRepository
        .getRequestsForClient(clientId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Số đơn đang xử lý (không phải DELIVERED hay CANCELLED) */
    val activeOrderCount: StateFlow<Int> = myRequests
        .map { list ->
            list.count { it.status !in listOf(DeliveryStatus.DELIVERED, DeliveryStatus.CANCELLED) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /** Số đơn đã hoàn tất */
    val completedOrderCount: StateFlow<Int> = myRequests
        .map { list -> list.count { it.status == DeliveryStatus.DELIVERED } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /** Đơn gần đây nhất */
    val recentOrder: StateFlow<DeliveryRequestEntity?> = myRequests
        .map { it.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Danh sách đơn đang hoạt động (để hiển thị danh sách) */
    val activeOrders: StateFlow<List<DeliveryRequestEntity>> = myRequests
        .map { list ->
            list.filter { it.status !in listOf(DeliveryStatus.DELIVERED, DeliveryStatus.CANCELLED) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
