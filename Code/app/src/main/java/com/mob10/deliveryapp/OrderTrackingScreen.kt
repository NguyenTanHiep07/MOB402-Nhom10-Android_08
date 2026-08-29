package com.mob10.deliveryapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mob10.deliveryapp.data.local.entity.DeliveryRequestEntity
import com.mob10.deliveryapp.data.model.DeliveryStatus
import com.mob10.deliveryapp.ui.customer.ClientBottomNavigation
import com.mob10.deliveryapp.ui.rating.RatingDialog
import com.mob10.deliveryapp.ui.rating.RatingViewModel
import com.mob10.deliveryapp.ui.rating.RatingViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(
    orderViewModel: OrderViewModel,
    onCreateNewOrder: () -> Unit,
    onBackToHome: () -> Unit,
    title: String = "Đơn hàng của tôi",
    activeOnly: Boolean = false,
    showCreateButton: Boolean = true,
    selectedTab: Int? = null,
    onTabSelected: ((Int) -> Unit)? = null
) {
    val orders by orderViewModel.orderHistory.collectAsState()
    val visibleOrders = if (activeOnly) {
        orders.filter { it.status !in listOf(DeliveryStatus.DA_GIAO, DeliveryStatus.DA_HUY) }
    } else {
        orders
    }
    val selectedOrder by orderViewModel.selectedOrder.collectAsState()
    val history by orderViewModel.selectedHistory.collectAsState()

    // --- Rating: ViewModel + state riêng cho luồng đánh giá ---
    val ratingViewModel: RatingViewModel = viewModel(factory = RatingViewModelFactory())
    val ratingUiState by ratingViewModel.uiState.collectAsState()
    var showRatingDialog by remember { mutableStateOf(false) }

    LaunchedEffect(selectedOrder?.id, selectedOrder?.status) {
        val order = selectedOrder
        if (order != null && order.status == DeliveryStatus.DA_GIAO) {
            ratingViewModel.checkExistingRating(order.id)
        }
    }

    LaunchedEffect(ratingUiState.submitSuccess) {
        if (ratingUiState.submitSuccess) {
            showRatingDialog = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBackToHome) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Trở về trang chủ", tint = TextPrimary) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground,
        bottomBar = {
            if (selectedTab != null && onTabSelected != null) {
                ClientBottomNavigation(selectedTab, onTabSelected)
            }
        },
        floatingActionButton = {
            if (showCreateButton) {
                FloatingActionButton(onClick = onCreateNewOrder, containerColor = PrimaryBlue) {
                    Text("+ Tạo đơn", color = Color.White, modifier = Modifier.padding(horizontal = 12.dp))
                }
            }
        }
    ) { paddingValues ->
        Box(Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            if (visibleOrders.isEmpty()) {
                Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (activeOnly) "Không có đơn đang theo dõi" else "Chưa có đơn hàng nào", color = TextSecondary, fontSize = 16.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(if (activeOnly) "Các đơn đang xử lý sẽ xuất hiện tại đây." else "Hãy tạo đơn hàng đầu tiên của bạn!", color = TextSecondary, fontSize = 13.sp)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(visibleOrders, key = { it.id }) { order -> OrderCard(order) { orderViewModel.selectOrder(order) } }
                }
            }
        }
    }

    selectedOrder?.let { order ->
        AlertDialog(
            onDismissRequest = orderViewModel::clearSelectedOrder,
            title = { Text("Lịch sử #GD-${order.id}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Trạng thái hiện tại: ${order.status.label()}", fontWeight = FontWeight.Bold)
                    if (history.isEmpty()) Text("Chưa có mốc lịch sử chi tiết.") else history.forEach { item ->
                        Column {
                            Text(item.toStatus.label(), fontWeight = FontWeight.SemiBold)
                            Text("${formatTimestamp(item.timestamp)}${item.note?.let { " · $it" }.orEmpty()}", color = TextSecondary, fontSize = 12.sp)
                        }
                    }

                    // --- Nút Đánh giá: chỉ hiện khi đã giao xong và có tài xế ---
                    val driverId = order.deliveryPersonId
                    if (order.status == DeliveryStatus.DA_GIAO && driverId != null) {
                        Spacer(Modifier.height(4.dp))
                        if (ratingUiState.alreadyRated) {
                            Text("Bạn đã đánh giá đơn này. Cảm ơn bạn!", color = TextSecondary, fontSize = 13.sp)
                        } else {
                            Button(
                                onClick = { showRatingDialog = true },
                                enabled = !ratingUiState.isLoading
                            ) {
                                Text("Đánh giá tài xế")
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = orderViewModel::clearSelectedOrder) { Text("Đóng") } }
        )
    }

    // --- Dialog đánh giá, hiển thị đè lên khi bấm nút ---
    if (showRatingDialog && selectedOrder != null && selectedOrder!!.deliveryPersonId != null) {
        val order = selectedOrder!!
        RatingDialog(
            deliveryRequestId = order.id,
            clientId = orderViewModel.clientId,
            driverId = order.deliveryPersonId!!,
            onDismiss = {
                showRatingDialog = false
                ratingViewModel.clearError()
            },
            onSubmit = { stars, comment ->
                ratingViewModel.submitRating(
                    deliveryRequestId = order.id,
                    clientId = orderViewModel.clientId,
                    driverId = order.deliveryPersonId!!,
                    stars = stars,
                    comment = comment.ifBlank { null }
                )
            },
            isSubmitting = ratingUiState.isSubmitting,
            errorMessage = ratingUiState.errorMessage
        )
    }
} // end fun OrderTrackingScreen

@Composable
private fun OrderCard(order: DeliveryRequestEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("#GD-${order.id}", color = PrimaryBlue, fontWeight = FontWeight.Bold)
                Text(order.status.label(), color = order.status.statusColor(), fontSize = 12.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text("Người nhận: ${order.recipientName}", color = TextPrimary)
            Text("Địa chỉ: ${order.deliveryAddress}", color = TextSecondary, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${order.distanceKm} km", color = TextSecondary, fontSize = 12.sp)
                Text("${formatMoney(order.totalCost.toLong())} đ", color = TextPrimary, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            Text("Chạm để xem lịch sử trạng thái", color = PrimaryBlue, fontSize = 12.sp)
        }
    }
}

private fun DeliveryStatus.label(): String = when (this) {
    DeliveryStatus.CHO_TIEP_NHAN -> "Chờ tiếp nhận"
    DeliveryStatus.DA_CHAP_NHAN -> "Đã chấp nhận"
    DeliveryStatus.DA_DEN_NHA_HANG -> "Đã đến điểm lấy"
    DeliveryStatus.DA_LAY_HANG -> "Đã lấy hàng"
    DeliveryStatus.DA_DEN_KHACH_HANG -> "Đã đến điểm giao"
    DeliveryStatus.DA_GIAO -> "Đã giao"
    DeliveryStatus.DA_HUY -> "Đã hủy"
}

@Composable
private fun DeliveryStatus.statusColor(): Color = when (this) {
    DeliveryStatus.DA_GIAO -> Color(0xFF16A34A)
    DeliveryStatus.DA_HUY -> MaterialTheme.colorScheme.error
    else -> Color(0xFFF59E0B)
}

private fun formatTimestamp(timestamp: Long): String = SimpleDateFormat("HH:mm · dd/MM/yyyy", Locale.forLanguageTag("vi-VN")).format(Date(timestamp))