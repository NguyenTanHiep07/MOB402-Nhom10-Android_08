package com.mob10.deliveryapp.ui.driver

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.clickable
import androidx.compose.animation.animateContentSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mob10.deliveryapp.data.model.Order
import com.mob10.deliveryapp.data.model.OrderPackage
import com.mob10.deliveryapp.data.model.RejectionReason
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
    newOrders: List<Order>,
    rejectionReasons: List<RejectionReason> = emptyList(),
    packagesByOrder: Map<Int, List<OrderPackage>> = emptyMap(),
    onAcceptOrder: (Int) -> Unit,
    onRejectOrder: (orderId: Int, reason: String, note: String) -> Unit,
    driverStatus: DriverWorkingStatus = DriverWorkingStatus.AVAILABLE,
    actionInProgress: Boolean = false,
    rejectedOrderId: Int? = null,
    errorMessage: String? = null
) {
    var selectedRejectOrderId by rememberSaveable { mutableStateOf<Int?>(null) }
    LaunchedEffect(rejectedOrderId) {
        if (rejectedOrderId != null && rejectedOrderId == selectedRejectOrderId) selectedRejectOrderId = null
    }

    // BottomSheet chọn lý do từ chối
    selectedRejectOrderId?.let { orderId ->
        RejectReasonBottomSheet(
            orderId = orderId,
            rejectionReasons = rejectionReasons,
            isSubmitting = actionInProgress,
            errorMessage = errorMessage,
            onDismiss = { selectedRejectOrderId = null },
            onConfirmReject = { id, reason, note ->
                onRejectOrder(id, reason, note)
            }
        )
    }

    if (driverStatus == DriverWorkingStatus.OFFLINE || driverStatus == DriverWorkingStatus.BUSY) {
        val isOffline = driverStatus == DriverWorkingStatus.OFFLINE
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, start = 4.dp, end = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isOffline) UthOutlineVariant.copy(alpha = 0.4f) else UthWarningContainer.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = if (isOffline) "CHẾ ĐỘ TẠM NGHỈ" else "ĐANG TRONG CHUYẾN GIAO",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isOffline) UthOnSurfaceVariant else UthWarning,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isOffline) "Tài khoản đang tạm dừng nhận đơn" else "Bạn đang bận giao hàng",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = UthOnSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isOffline)
                            "Vui lòng chuyển trạng thái sang \"Sẵn sàng\" trong mục Hồ sơ để bắt đầu nhận chuyến mới."
                        else
                            "Hãy hoàn tất đơn hàng hiện tại ở mục \"Đang giao\" hoặc chuyển trạng thái để tiếp tục nhận thêm đơn mới.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = UthOnSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    } else if (newOrders.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, start = 4.dp, end = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = UthPrimaryContainer.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "DANH SÁCH CHỜ",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = UthPrimary,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Chưa có đơn hàng mới",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = UthOnSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Các đơn hàng mới phát sinh trong khu vực sẽ tự động hiển thị tại đây khi khách hàng đặt chuyến.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = UthOnSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Đơn chờ tiếp nhận (${newOrders.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = UthOnSurface
                )
                Text(
                    text = "Mới cập nhật",
                    style = MaterialTheme.typography.labelMedium,
                    color = UthPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            newOrders.forEach { order ->
                NewOrderCard(
                    order = order,
                    packages = packagesByOrder[order.id.toInt()] ?: order.packages,
                    onAccept = { onAcceptOrder(order.id.toInt()) },
                    onReject = { selectedRejectOrderId = order.id.toInt() }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NewOrderCard(
    order: Order,
    packages: List<OrderPackage> = order.packages,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    val context = LocalContext.current
    val actualPackages = if (packages.isNotEmpty()) packages else order.packages
    val totalWeight = actualPackages.sumOf { it.weightKg }
    val hasFragile = actualPackages.any { it.isFragile }
    val isExpress = actualPackages.any { it.isExpress } || order.fragileCharge > (if (hasFragile) 5000.0 else 0.0)
    
    var isItemsExpanded by remember { mutableStateOf(false) }
    fun dial(phone: String) {
        val cleanPhone = phone.filter { it.isDigit() || it == '+' }
        if (cleanPhone.isBlank()) return
        val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", cleanPhone, null))
        if (intent.resolveActivity(context.packageManager) != null) context.startActivity(intent)
        else android.widget.Toast.makeText(context, "Thiết bị không có ứng dụng gọi điện", android.widget.Toast.LENGTH_LONG).show()
    }

    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, UthOutlineVariant)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
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
                        fontWeight = FontWeight.ExtraBold,
                        color = UthPrimary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = UthPrimaryContainer
                    ) {
                        Text(
                            text = "Chờ tiếp nhận",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = UthPrimary
                        )
                    }
                }
                Text(
                    text = String.format(java.util.Locale.forLanguageTag("vi-VN"), "%,.0fđ", order.totalCost),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = UthSuccess
                )
            }

            // Feature Badges
            Spacer(modifier = Modifier.height(14.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (hasFragile) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = UthWarningContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = UthWarning,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
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
                        shape = RoundedCornerShape(10.dp),
                        color = UthSecondaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ElectricBolt,
                                contentDescription = null,
                                tint = UthSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
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
                        shape = RoundedCornerShape(10.dp),
                        color = UthPrimaryContainer.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Scale,
                                contentDescription = null,
                                tint = UthPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = String.format(java.util.Locale.forLanguageTag("vi-VN"), "%.1f kg", totalWeight),
                                style = MaterialTheme.typography.labelSmall,
                                color = UthPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pickup Info
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(UthPrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Store,
                        contentDescription = null,
                        tint = UthPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = order.senderName.ifEmpty { "Người gửi / Cửa hàng" },
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = UthOnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = order.pickupAddress.ifEmpty { "Chưa cập nhật địa chỉ lấy" },
                        style = MaterialTheme.typography.bodySmall,
                        color = UthOnSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(order.senderPhone, style = MaterialTheme.typography.labelSmall, color = UthPrimary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Destination Info
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(UthSuccessContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = UthSuccess,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = order.recipientName.ifEmpty { "Khách nhận hàng" },
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = UthOnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = order.deliveryAddress.ifEmpty { "Chưa cập nhật địa chỉ giao" },
                        style = MaterialTheme.typography.bodySmall,
                        color = UthOnSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(order.recipientPhone, style = MaterialTheme.typography.labelSmall, color = UthSuccess)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { dial(order.senderPhone) }, modifier = Modifier.weight(1f),
                    enabled = order.senderPhone.isNotBlank(),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)) {
                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(5.dp)); Text("Gọi người gửi", maxLines = 1, fontSize = 12.sp)
                }
                OutlinedButton(onClick = { dial(order.recipientPhone) }, modifier = Modifier.weight(1f),
                    enabled = order.recipientPhone.isNotBlank(),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)) {
                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(5.dp)); Text("Gọi người nhận", maxLines = 1, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = UthOutlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            // Items & Distance Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { isItemsExpanded = !isItemsExpanded }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Kiện hàng: ${packages.size} món",
                            style = MaterialTheme.typography.bodySmall,
                            color = UthOnSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = if (isItemsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = UthOnSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    if (isItemsExpanded) {
                        Spacer(modifier = Modifier.height(4.dp))
                        packages.forEach { pkg ->
                            Text(
                                text = "• ${pkg.quantity} × ${pkg.name} (${pkg.weightKg} kg)",
                                style = MaterialTheme.typography.bodySmall,
                                color = UthOnSurface
                            )
                        }
                    } else {
                        Text(
                            text = packages.joinToString(", ") { "${it.quantity} × ${it.name}" },
                            style = MaterialTheme.typography.bodySmall,
                            color = UthOnSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = String.format(java.util.Locale.forLanguageTag("vi-VN"), "Khoảng cách: %.1f km", order.distanceKm),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = UthOnSurface
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = UthError)
                ) {
                    Text("TỪ CHỐI", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                Button(
                    onClick = onAccept,
                    modifier = Modifier
                        .weight(1.5f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = UthPrimary)
                ) {
                    Text("NHẬN ĐƠN", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                }
            }
        }
    }
}
