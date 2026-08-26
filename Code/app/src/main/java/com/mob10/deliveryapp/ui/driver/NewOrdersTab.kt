package com.mob10.deliveryapp.ui.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mob10.deliveryapp.data.local.entity.DeliveryRequestEntity
import com.mob10.deliveryapp.data.local.entity.PackageEntity
import com.mob10.deliveryapp.ui.theme.UthError
import com.mob10.deliveryapp.ui.theme.UthOnSurface
import com.mob10.deliveryapp.ui.theme.UthOnSurfaceVariant
import com.mob10.deliveryapp.ui.theme.UthOutlineVariant
import com.mob10.deliveryapp.ui.theme.UthPrimary
import com.mob10.deliveryapp.ui.theme.UthPrimaryContainer
import com.mob10.deliveryapp.ui.theme.UthSecondary
import com.mob10.deliveryapp.ui.theme.UthSecondaryContainer
import com.mob10.deliveryapp.ui.theme.UthSuccess
import com.mob10.deliveryapp.ui.theme.UthSuccessContainer
import com.mob10.deliveryapp.ui.theme.UthWarning
import com.mob10.deliveryapp.ui.theme.UthWarningContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewOrdersTab(
    newOrders: List<DeliveryRequestEntity>,
    packagesByOrder: Map<Int, List<PackageEntity>>,
    onAcceptOrder: (Int) -> Unit,
    onRejectOrder: (orderId: Int, reason: String, note: String) -> Unit,
    driverStatus: DriverWorkingStatus = DriverWorkingStatus.AVAILABLE
) {
    var selectedRejectOrderId by remember { mutableStateOf<Int?>(null) }

    // BottomSheet chọn lý do từ chối
    selectedRejectOrderId?.let { orderId ->
        RejectReasonBottomSheet(
            orderId = orderId,
            onDismiss = { selectedRejectOrderId = null },
            onConfirmReject = { id, reason, note ->
                onRejectOrder(id, reason, note)
                selectedRejectOrderId = null
            }
        )
    }

    if (driverStatus == DriverWorkingStatus.OFFLINE) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(UthOutlineVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerOff,
                        contentDescription = null,
                        tint = UthOnSurfaceVariant,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Bạn đang ở trạng thái Ngoại tuyến",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = UthOnSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Hãy chuyển trạng thái sang \"Sẵn sàng\" ở góc trên để bắt đầu nhận các đơn hàng mới.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = UthOnSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    } else if (newOrders.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(UthPrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Inbox,
                        contentDescription = null,
                        tint = UthPrimary,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Chưa có đơn hàng mới nào",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = UthOnSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Hệ thống sẽ tự động hiển thị các đơn chờ tiếp nhận trong khu vực khi khách đặt hàng.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = UthOnSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Đơn chờ tiếp nhận (${newOrders.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = UthOnSurface
                )
                Text(
                    text = "Open Pool",
                    style = MaterialTheme.typography.labelSmall,
                    color = UthPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            newOrders.forEach { order ->
                NewOrderCard(
                    order = order,
                    packages = packagesByOrder[order.id] ?: emptyList(),
                    onAccept = { onAcceptOrder(order.id) },
                    onReject = { selectedRejectOrderId = order.id }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NewOrderCard(
    order: DeliveryRequestEntity,
    packages: List<PackageEntity>,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    val totalWeight = packages.sumOf { it.weightKg }
    val hasFragile = packages.any { it.isFragile }
    val isExpress = order.fragileCharge > (if (hasFragile) 5000.0 else 0.0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, UthOutlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Code & Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "#${order.id}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = UthPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = UthPrimaryContainer
                    ) {
                        Text(
                            text = "Chờ tiếp nhận",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = UthPrimary
                        )
                    }
                }
                Text(
                    text = String.format("%,.0fđ", order.totalCost),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = UthSuccess
                )
            }

            // Feature Badges (Dễ vỡ, Hỏa tốc, Khối lượng)
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (hasFragile) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = UthWarningContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = UthWarning,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Hàng dễ vỡ",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = UthWarning
                            )
                        }
                    }
                }

                if (isExpress) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = UthSecondaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ElectricBolt,
                                contentDescription = null,
                                tint = UthSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Giao hỏa tốc",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = UthSecondary
                            )
                        }
                    }
                }

                if (totalWeight > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = UthPrimaryContainer.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Scale,
                                contentDescription = null,
                                tint = UthPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format("%.1f kg", totalWeight),
                                style = MaterialTheme.typography.labelSmall,
                                color = UthPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Pickup Info
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(UthPrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Store,
                        contentDescription = null,
                        tint = UthPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = order.senderName.ifEmpty { "Người gửi / Cửa hàng" },
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = UthOnSurface
                    )
                    Text(
                        text = order.pickupAddress.ifEmpty { "Chưa cập nhật địa chỉ lấy" },
                        style = MaterialTheme.typography.bodySmall,
                        color = UthOnSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Destination Info
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(UthSuccessContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = UthSuccess,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = order.recipientName.ifEmpty { "Khách nhận hàng" },
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = UthOnSurface
                    )
                    Text(
                        text = order.deliveryAddress.ifEmpty { "Chưa cập nhật địa chỉ giao" },
                        style = MaterialTheme.typography.bodySmall,
                        color = UthOnSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = UthOutlineVariant)
            Spacer(modifier = Modifier.height(10.dp))

            // Items & Distance Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Kiện hàng: ${packages.size} món (${packages.joinToString(", ") { "${it.quantity}x ${it.name}" }})",
                    style = MaterialTheme.typography.bodySmall,
                    color = UthOnSurfaceVariant,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = String.format("Khoảng cách: %.1f km", order.distanceKm),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = UthOnSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Buttons: Từ chối & Nhận đơn
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = UthError)
                ) {
                    Text("TỪ CHỐI", fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1.5f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = UthPrimary)
                ) {
                    Text("CHẤP NHẬN ĐƠN", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
