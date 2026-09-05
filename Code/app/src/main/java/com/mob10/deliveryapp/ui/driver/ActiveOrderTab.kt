package com.mob10.deliveryapp.ui.driver

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.TwoWheeler
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
import com.mob10.deliveryapp.data.model.Order
import com.mob10.deliveryapp.data.model.OrderPackage
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
    activeOrders: List<Order>,
    packagesByOrder: Map<Int, List<OrderPackage>> = emptyMap(),
    onDelivered: () -> Unit = {},
    actionInProgress: Boolean = false,
    onUpdateStatus: (orderId: Int, newStatus: DeliveryStatus) -> Unit
) {
    var successOrder by remember { mutableStateOf<Order?>(null) }

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
                        imageVector = Icons.Default.TwoWheeler,
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                    actionInProgress = actionInProgress,
                    packages = packagesByOrder[order.id.toInt()] ?: order.packages,
                    onUpdateStatus = onUpdateStatus,
                    onShowSuccess = { successOrder = it; onDelivered() }
                )
            }
        }
    }

    // Success Lottie Overlay chúc mừng khi hoàn tất đơn (tự động biến mất sau 2s)
    successOrder?.let { order ->
        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"))
        val formattedAmount = currencyFormatter.format(order.totalCost)
        LottieOverlay(
            visible = true,
            animationResId = R.raw.online_delivery_service,
            title = "Giao hàng thành công!",
            subtitle = "Đơn #${order.id} đã hoàn thành xuất sắc\nThu nhập cộng thêm: $formattedAmount",
            buttonText = null,
            autoDismissMs = 2000L,
            onDismiss = {
                successOrder = null
            }
        )
    }
}

@Composable
fun ActiveOrderCard(
    order: Order,
    actionInProgress: Boolean = false,
    packages: List<OrderPackage> = order.packages,
    onUpdateStatus: (Int, DeliveryStatus) -> Unit,
    onShowSuccess: (Order) -> Unit
) {
    val context = LocalContext.current

    val isPickupPhase = order.status in listOf(DeliveryStatus.DA_CHAP_NHAN, DeliveryStatus.DA_DEN_NHA_HANG)
    val targetLat = if (isPickupPhase) order.pickupLatitude else order.deliveryLatitude
    val targetLng = if (isPickupPhase) order.pickupLongitude else order.deliveryLongitude
    val currentTargetAddress = if (isPickupPhase) order.pickupAddress else order.deliveryAddress

    val openMaps: (String, Double?, Double?) -> Unit = openMaps@{ address, lat, lng ->
        val query = when {
            lat != null && lng != null && lat.isFinite() && lng.isFinite() &&
                lat in -90.0..90.0 && lng in -180.0..180.0 -> "$lat,$lng"
            address.isNotBlank() -> address.trim()
            else -> {
                android.widget.Toast.makeText(context, "Đơn hàng chưa có địa chỉ để chỉ đường", android.widget.Toast.LENGTH_LONG).show()
                return@openMaps
            }
        }
        val uri = Uri.parse("https://www.google.com/maps/dir/").buildUpon()
            .appendQueryParameter("api", "1")
            .appendQueryParameter("destination", query)
            .appendQueryParameter("dir_action", "navigate")
            .build()
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                .onFailure { android.widget.Toast.makeText(context, "Không tìm thấy ứng dụng mở bản đồ", android.widget.Toast.LENGTH_LONG).show() }
        }
    }

    val makeCall: (String) -> Unit = { phone ->
        val cleanPhone = phone.filter { it.isDigit() || it == '+' }
        if (cleanPhone.isNotBlank()) {
            val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", cleanPhone, null))
            if (intent.resolveActivity(context.packageManager) != null) context.startActivity(intent)
            else android.widget.Toast.makeText(context, "Thiết bị không có ứng dụng gọi điện", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, UthOutlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            androidx.compose.animation.AnimatedVisibility(actionInProgress) {
                Column(Modifier.padding(bottom = 12.dp)) {
                    androidx.compose.material3.LinearProgressIndicator(Modifier.fillMaxWidth())
                    Text("Đang cập nhật tiến trình…", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 6.dp))
                }
            }
            // Header: Code, Status & Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "#GD-${order.id}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = UthPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    DriverStatusBadge(status = order.status)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Phí giao",
                        style = MaterialTheme.typography.labelSmall,
                        color = UthOnSurfaceVariant,
                        fontSize = 11.sp
                    )
                    Text(
                        text = String.format(java.util.Locale.forLanguageTag("vi-VN"), "%,.0fđ", order.totalCost),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = UthSuccess
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Step-by-step Delivery Progress Indicator
            DeliveryStepProgress(currentStatus = order.status)

            Spacer(modifier = Modifier.height(20.dp))

            // Google Maps is opened for navigation, so the demo has no Maps API key dependency.
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
                            text = String.format(java.util.Locale.forLanguageTag("vi-VN"), "%.1f km", order.distanceKm),
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
                        isHighlighted = order.status in listOf(DeliveryStatus.DA_LAY_HANG, DeliveryStatus.DANG_VAN_CHUYEN, DeliveryStatus.DA_DEN_KHACH_HANG)
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { makeCall(order.senderPhone) },
                            enabled = order.senderPhone.isNotBlank(),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(5.dp)); Text("Gọi người gửi", maxLines = 1, fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = { makeCall(order.recipientPhone) },
                            enabled = order.recipientPhone.isNotBlank(),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(5.dp)); Text("Gọi người nhận", maxLines = 1, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Button mở Google Maps chỉ đường
                    Button(
                        onClick = { openMaps(currentTargetAddress, targetLat, targetLng) },
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
                            text = if (isPickupPhase) "Chỉ đường đến điểm lấy" else "Chỉ đường đến điểm giao",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "Mở bằng Google Maps hoặc trình duyệt. Quay lại GoDrop để cập nhật trạng thái giao hàng.",
                        modifier = Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = UthOnSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Packages Checklist
            if (packages.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = UthBackground,
                    border = BorderStroke(1.dp, UthOutlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = UthPrimaryContainer
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Inventory,
                                        contentDescription = null,
                                        tint = UthPrimary,
                                        modifier = Modifier.padding(8.dp).size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Danh sách kiện hàng",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = UthOnSurface
                                    )
                                    Text(
                                        text = "${packages.size} loại hàng",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = UthOnSurfaceVariant
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = UthPrimaryContainer
                            ) {
                                Text(
                                    text = "${packages.sumOf { it.quantity }} món",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = UthPrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        packages.forEachIndexed { index, pkg ->
                            PackageItemCard(index = index + 1, item = pkg)
                            if (index < packages.lastIndex) Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Next Step Action Button - Nổi bật hơn
            when (order.status) {
                DeliveryStatus.DA_CHAP_NHAN -> {
                    Button(
                        onClick = { onUpdateStatus(order.id.toInt(), DeliveryStatus.DA_DEN_NHA_HANG) },
                        enabled = !actionInProgress,
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
                        onClick = { onUpdateStatus(order.id.toInt(), DeliveryStatus.DA_LAY_HANG) },
                        enabled = !actionInProgress,
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
                    Button(onClick = { onUpdateStatus(order.id.toInt(), DeliveryStatus.DANG_VAN_CHUYEN) },
                        enabled = !actionInProgress,
                        modifier = Modifier.fillMaxWidth().height(56.dp)) {
                        Text("BẮT ĐẦU VẬN CHUYỂN", fontWeight = FontWeight.Bold)
                    }
                }
                DeliveryStatus.DANG_VAN_CHUYEN -> {
                    Button(
                        onClick = { onUpdateStatus(order.id.toInt(), DeliveryStatus.DA_DEN_KHACH_HANG) },
                        enabled = !actionInProgress,
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
                    DeliveryPhotoPanel(order.id, capture = true, onCompleted = { onShowSuccess(order) })
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun PackageItemCard(index: Int, item: OrderPackage) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, UthOutlineVariant.copy(alpha = 0.8f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(shape = CircleShape, color = UthPrimaryContainer) {
                Text(
                    text = index.toString(),
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = UthPrimary
                )
            }
            Spacer(modifier = Modifier.width(11.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = UthOnSurface
                )
                Spacer(modifier = Modifier.height(7.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    PackageInfoChip("Số lượng: ${item.quantity}")
                    PackageInfoChip(
                        String.format(Locale.forLanguageTag("vi-VN"), "Khối lượng: %.2f kg", item.weightKg)
                            .replace(",00", "")
                    )
                }
                if (item.isFragile || item.isExpress || !item.notes.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        if (item.isFragile) PackageInfoChip("Dễ vỡ", warning = true)
                        if (item.isExpress) PackageInfoChip("Giao nhanh", warning = true)
                    }
                    item.notes?.takeIf { it.isNotBlank() }?.let { note ->
                        Text(
                            text = "Ghi chú: $note",
                            modifier = Modifier.padding(top = 7.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = UthOnSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PackageInfoChip(text: String, warning: Boolean = false) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (warning) UthWarningContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (warning) UthWarning else UthOnSurfaceVariant
        )
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
        "Nhận đơn" to (currentStatus in listOf(DeliveryStatus.DA_CHAP_NHAN, DeliveryStatus.DA_DEN_NHA_HANG, DeliveryStatus.DA_LAY_HANG, DeliveryStatus.DANG_VAN_CHUYEN, DeliveryStatus.DA_DEN_KHACH_HANG, DeliveryStatus.DA_GIAO)),
        "Điểm lấy" to (currentStatus in listOf(DeliveryStatus.DA_DEN_NHA_HANG, DeliveryStatus.DA_LAY_HANG, DeliveryStatus.DANG_VAN_CHUYEN, DeliveryStatus.DA_DEN_KHACH_HANG, DeliveryStatus.DA_GIAO)),
        "Lấy hàng" to (currentStatus in listOf(DeliveryStatus.DA_LAY_HANG, DeliveryStatus.DANG_VAN_CHUYEN, DeliveryStatus.DA_DEN_KHACH_HANG, DeliveryStatus.DA_GIAO)),
        "Đang giao" to (currentStatus in listOf(DeliveryStatus.DANG_VAN_CHUYEN, DeliveryStatus.DA_DEN_KHACH_HANG, DeliveryStatus.DA_GIAO)),
        "Điểm giao" to (currentStatus in listOf(DeliveryStatus.DA_DEN_KHACH_HANG, DeliveryStatus.DA_GIAO)),
        "Đã giao" to (currentStatus == DeliveryStatus.DA_GIAO)
    )

    val currentIndex = steps.indexOfLast { it.second }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        steps.forEachIndexed { index, (label, isCompleted) ->
            val isCurrent = index == currentIndex
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                // Top row with circle and connector lines
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    // Left connecting line
                    if (index > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .align(Alignment.CenterStart)
                                .height(2.5.dp)
                                .background(if (isCompleted || isCurrent) UthSuccess else UthOutlineVariant.copy(alpha = 0.6f))
                        )
                    }
                    // Right connecting line
                    if (index < steps.size - 1) {
                        val nextCompleted = steps[index + 1].second
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .align(Alignment.CenterEnd)
                                .height(2.5.dp)
                                .background(if (nextCompleted) UthSuccess else UthOutlineVariant.copy(alpha = 0.6f))
                        )
                    }

                    // Circle itself in center
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isCurrent -> UthPrimaryContainer
                                    isCompleted -> UthSuccess
                                    else -> UthOutlineVariant.copy(alpha = 0.8f)
                                }
                            )
                            .then(
                                if (isCurrent) Modifier.border(1.5.dp, UthPrimary, CircleShape)
                                else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted && !isCurrent) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                        } else {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isCurrent) UthPrimary else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.5.sp,
                    color = when {
                        isCurrent -> UthPrimary
                        isCompleted -> UthSuccess
                        else -> UthOnSurfaceVariant
                    },
                    fontWeight = if (isCompleted || isCurrent) FontWeight.Bold else FontWeight.Normal,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
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
        DeliveryStatus.DA_LAY_HANG -> Triple("Đã lấy hàng", UthPrimary, UthPrimaryContainer)
        DeliveryStatus.DANG_VAN_CHUYEN -> Triple("Đang vận chuyển", UthPrimary, UthPrimaryContainer)
        DeliveryStatus.DA_DEN_KHACH_HANG -> Triple("Đã đến điểm giao", UthWarning, UthWarningContainer)
        DeliveryStatus.DA_GIAO -> Triple("Đã giao thành công", UthSuccess, UthSuccessContainer)
        DeliveryStatus.DA_HUY -> Triple("Đã hủy", UthError, UthError.copy(alpha = 0.15f))
        DeliveryStatus.CHO_TIEP_NHAN -> Triple("Chờ tiếp nhận", UthOnSurfaceVariant, UthBackground)
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
