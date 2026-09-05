// LEGACY / UNUSED
package com.mob10.deliveryapp.ui.customer

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mob10.deliveryapp.data.model.DeliveryStatus
import com.mob10.deliveryapp.data.model.Order
import com.mob10.deliveryapp.ui.components.StatusPill
import java.text.NumberFormat
import java.util.Locale

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CustomerOrderListScreen(
    viewModel: CustomerViewModel,
    onOrderClick: (Long) -> Unit,
    onBack: () -> Unit
) {
    val orders by viewModel.allMyOrders.collectAsStateWithLifecycle()
    var hasLoadedOnce by remember { mutableStateOf(false) }

    LaunchedEffect(orders) {
        hasLoadedOnce = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đơn hàng của tôi", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            when {
                !hasLoadedOnce -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                orders.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Inbox,
                            contentDescription = null,
                            modifier = Modifier.size(54.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Chưa có đơn hàng", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "Đơn mới sẽ xuất hiện tại đây",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(orders) { order ->
                            OrderListItem(order = order, onClick = { onOrderClick(order.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderListItem(order: Order, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Đơn #${order.id}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                StatusPill(text = order.status.customerLabel())
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    order.pickupAddress,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    order.deliveryAddress,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${order.distanceKm} km", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "${NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")).format(order.totalCost)} đ",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun DeliveryStatus.customerLabel(): String = when (this) {
    DeliveryStatus.CHO_TIEP_NHAN -> "Chờ tiếp nhận"
    DeliveryStatus.DA_CHAP_NHAN -> "Đã chấp nhận"
    DeliveryStatus.DA_DEN_NHA_HANG -> "Đến điểm lấy"
    DeliveryStatus.DA_LAY_HANG -> "Đã lấy hàng"
    DeliveryStatus.DANG_VAN_CHUYEN -> "Đang vận chuyển"
    DeliveryStatus.DA_DEN_KHACH_HANG -> "Đến điểm giao"
    DeliveryStatus.DA_GIAO -> "Đã giao"
    DeliveryStatus.DA_HUY -> "Đã hủy"
}
