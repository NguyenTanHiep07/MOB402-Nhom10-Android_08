package com.mob10.deliveryapp.ui.driver

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mob10.deliveryapp.data.local.entity.DeliveryRequestEntity
import com.mob10.deliveryapp.data.local.entity.PackageEntity
import com.mob10.deliveryapp.data.local.entity.StatusHistoryEntity
import com.mob10.deliveryapp.data.model.DeliveryStatus
import com.mob10.deliveryapp.ui.components.MetricCard
import com.mob10.deliveryapp.ui.components.SectionTitle
import com.mob10.deliveryapp.ui.components.StatusPill
import com.mob10.deliveryapp.ui.theme.UthError
import com.mob10.deliveryapp.ui.theme.UthOnSurface
import com.mob10.deliveryapp.ui.theme.UthOnSurfaceVariant
import com.mob10.deliveryapp.ui.theme.UthPrimary
import com.mob10.deliveryapp.ui.theme.UthSecondary
import com.mob10.deliveryapp.ui.theme.UthSuccess
import com.mob10.deliveryapp.ui.theme.UthSuccessContainer
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class HistoryFilter {
    ALL,
    COMPLETED,
    CANCELLED
}

@Composable
fun DeliveryHistoryTab(
    historyOrders: List<DeliveryRequestEntity>,
    packagesByOrder: Map<Int, List<PackageEntity>>,
    historiesByOrder: Map<Int, List<StatusHistoryEntity>>,
    totalEarnings: Double,
    todayEarnings: Double,
    completedCount: Int,
    deliveredTodayCount: Int
) {
    var currentFilter by remember { mutableStateOf(HistoryFilter.ALL) }

    val filteredOrders = remember(historyOrders, currentFilter) {
        when (currentFilter) {
            HistoryFilter.ALL -> historyOrders
            HistoryFilter.COMPLETED -> historyOrders.filter { it.status == DeliveryStatus.DA_GIAO }
            HistoryFilter.CANCELLED -> historyOrders.filter { it.status == DeliveryStatus.DA_HUY }
        }
    }

    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN")) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Summary Header Cards
        SectionTitle(title = "Tổng quan thu nhập")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                modifier = Modifier.weight(1f),
                label = "Tổng thu nhập",
                value = currencyFormatter.format(totalEarnings),
                helper = "Từ $completedCount đơn hoàn thành",
                icon = Icons.Default.Payments,
                highlighted = totalEarnings > 0
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                label = "Thu nhập hôm nay",
                value = currencyFormatter.format(todayEarnings),
                helper = "$deliveredTodayCount đơn trong ngày",
                icon = Icons.Default.CheckCircle
            )
        }

        // Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = currentFilter == HistoryFilter.ALL,
                onClick = { currentFilter = HistoryFilter.ALL },
                label = { Text("Tất cả (${historyOrders.size})") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = UthPrimary,
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = currentFilter == HistoryFilter.COMPLETED,
                onClick = { currentFilter = HistoryFilter.COMPLETED },
                label = { Text("Đã giao (${historyOrders.count { it.status == DeliveryStatus.DA_GIAO }})") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = UthSuccess,
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = currentFilter == HistoryFilter.CANCELLED,
                onClick = { currentFilter = HistoryFilter.CANCELLED },
                label = { Text("Đã hủy (${historyOrders.count { it.status == DeliveryStatus.DA_HUY }})") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = UthError,
                    selectedLabelColor = Color.White
                )
            )
        }

        // History List
        if (filteredOrders.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = UthOnSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Không có lịch sử giao hàng",
                        fontWeight = FontWeight.Bold,
                        color = UthOnSurface,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Các đơn hàng đã hoàn tất hoặc đã hủy sẽ hiển thị ở đây.",
                        color = UthOnSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            filteredOrders.forEach { order ->
                HistoryOrderCard(
                    order = order,
                    packages = packagesByOrder[order.id] ?: emptyList(),
                    histories = historiesByOrder[order.id] ?: emptyList()
                )
            }
        }
    }
}

@Composable
private fun HistoryOrderCard(
    order: DeliveryRequestEntity,
    packages: List<PackageEntity>,
    histories: List<StatusHistoryEntity>
) {
    var isExpanded by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN")) }

    val displayTime = order.actualDeliveryTime?.let { dateFormat.format(Date(it)) }
        ?: dateFormat.format(Date(order.createdAt))

    val isCompleted = order.status == DeliveryStatus.DA_GIAO

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "#GD-${order.id}",
                        fontWeight = FontWeight.Bold,
                        color = UthOnSurface,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = UthOnSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = displayTime,
                            color = UthOnSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }

                StatusPill(
                    text = if (isCompleted) "Đã giao" else "Đã hủy",
                    containerColor = if (isCompleted) UthSuccessContainer else MaterialTheme.colorScheme.errorContainer,
                    contentColor = if (isCompleted) UthSuccess else UthError,
                    dotColor = if (isCompleted) UthSuccess else UthError
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Route addresses
            AddressRow(
                icon = Icons.Default.LocationOn,
                iconTint = UthPrimary,
                label = "Lấy hàng: ",
                address = order.pickupAddress.ifEmpty { "Địa chỉ lấy hàng" },
                contact = if (order.senderName.isNotEmpty()) "${order.senderName} (${order.senderPhone})" else null
            )
            Spacer(modifier = Modifier.height(6.dp))
            AddressRow(
                icon = Icons.Default.LocalShipping,
                iconTint = UthSecondary,
                label = "Giao hàng: ",
                address = order.deliveryAddress.ifEmpty { "Địa chỉ giao hàng" },
                contact = if (order.recipientName.isNotEmpty()) "${order.recipientName} (${order.recipientPhone})" else null
            )

            // Packages preview
            if (packages.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = UthOnSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Kiện hàng: " + packages.joinToString { "${it.quantity}x ${it.name}" },
                        color = UthOnSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer: Cost & Expand Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Thu nhập: ",
                        color = UthOnSurfaceVariant,
                        fontSize = 12.sp
                    )
                    Text(
                        text = currencyFormatter.format(order.totalCost),
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted) UthSuccess else UthOnSurfaceVariant,
                        fontSize = 14.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = if (isExpanded) "Thu gọn" else "Tiến trình (${histories.size})",
                        color = UthPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = UthPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Expandable History Timeline
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Lịch sử cập nhật trạng thái",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = UthOnSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (histories.isEmpty()) {
                        Text(
                            text = "Chưa có thông tin tiến trình",
                            fontSize = 11.sp,
                            color = UthOnSurfaceVariant
                        )
                    } else {
                        val sortedHistories = histories.sortedBy { it.timestamp }
                        sortedHistories.forEachIndexed { index, history ->
                            val isLast = index == sortedHistories.size - 1
                            val statusTime = timeFormat.format(Date(history.timestamp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (isLast) UthPrimary else UthOnSurfaceVariant)
                                    )
                                    if (!isLast) {
                                        Box(
                                            modifier = Modifier
                                                .width(1.dp)
                                                .height(24.dp)
                                                .background(UthOnSurfaceVariant.copy(alpha = 0.4f))
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = getStatusLabel(history.toStatus),
                                            fontWeight = if (isLast) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 12.sp,
                                            color = if (isLast) UthPrimary else UthOnSurface
                                        )
                                        Text(
                                            text = statusTime,
                                            fontSize = 11.sp,
                                            color = UthOnSurfaceVariant
                                        )
                                    }
                                    history.note?.takeIf { it.isNotBlank() }?.let { note ->
                                        Text(
                                            text = note,
                                            fontSize = 11.sp,
                                            color = UthOnSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getStatusLabel(status: DeliveryStatus): String = when (status) {
    DeliveryStatus.CHO_TIEP_NHAN -> "Tạo đơn hàng"
    DeliveryStatus.DA_CHAP_NHAN -> "Tài xế nhận đơn"
    DeliveryStatus.DA_DEN_NHA_HANG -> "Tài xế tới điểm lấy hàng"
    DeliveryStatus.DA_LAY_HANG -> "Đã lấy hàng"
    DeliveryStatus.DA_DEN_KHACH_HANG -> "Đã tới điểm giao"
    DeliveryStatus.DA_GIAO -> "Giao hàng thành công"
    DeliveryStatus.DA_HUY -> "Đã hủy đơn"
}

@Composable
private fun AddressRow(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    address: String,
    contact: String? = null
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Row {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = UthOnSurface
                )
                Text(
                    text = address,
                    fontSize = 12.sp,
                    color = UthOnSurface
                )
            }
            contact?.takeIf { it.isNotBlank() }?.let { contactText ->
                Text(
                    text = contactText,
                    fontSize = 11.sp,
                    color = UthOnSurfaceVariant
                )
            }
        }
    }
}
