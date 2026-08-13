package com.mob10.deliveryapp.ui.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mob10.deliveryapp.data.local.entity.DeliveryRequestEntity
import com.mob10.deliveryapp.data.local.entity.PackageEntity
import com.mob10.deliveryapp.data.model.DeliveryStatus
import com.mob10.deliveryapp.ui.theme.UthOnSurfaceVariant
import com.mob10.deliveryapp.ui.theme.UthPrimary
import com.mob10.deliveryapp.ui.theme.UthSuccess
import com.mob10.deliveryapp.ui.theme.UthWarning

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ActiveOrderTab(
    activeOrders: List<DeliveryRequestEntity>,
    packagesByOrder: Map<Int, List<PackageEntity>>,
    onUpdateStatus: (orderId: Int, newStatus: DeliveryStatus) -> Unit
) {
    var successOrder by remember { mutableStateOf<DeliveryRequestEntity?>(null) }

    if (activeOrders.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Chưa có đơn hàng nào đang giao",
                style = MaterialTheme.typography.bodyLarge,
                color = UthOnSurfaceVariant
            )
        }
    } else {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            activeOrders.forEach { order ->
                ActiveOrderCard(
                    order = order,
                    packages = packagesByOrder[order.id] ?: emptyList(),
                    onUpdateStatus = onUpdateStatus,
                    onShowSuccess = { successOrder = it }
                )
            }
        }
    }

    // Success Dialog
    successOrder?.let { order ->
        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        val formattedAmount = currencyFormatter.format(order.totalCost)
        AlertDialog(
            onDismissRequest = { /* Require explicit confirmation */ },
            title = {
                Text(text = "Giao hàng thành công!", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Đơn hàng #${order.id} đã được giao thành công cho khách hàng.")
                    Text(
                        text = "Thu nhập: $formattedAmount",
                        fontWeight = FontWeight.Bold,
                        color = UthSuccess,
                        fontSize = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateStatus(order.id, DeliveryStatus.DA_GIAO)
                        successOrder = null
                    }
                ) {
                    Text("Hoàn tất")
                }
            },
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun ActiveOrderCard(
    order: DeliveryRequestEntity,
    packages: List<PackageEntity>,
    onUpdateStatus: (Int, DeliveryStatus) -> Unit,
    onShowSuccess: (DeliveryRequestEntity) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val openMaps: (String) -> Unit = { address ->
        val uri = android.net.Uri.parse("google.navigation:q=${android.net.Uri.encode(address)}")
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            val fallbackUri = android.net.Uri.parse("https://www.google.com/maps/search/?api=1&query=${android.net.Uri.encode(address)}")
            context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, fallbackUri))
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ĐƠN #${order.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                StatusBadge(status = order.status)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress tracking style UI based on status
            when (order.status) {
                DeliveryStatus.DA_CHAP_NHAN -> {
                    InfoRow(icon = Icons.Default.Store, title = "Điểm lấy hàng", desc = order.restaurantName, address = order.restaurantAddress)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { openMaps(order.restaurantAddress) },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ĐIỀU HƯỚNG", fontSize = 12.sp)
                        }
                        Button(
                            onClick = { onUpdateStatus(order.id, DeliveryStatus.DA_DEN_NHA_HANG) },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("ĐÃ ĐẾN NƠI", fontSize = 12.sp)
                        }
                    }
                }
                DeliveryStatus.DA_DEN_NHA_HANG -> {
                    Text(text = "Danh sách món cần lấy:", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    packages.forEach { pkg ->
                        Text(text = "- ${pkg.quantity}x ${pkg.name}", style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    ActionButton(text = "XÁC NHẬN ĐÃ LẤY HÀNG") {
                        onUpdateStatus(order.id, DeliveryStatus.DA_LAY_HANG)
                    }
                }
                DeliveryStatus.DA_LAY_HANG -> {
                    InfoRow(icon = Icons.Default.Person, title = "Giao đến khách", desc = order.customerName, address = order.customerAddress)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { openMaps(order.customerAddress) },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ĐIỀU HƯỚNG", fontSize = 12.sp)
                        }
                        Button(
                            onClick = { onUpdateStatus(order.id, DeliveryStatus.DA_DEN_KHACH_HANG) },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("ĐÃ TỚI NƠI", fontSize = 12.sp)
                        }
                    }
                }
                DeliveryStatus.DA_DEN_KHACH_HANG -> {
                    InfoRow(icon = Icons.Default.Person, title = "Khách hàng", desc = order.customerName, address = order.customerAddress)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Đơn hàng #${order.id}", style = MaterialTheme.typography.bodySmall, color = UthOnSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    ActionButton(text = "XÁC NHẬN GIAO HÀNG") {
                        onShowSuccess(order)
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun StatusBadge(status: DeliveryStatus) {
    val (text, color) = when (status) {
        DeliveryStatus.DA_CHAP_NHAN -> "Đã nhận đơn" to UthPrimary
        DeliveryStatus.DA_DEN_NHA_HANG -> "Đã đến quán" to UthWarning
        DeliveryStatus.DA_LAY_HANG -> "Đang giao" to UthPrimary
        DeliveryStatus.DA_DEN_KHACH_HANG -> "Đã đến khách" to UthWarning
        else -> status.name to UthOnSurfaceVariant
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = CircleShape,
        contentColor = color
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String,
    address: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = UthOnSurfaceVariant, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = UthOnSurfaceVariant)
            Text(text = desc, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(text = address, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun ActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = text, fontWeight = FontWeight.Bold)
    }
}
