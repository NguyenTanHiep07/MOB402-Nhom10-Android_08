package com.mob10.deliveryapp.ui.driver

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mob10.deliveryapp.R
import com.mob10.deliveryapp.data.local.entity.DeliveryRequestEntity
import com.mob10.deliveryapp.data.local.entity.PackageEntity
import com.mob10.deliveryapp.data.model.DeliveryStatus
import com.mob10.deliveryapp.ui.components.LottieOverlay
import com.mob10.deliveryapp.ui.components.StatusPill
import com.mob10.deliveryapp.ui.theme.UthBackground
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
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(UthPrimaryContainer.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalShipping,
                        contentDescription = null,
                        tint = UthPrimary,
                        modifier = Modifier.size(42.dp)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Chưa có đơn hàng nào đang giao",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = UthOnSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Hãy chuyển sang tab \"Đơn chờ\" để nhận các đơn hàng có sẵn trong khu vực.",
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
                    text = "Đơn đang thực hiện (${activeOrders.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = UthOnSurface
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = UthPrimaryContainer
                ) {
                    Text(
                        text = "Đang giao",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = UthPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

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

    // Success Lottie Overlay chúc mừng khi hoàn tất đơn
    successOrder?.let { order ->
        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"))
        val formattedAmount = currencyFormatter.format(order.totalCost)
        LottieOverlay(
            visible = true,
            animationResId = R.raw.online_delivery_service,
            title = "Giao hàng thành công!",
            subtitle = "Đơn #${order.id} đã hoàn thành xuất sắc\nThu nhập cộng thêm: $formattedAmount",
            buttonText = "Xác nhận & Hoàn tất",
            onDismiss = {
                onUpdateStatus(order.id, DeliveryStatus.DA_GIAO)
                successOrder = null
            }
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
    val context = LocalContext.current

    val openMaps: (String) -> Unit = { address ->
        val query = if (address.isNotBlank()) address else "${order.distanceKm} km"
        val uri = Uri.parse("google.navigation:q=${Uri.encode(query)}")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            val fallbackUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(query)}")
            context.startActivity(Intent(Intent.ACTION_VIEW, fallbackUri))
        }
    }

    val makeCall: (String) -> Unit = { phone ->
        if (phone.isNotBlank()) {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
            context.startActivity(intent)
        }
    }

    val currentTargetAddress = when (order.status) {
        DeliveryStatus.DA_CHAP_NHAN, DeliveryStatus.DA_DEN_NHA_HANG -> order.pickupAddress
        else -> order.deliveryAddress
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, UthOutlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header: Code, Price & Call Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "#GD-${order.id}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = UthPrimary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        DriverStatusBadge(status = order.status)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Phí giao: ${String.format("%,.0fđ", order.totalCost)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = UthSuccess
                    )
                }

                // Quick Call Button
                val activePhone = if (order.status in listOf(DeliveryStatus.DA_CHAP_NHAN, DeliveryStatus.DA_DEN_NHA_HANG)) {
                    order.senderPhone
                } else {
                    order.recipientPhone
                }
                if (activePhone.isNotBlank()) {
                    Surface(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { makeCall(activePhone) },
                        shape = CircleShape,
                        color = UthSuccessContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Gọi điện",
                                tint = UthSuccess,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Gọi",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = UthSuccess
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Step-by-step Delivery Progress Indicator
            DeliveryStepProgress(currentStatus = order.status)

            Spacer(modifier = Modifier.height(20.dp))

            // Route & Google Maps Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = UthBackground,
                border = BorderStroke(1.dp, UthOutlineVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NearMe,
                                contentDescription = null,
                                tint = UthPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Lộ trình di chuyển",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = UthOnSurface
                            )
                        }
                        Text(
                            text = String.format("%.1f km (~%d phút)", order.distanceKm, (order.distanceKm * 3 + 4).toInt()),
                            style = MaterialTheme.typography.labelMedium,
                            color = UthPrimary,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Origin & Destination display
                    RoutePointRow(
                        icon = Icons.Default.Store,
                        iconColor = UthPrimary,
                        title = order.senderName.ifEmpty { "Người gửi" },
                        address = order.pickupAddress.ifEmpty { "Địa chỉ lấy hàng" },
                        isHighlighted = order.status in listOf(DeliveryStatus.DA_CHAP_NHAN, DeliveryStatus.DA_DEN_NHA_HANG)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    RoutePointRow(
                        icon = Icons.Default.LocationOn,
                        iconColor = UthSuccess,
                        title = order.recipientName.ifEmpty { "Khách nhận" },
                        address = order.deliveryAddress.ifEmpty { "Địa chỉ giao hàng" },
                        isHighlighted = order.status in listOf(DeliveryStatus.DA_LAY_HANG, DeliveryStatus.DA_DEN_KHACH_HANG)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Button mở Google Maps chỉ đường
                    Button(
                        onClick = { openMaps(currentTargetAddress) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = UthPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Directions,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Chỉ đường Google Maps (${if (order.status in listOf(DeliveryStatus.DA_CHAP_NHAN, DeliveryStatus.DA_DEN_NHA_HANG)) "Điểm lấy" else "Điểm giao"})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Packages Checklist
            if (packages.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Inventory,
                                contentDescription = null,
                                tint = UthOnSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Danh sách kiện hàng (${packages.size} món):",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = UthOnSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        packages.forEach { pkg ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "• ${pkg.quantity}x ${pkg.name} (${pkg.weightKg} kg)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = UthOnSurface
                                )
                                if (pkg.isFragile) {
                                    Text(
                                        text = "[Dễ vỡ]",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = UthWarning,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Next Step Action Button - Nổi bật hơn
            when (order.status) {
                DeliveryStatus.DA_CHAP_NHAN -> {
                    Button(
                        onClick = { onUpdateStatus(order.id, DeliveryStatus.DA_DEN_NHA_HANG) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = UthPrimary)
                    ) {
                        Icon(Icons.Default.Store, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("TÔI ĐÃ ĐẾN ĐIỂM LẤY HÀNG", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                    }
                }
                DeliveryStatus.DA_DEN_NHA_HANG -> {
                    Button(
                        onClick = { onUpdateStatus(order.id, DeliveryStatus.DA_LAY_HANG) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = UthSuccess)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("XÁC NHẬN ĐÃ NHẬN ĐỦ HÀNG", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                    }
                }
                DeliveryStatus.DA_LAY_HANG -> {
                    Button(
                        onClick = { onUpdateStatus(order.id, DeliveryStatus.DA_DEN_KHACH_HANG) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = UthPrimary)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("TÔI ĐÃ ĐẾN ĐIỂM GIAO CHO KHÁCH", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                    }
                }
                DeliveryStatus.DA_DEN_KHACH_HANG -> {
                    Button(
                        onClick = { onShowSuccess(order) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = UthSuccess)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("XÁC NHẬN GIAO HÀNG THÀNH CÔNG", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun RoutePointRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    address: String,
    isHighlighted: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium,
                color = UthOnSurface
            )
            Text(
                text = address,
                style = MaterialTheme.typography.labelMedium,
                color = UthOnSurfaceVariant,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun DeliveryStepProgress(currentStatus: DeliveryStatus) {
    val steps = listOf(
        "Nhận đơn" to (currentStatus in listOf(DeliveryStatus.DA_CHAP_NHAN, DeliveryStatus.DA_DEN_NHA_HANG, DeliveryStatus.DA_LAY_HANG, DeliveryStatus.DA_DEN_KHACH_HANG, DeliveryStatus.DA_GIAO)),
        "Đến quán" to (currentStatus in listOf(DeliveryStatus.DA_DEN_NHA_HANG, DeliveryStatus.DA_LAY_HANG, DeliveryStatus.DA_DEN_KHACH_HANG, DeliveryStatus.DA_GIAO)),
        "Lấy hàng" to (currentStatus in listOf(DeliveryStatus.DA_LAY_HANG, DeliveryStatus.DA_DEN_KHACH_HANG, DeliveryStatus.DA_GIAO)),
        "Đến khách" to (currentStatus in listOf(DeliveryStatus.DA_DEN_KHACH_HANG, DeliveryStatus.DA_GIAO)),
        "Đã giao" to (currentStatus == DeliveryStatus.DA_GIAO)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, (label, isCompleted) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (isCompleted) UthSuccess else UthOutlineVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    color = if (isCompleted) UthSuccess else UthOnSurfaceVariant,
                    fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun DriverStatusBadge(status: DeliveryStatus) {
    val (text, color, containerColor) = when (status) {
        DeliveryStatus.DA_CHAP_NHAN -> Triple("Đã nhận đơn", UthPrimary, UthPrimaryContainer)
        DeliveryStatus.DA_DEN_NHA_HANG -> Triple("Đã đến điểm lấy", UthWarning, UthWarningContainer)
        DeliveryStatus.DA_LAY_HANG -> Triple("Đang giao hàng", UthPrimary, UthPrimaryContainer)
        DeliveryStatus.DA_DEN_KHACH_HANG -> Triple("Đã đến điểm giao", UthWarning, UthWarningContainer)
        DeliveryStatus.DA_GIAO -> Triple("Đã giao thành công", UthSuccess, UthSuccessContainer)
        DeliveryStatus.DA_HUY -> Triple("Đã hủy", UthError, UthError.copy(alpha = 0.15f))
        else -> Triple(status.name, UthOnSurfaceVariant, UthBackground)
    }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
