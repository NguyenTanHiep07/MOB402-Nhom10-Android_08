package com.mob10.deliveryapp.ui.driver

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mob10.deliveryapp.data.local.entity.DeliveryRequestEntity
import com.mob10.deliveryapp.data.local.entity.PackageEntity
import com.mob10.deliveryapp.ui.theme.UthOnSurfaceVariant
import com.mob10.deliveryapp.ui.theme.UthPrimary
import com.mob10.deliveryapp.ui.theme.UthSuccess

@Composable
fun NewOrdersTab(
    newOrders: List<DeliveryRequestEntity>,
    packagesByOrder: Map<Int, List<PackageEntity>>,
    onAcceptOrder: (Int) -> Unit,
    onRejectOrder: (Int) -> Unit
) {
    if (newOrders.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text(
                text = "Không có đơn giao mới nào",
                style = MaterialTheme.typography.bodyLarge,
                color = UthOnSurfaceVariant
            )
        }
    } else {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            newOrders.forEach { order ->
                NewOrderCard(
                    order = order,
                    packages = packagesByOrder[order.id] ?: emptyList(),
                    onAccept = { onAcceptOrder(order.id) },
                    onReject = { onRejectOrder(order.id) }
                )
            }
        }
    }
}

@Composable
fun NewOrderCard(
    order: DeliveryRequestEntity,
    packages: List<PackageEntity>,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Mã đơn: #${order.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Chờ tiếp nhận",
                    style = MaterialTheme.typography.labelMedium,
                    color = UthPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Restaurant Info
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Icon(Icons.Default.Store, contentDescription = null, tint = UthPrimary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = order.senderName.ifEmpty { "Nhà hàng chưa rõ" }, fontWeight = FontWeight.SemiBold)
                    Text(text = order.pickupAddress.ifEmpty { "Địa chỉ chưa rõ" }, style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Customer Info
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = UthSuccess, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = order.recipientName.ifEmpty { "Khách hàng" }, fontWeight = FontWeight.SemiBold)
                    Text(text = order.deliveryAddress.ifEmpty { "Địa chỉ giao" }, style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            // Order details
            Text(text = "Thông tin món:", style = MaterialTheme.typography.labelMedium)
            packages.forEach { pkg ->
                Text(
                    text = "- ${pkg.quantity}x ${pkg.name}",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (!pkg.notes.isNullOrBlank()) {
                    Text(
                        text = "  Ghi chú: ${pkg.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = UthOnSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Khoảng cách: ${String.format("%.1f", order.distanceKm)} km")
                Text(text = "Phí giao: ${String.format("%,.0f", order.totalCost)}đ", fontWeight = FontWeight.Bold, color = UthSuccess)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(onClick = onReject) {
                    Text("TỪ CHỐI")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = onAccept) {
                    Text("CHẤP NHẬN ĐƠN")
                }
            }
        }
    }
}
