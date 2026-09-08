package com.mob10.deliveryapp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mob10.deliveryapp.data.model.DeliveryStatus
import com.mob10.deliveryapp.data.model.Order
import com.mob10.deliveryapp.data.model.StatusHistory
import com.mob10.deliveryapp.ui.components.StatusPill
import com.mob10.deliveryapp.ui.customer.ClientBottomNavigation
import com.mob10.deliveryapp.ui.rating.RatingDialog
import com.mob10.deliveryapp.ui.rating.RatingViewModel
import com.mob10.deliveryapp.ui.rating.RatingViewModelFactory
import com.mob10.deliveryapp.ui.theme.UthBackground
import com.mob10.deliveryapp.ui.theme.UthError
import com.mob10.deliveryapp.ui.theme.UthErrorContainer
import com.mob10.deliveryapp.ui.theme.UthOnSurface
import com.mob10.deliveryapp.ui.theme.UthOnSurfaceVariant
import com.mob10.deliveryapp.ui.theme.UthPrimary
import com.mob10.deliveryapp.ui.theme.UthPrimaryContainer
import com.mob10.deliveryapp.ui.theme.UthSecondary
import com.mob10.deliveryapp.ui.theme.UthSuccess
import com.mob10.deliveryapp.ui.theme.UthSuccessContainer
import com.mob10.deliveryapp.ui.theme.UthWarning
import com.mob10.deliveryapp.ui.theme.UthWarningContainer
import com.mob10.deliveryapp.ui.theme.UthSurface
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
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
    val orders by orderViewModel.orderHistory.collectAsStateWithLifecycle()
    val visibleOrders = if (activeOnly) {
        orders.filter { it.status !in listOf(DeliveryStatus.DA_GIAO, DeliveryStatus.DA_HUY) }
    } else {
        orders
    }
    val selectedOrder by orderViewModel.selectedOrder.collectAsStateWithLifecycle()
    val history by orderViewModel.selectedHistory.collectAsStateWithLifecycle()
    val loading by orderViewModel.isLoading.collectAsStateWithLifecycle()
    val error by orderViewModel.errorMessage.collectAsStateWithLifecycle()
    val detailError by orderViewModel.detailError.collectAsStateWithLifecycle()
    val detailLoading by orderViewModel.detailLoading.collectAsStateWithLifecycle()
    val cancelling by orderViewModel.isCancelling.collectAsStateWithLifecycle()
    var confirmCancel by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { orderViewModel.loadOrders() }

    // --- Rating: ViewModel + state riêng cho luồng đánh giá ---
    val ratingViewModel: RatingViewModel = viewModel(factory = RatingViewModelFactory())
    val ratingUiState by ratingViewModel.uiState.collectAsStateWithLifecycle()
    var showRatingDialog by androidx.compose.runtime.saveable.rememberSaveable(selectedOrder?.id) { mutableStateOf(false) }

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
                title = { Text(title, color = UthOnSurface, fontWeight = FontWeight.ExtraBold) },
                navigationIcon = { IconButton(onClick = onBackToHome) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Trở về trang chủ", tint = UthOnSurface) } },
                actions = { TextButton(onClick = orderViewModel::loadOrders, enabled = !loading) { Text("Tải lại") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = UthBackground,
        bottomBar = {
            if (selectedTab != null && onTabSelected != null) {
                ClientBottomNavigation(selectedTab, onTabSelected)
            }
        },
        floatingActionButton = {
            if (showCreateButton) {
                ExtendedFloatingActionButton(
                    onClick = onCreateNewOrder,
                    containerColor = UthPrimary,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Tạo đơn", fontWeight = FontWeight.Bold) }
                )
            }
        }
    ) { paddingValues ->
        Box(Modifier.fillMaxSize().padding(paddingValues)) {
            if (loading && orders.isEmpty()) {
                Box(Modifier.padding(20.dp)) { com.mob10.deliveryapp.ui.components.OrderLoadingSkeleton() }
            } else if (error != null) {
                Column(Modifier.align(Alignment.Center).padding(24.dp)) {
                    Text(error.orEmpty(), color = UthError)
                    TextButton(onClick = orderViewModel::loadOrders) { Text("Thử lại") }
                }
            } else if (visibleOrders.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                            contentDescription = null,
                            tint = UthOnSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (activeOnly) "Không có đơn đang theo dõi" else "Chưa có đơn hàng nào",
                        color = UthOnSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (activeOnly) "Các đơn đang xử lý sẽ xuất hiện tại đây." else "Hãy tạo đơn hàng đầu tiên của bạn!",
                        color = UthOnSurfaceVariant,
                        fontSize = 14.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(visibleOrders, key = { it.id }) { order -> 
                        OrderCard(order) { orderViewModel.selectOrder(order) } 
                    }
                }
            }
        }
    }

    selectedOrder?.let { order ->
        AlertDialog(
            onDismissRequest = orderViewModel::clearSelectedOrder,
            title = { Text("Hành trình #GD-${order.id}", fontWeight = FontWeight.Bold) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    com.mob10.deliveryapp.ui.components.OrderJourney(order, history)
                    if (order.status == DeliveryStatus.DA_GIAO) com.mob10.deliveryapp.ui.driver.DeliveryPhotoPanel(order.id)
                    Text("Lấy hàng: ${order.pickupAddress}\nGiao hàng: ${order.deliveryAddress}")
                    order.packages.forEach { Text("${it.name} • ${it.weightKg} kg × ${it.quantity}") }
                    if (detailLoading) androidx.compose.material3.LinearProgressIndicator(Modifier.fillMaxWidth())
                    detailError?.let { Text(it, color = UthError); TextButton(onClick = { orderViewModel.selectOrder(order) }) { Text("Thử lại") } }
                    if (order.status in listOf(DeliveryStatus.CHO_TIEP_NHAN, DeliveryStatus.DA_CHAP_NHAN, DeliveryStatus.DA_DEN_NHA_HANG)) {
                        TextButton(onClick = { confirmCancel = true }, enabled = !cancelling) { Text(if (cancelling) "Đang hủy..." else "Hủy đơn hàng", color = UthError) }
                    }
                    
                    if (history.isEmpty()) {
                        Text("Chưa có mốc lịch sử chi tiết.", color = UthOnSurfaceVariant)
                    } else {
                        val sortedHistories = history.sortedBy { it.timestamp }
                        sortedHistories.forEachIndexed { index, item ->
                            val isLast = index == sortedHistories.size - 1
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(androidx.compose.foundation.shape.CircleShape)
                                            .background(if (isLast) UthPrimary else UthOnSurfaceVariant)
                                    )
                                    if (!isLast) {
                                        Box(
                                            modifier = Modifier
                                                .width(2.dp)
                                                .height(28.dp)
                                                .background(UthOnSurfaceVariant.copy(alpha = 0.3f))
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = item.toStatus?.label() ?: "Không rõ",
                                        fontWeight = if (isLast) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isLast) UthPrimary else UthOnSurface,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "${formatTimestamp(item.timestamp)}${item.note?.let { " · $it" }.orEmpty()}",
                                        color = UthOnSurfaceVariant,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    // --- Nút Đánh giá: chỉ hiện khi đã giao xong và có tài xế ---
                    val driverId = order.deliveryPerson?.id
                    if (order.status == DeliveryStatus.DA_GIAO && driverId != null) {
                        Spacer(Modifier.height(8.dp))
                        if (ratingUiState.alreadyRated) {
                            Text("Bạn đã đánh giá đơn này. Cảm ơn bạn!", color = UthSuccess, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        } else {
                            Button(
                                onClick = { showRatingDialog = true },
                                enabled = !ratingUiState.isLoading,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = UthPrimary)
                            ) {
                                Text("Đánh giá tài xế", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = { 
                TextButton(onClick = orderViewModel::clearSelectedOrder) { 
                    Text("Đóng", fontWeight = FontWeight.Bold, color = UthOnSurfaceVariant) 
                } 
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // --- Dialog đánh giá, hiển thị đè lên khi bấm nút ---
    val currentSelectedOrder = selectedOrder
    if (confirmCancel && currentSelectedOrder != null) AlertDialog(
        onDismissRequest = { confirmCancel = false },
        title = { Text("Hủy đơn #${currentSelectedOrder.id}?") },
        text = { Text("Đơn sẽ dừng giao và không thể khôi phục. Bạn vẫn có thể tạo đơn mới.") },
        confirmButton = { TextButton(onClick = { confirmCancel = false; orderViewModel.cancelSelectedOrder() }) { Text("Xác nhận hủy") } },
        dismissButton = { TextButton(onClick = { confirmCancel = false }) { Text("Giữ đơn") } }
    )
    val currentDriverId = currentSelectedOrder?.deliveryPerson?.id
    if (showRatingDialog && currentSelectedOrder != null && currentDriverId != null) {
        RatingDialog(
            deliveryRequestId = currentSelectedOrder.id,
            clientId = orderViewModel.clientId.toLong(),
            driverId = currentDriverId,
            onDismiss = {
                showRatingDialog = false
                ratingViewModel.clearError()
            },
            onSubmit = { stars, comment ->
                ratingViewModel.submitRating(
                    deliveryRequestId = currentSelectedOrder.id,
                    clientId = orderViewModel.clientId.toLong(),
                    driverId = currentDriverId,
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
private fun OrderCard(order: Order, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.medium,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("#GD-${order.id}", color = UthOnSurface, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                
                val (containerColor, contentColor) = order.status.pillColors()
                StatusPill(
                    text = order.status.label(),
                    containerColor = containerColor,
                    contentColor = contentColor,
                    dotColor = contentColor
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.LocationOn, null, tint = UthPrimary, modifier = Modifier.size(18.dp).padding(top = 2.dp))
                Column(Modifier.padding(start = 10.dp)) {
                    Text(
                        text = order.pickupAddress, 
                        color = UthOnSurface, 
                        fontSize = 14.sp, 
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text("Điểm lấy hàng", color = UthOnSurfaceVariant, fontSize = 12.sp)
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.TwoWheeler, null, tint = UthSecondary, modifier = Modifier.size(18.dp).padding(top = 2.dp))
                Column(Modifier.padding(start = 10.dp)) {
                    Text(
                        text = order.deliveryAddress, 
                        color = UthOnSurface, 
                        fontSize = 14.sp, 
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text("Giao cho ${order.recipientName}", color = UthOnSurfaceVariant, fontSize = 12.sp)
                }
            }
            
            Spacer(Modifier.height(16.dp))
            androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
            Spacer(Modifier.height(12.dp))
            
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.NearMe, contentDescription = null, tint = UthOnSurfaceVariant, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${order.distanceKm} km", color = UthOnSurfaceVariant, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
                Text(String.format(java.util.Locale.forLanguageTag("vi-VN"), "%,.0fđ", order.totalCost), color = UthOnSurface, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }
            
            Spacer(Modifier.height(12.dp))
            Text("Chạm để xem lịch sử trạng thái", color = UthPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

fun DeliveryStatus.label(): String = when (this) {
    DeliveryStatus.CHO_TIEP_NHAN -> "Chờ tiếp nhận"
    DeliveryStatus.DA_CHAP_NHAN -> "Đã chấp nhận"
    DeliveryStatus.DA_DEN_NHA_HANG -> "Đã đến điểm lấy"
    DeliveryStatus.DA_LAY_HANG -> "Đã lấy hàng"
    DeliveryStatus.DANG_VAN_CHUYEN -> "Đang vận chuyển"
    DeliveryStatus.DA_DEN_KHACH_HANG -> "Đã tới điểm giao"
    DeliveryStatus.DA_GIAO -> "Đã giao"
    DeliveryStatus.DA_HUY -> "Đã hủy"
}

private fun DeliveryStatus.pillColors(): Pair<Color, Color> = when (this) {
    DeliveryStatus.CHO_TIEP_NHAN -> Pair(Color(0xFFE2E8F0), UthOnSurfaceVariant)
    DeliveryStatus.DA_CHAP_NHAN, DeliveryStatus.DA_LAY_HANG, DeliveryStatus.DANG_VAN_CHUYEN -> Pair(UthPrimaryContainer, UthPrimary)
    DeliveryStatus.DA_DEN_NHA_HANG, DeliveryStatus.DA_DEN_KHACH_HANG -> Pair(UthWarningContainer, UthWarning)
    DeliveryStatus.DA_GIAO -> Pair(UthSuccessContainer, UthSuccess)
    DeliveryStatus.DA_HUY -> Pair(UthErrorContainer, UthError)
}

/**
 * Format timestamp — hỗ trợ cả ISO 8601 string (từ REST API) và epoch millis string.
 */
private fun formatTimestamp(timestamp: String?): String {
    if (timestamp.isNullOrBlank()) return ""
    // Thử parse ISO 8601 trước
    return try {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = isoFormat.parse(timestamp.take(19)) // lấy phần không có timezone
        val displayFormat = SimpleDateFormat("HH:mm · dd/MM/yyyy", Locale.forLanguageTag("vi-VN"))
        displayFormat.format(date!!)
    } catch (_: Exception) {
        // Fallback: thử parse epoch millis
        try {
            val millis = timestamp.toLong()
            val displayFormat = SimpleDateFormat("HH:mm · dd/MM/yyyy", Locale.forLanguageTag("vi-VN"))
            displayFormat.format(Date(millis))
        } catch (_: Exception) {
            timestamp
        }
    }
}
