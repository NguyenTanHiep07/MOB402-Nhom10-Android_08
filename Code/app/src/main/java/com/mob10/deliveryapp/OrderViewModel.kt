package com.mob10.deliveryapp

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.NumberFormat
import java.util.Locale

// Hàm định dạng số tiền (VD: 25000 -> 25.000)
fun formatMoney(amount: Long): String {
    val formatter = NumberFormat.getInstance(Locale("vi", "VN"))
    return formatter.format(amount)
}

data class PendingOrderData(
    val senderName: String = "",
    val senderPhone: String = "",
    val senderAddress: String = "",
    val receiverName: String = "",
    val receiverPhone: String = "",
    val receiverAddress: String = "",
    val weight: String = "",
    val packageType: String = "Tiêu chuẩn",
    val shippingFee: Long = 25000,
    val extraFee: Long = 0,
    val totalFee: Long = 25000
)

data class OrderHistoryItem(
    val id: String,
    val receiverName: String,
    val address: String,
    val weight: String,
    val packageType: String,
    val totalFee: Long,
    val status: String = "Đang xử lý",
    val time: String = "Vừa xong"
)

class OrderViewModel : ViewModel() {
    private val _pendingOrder = MutableStateFlow(PendingOrderData())
    val pendingOrder: StateFlow<PendingOrderData> = _pendingOrder.asStateFlow()

    private val _orderHistory = MutableStateFlow<List<OrderHistoryItem>>(emptyList())
    val orderHistory: StateFlow<List<OrderHistoryItem>> = _orderHistory.asStateFlow()

    fun saveDraftOrder(
        senderName: String, senderPhone: String, senderAddress: String,
        receiverName: String, receiverPhone: String, receiverAddress: String,
        weight: String, packageType: String
    ) {
        val w = weight.toDoubleOrNull() ?: 1.0
        val baseFee = 25000L
        val calculatedExtra = if (w > 2.0) ((w - 2.0) * 5000).toLong() else 0L
        val total = baseFee + calculatedExtra

        _pendingOrder.value = PendingOrderData(
            senderName = senderName,
            senderPhone = senderPhone,
            senderAddress = senderAddress,
            receiverName = receiverName,
            receiverPhone = receiverPhone,
            receiverAddress = receiverAddress,
            weight = weight,
            packageType = packageType,
            shippingFee = baseFee,
            extraFee = calculatedExtra,
            totalFee = total
        )
    }

    fun confirmOrder() {
        val current = _pendingOrder.value
        val newOrder = OrderHistoryItem(
            id = "#UTH-${(1000..9999).random()}",
            receiverName = current.receiverName.ifEmpty { "Người nhận" },
            address = current.receiverAddress.ifEmpty { "Địa chỉ giao hàng" },
            weight = current.weight.ifEmpty { "1" },
            packageType = current.packageType,
            totalFee = current.totalFee
        )
        _orderHistory.update { listOf(newOrder) + it }
    }
}