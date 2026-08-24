package com.mob10.deliveryapp.ui.driver

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mob10.deliveryapp.data.local.entity.DeliveryRequestEntity
import com.mob10.deliveryapp.data.local.entity.StatusHistoryEntity
import com.mob10.deliveryapp.data.local.entity.UserEntity
import com.mob10.deliveryapp.data.model.DeliveryStatus
import com.mob10.deliveryapp.ui.components.DashboardNavItem
import com.mob10.deliveryapp.ui.components.DashboardScaffold
import com.mob10.deliveryapp.R
import com.mob10.deliveryapp.ui.components.GoDropHeader
import com.mob10.deliveryapp.ui.components.LottieOverlay
import com.mob10.deliveryapp.ui.components.MetricCard
import com.mob10.deliveryapp.ui.components.QuickActionCard
import com.mob10.deliveryapp.ui.components.SectionTitle
import com.mob10.deliveryapp.ui.components.StatusPill
import com.mob10.deliveryapp.ui.theme.Android08Theme
import com.mob10.deliveryapp.ui.theme.UthOnSurface
import com.mob10.deliveryapp.ui.theme.UthOnSurfaceVariant
import com.mob10.deliveryapp.ui.theme.UthPrimary
import com.mob10.deliveryapp.ui.theme.UthPrimaryContainer
import com.mob10.deliveryapp.ui.theme.UthSuccess
import com.mob10.deliveryapp.ui.theme.UthSuccessContainer
import com.mob10.deliveryapp.ui.theme.UthWarning
import com.mob10.deliveryapp.ui.theme.UthWarningContainer


@Composable
fun DriverHomeScreen(
    currentUser: UserEntity? = null,
    onLogout: () -> Unit = {},
    onUpdateProfile: (String, String, String, String) -> Unit = { _, _, _, _ -> }
) {
    var selectedTab by remember { mutableStateOf(0) }
    var isAvailable by remember { mutableStateOf(true) }
    var showLoginAnimation by remember { mutableStateOf(true) }
    
    val context = LocalContext.current
    val viewModel: DriverViewModel = viewModel(
        factory = DriverViewModelFactory(context)
    )
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Hiển thị Snackbar khi có thông báo Accept
    LaunchedEffect(uiState.acceptMessage) {
        uiState.acceptMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearAcceptMessage()
        }
    }

    LaunchedEffect(currentUser?.id) {
        if (currentUser != null) {
            viewModel.loadDriverData(currentUser.id)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    DashboardScaffold(
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it },
        navItems = listOf(
            DashboardNavItem("Trang chủ", Icons.Default.Home),
            DashboardNavItem("Đơn chờ", Icons.Default.ListAlt),
            DashboardNavItem("Đang giao", Icons.Default.LocalShipping),
            DashboardNavItem("Hồ sơ", Icons.Default.Person)
        ),
        header = {
            GoDropHeader(
                roleLabel = "Khu vực tài xế",
                name = currentUser?.fullName ?: "Tài xế",
                subtitle = if (selectedTab == 4) "Lịch sử giao hàng & Thu nhập" else "Sẵn sàng làm việc hôm nay",
                statusLabel = if (isAvailable) "Đang trực tuyến" else "Tạm nghỉ",
                statusColor = if (isAvailable) UthSuccess else UthWarning,
                onLogout = onLogout
            )
        }
    ) {
        // Snackbar overlay cho feedback Accept
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (selectedTab == 0) {
            SectionTitle(
                title = "Hiệu suất hôm nay",
                actionLabel = "Chi tiết",
                onActionClick = { selectedTab = 4 }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    modifier = Modifier.weight(1f),
                    label = "Đơn đang chờ",
                    value = "%02d".format(uiState.pendingCount),
                    helper = if (uiState.pendingCount > 0) "Có thể nhận ngay" else "Không có đơn mới",
                    icon = Icons.Default.Inventory,
                    highlighted = uiState.pendingCount > 0
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    label = "Đã giao hôm nay",
                    value = "%02d".format(uiState.deliveredTodayCount),
                    helper = "Hoàn thành trong ngày",
                    icon = Icons.Default.CheckCircle
                )
            }

            DriverAvailabilityCard(
                isAvailable = isAvailable,
                onAvailabilityChanged = { isAvailable = it }
            )

            SectionTitle(title = "Đơn đang thực hiện")
            val firstActiveOrder = uiState.activeOrders.firstOrNull()
            if (firstActiveOrder != null) {
                ActiveDeliveryCard(
                    order = firstActiveOrder,
                    history = uiState.historiesByOrder[firstActiveOrder.id]?.maxByOrNull { it.timestamp },
                    onClick = { selectedTab = 2 }
                )
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Chưa có đơn đang thực hiện",
                            color = UthOnSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            SectionTitle(title = "Thao tác nhanh")
            QuickActionCard(
                title = "Đơn đang chờ",
                subtitle = "Xem các đơn trong khu vực của bạn",
                icon = Icons.Default.ListAlt,
                onClick = { selectedTab = 1 }
            )
            QuickActionCard(
                title = "Đơn của tôi",
                subtitle = "Quản lý các đơn đã nhận",
                icon = Icons.Default.Assignment,
                onClick = { selectedTab = 2 }
            )
            QuickActionCard(
                title = "Cập nhật trạng thái",
                subtitle = "Thông báo tiến độ cho khách hàng",
                icon = Icons.Default.Update,
                onClick = { selectedTab = 2 }
            )
            QuickActionCard(
                title = "Lịch sử giao hàng",
                subtitle = "Xem lại hiệu suất và thu nhập",
                icon = Icons.Default.History,
                onClick = { selectedTab = 4 }
            )
        } else if (selectedTab == 1) {
            NewOrdersTab(
                newOrders = uiState.newOrders,
                packagesByOrder = uiState.packagesByOrder,
                onAcceptOrder = { orderId -> currentUser?.id?.let { viewModel.acceptOrder(orderId, it) } },
                onRejectOrder = { orderId -> viewModel.rejectOrder(orderId) }
            )
        } else if (selectedTab == 2) {
            ActiveOrderTab(
                activeOrders = uiState.activeOrders,
                packagesByOrder = uiState.packagesByOrder,
                onUpdateStatus = { orderId, newStatus ->
                    viewModel.updateOrderStatus(orderId, newStatus, driverId = currentUser?.id)
                }
            )
        } else if (selectedTab == 3) {
            DriverProfileTab(
                currentUser = currentUser,
                isAvailable = isAvailable,
                onAvailabilityChanged = { isAvailable = it },
                onLogout = onLogout,
                onUpdateProfile = onUpdateProfile
            )
        } else if (selectedTab == 4) {
            DeliveryHistoryTab(
                historyOrders = uiState.historyOrders,
                packagesByOrder = uiState.packagesByOrder,
                historiesByOrder = uiState.historiesByOrder,
                totalEarnings = uiState.totalEarnings,
                todayEarnings = uiState.todayEarnings,
                completedCount = uiState.completedCount,
                deliveredTodayCount = uiState.deliveredTodayCount
            )
        } else {
            // Placeholder for other tabs
            Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Tính năng đang phát triển", color = UthOnSurfaceVariant)
            }
        }
    }

    // Overlay animation chào mừng khi login thành công
    LottieOverlay(
        visible = showLoginAnimation,
        animationResId = R.raw.online_delivery_service,
        title = "Chào mừng, ${currentUser?.fullName ?: "Tài xế"}!",
        subtitle = "Chúc bạn có một ngày giao hàng hiệu quả \uD83D\uDE80",
        onDismiss = { showLoginAnimation = false }
    )
    } // end Box
}

@Composable
fun DriverAvailabilityCard(
    isAvailable: Boolean,
    onAvailabilityChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(if (isAvailable) UthSuccessContainer else UthWarningContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = null,
                    tint = if (isAvailable) UthSuccess else UthWarning,
                    modifier = Modifier.size(21.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isAvailable) "Bạn đang sẵn sàng" else "Bạn đang tạm nghỉ",
                    color = UthOnSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isAvailable) "Nhận đơn mới trong khu vực" else "Bật trạng thái để nhận đơn",
                    color = UthOnSurfaceVariant,
                    fontSize = 11.sp
                )
            }
            Switch(
                checked = isAvailable,
                onCheckedChange = onAvailabilityChanged,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = UthSuccess,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = MaterialTheme.colorScheme.outline
                )
            )
        }
    }
}

@Composable
private fun ActiveDeliveryCard(
    order: DeliveryRequestEntity,
    history: StatusHistoryEntity? = null,
    onClick: () -> Unit = {}
) {
    val statusText = when (order.status) {
        DeliveryStatus.DA_CHAP_NHAN -> "Đã nhận đơn"
        DeliveryStatus.DA_DEN_NHA_HANG -> "Đã đến quán"
        DeliveryStatus.DA_LAY_HANG -> "Đang giao"
        DeliveryStatus.DA_DEN_KHACH_HANG -> "Đã đến khách"
        else -> order.status.name
    }
    val statusColor = when (order.status) {
        DeliveryStatus.DA_CHAP_NHAN -> UthPrimary
        DeliveryStatus.DA_DEN_NHA_HANG -> UthWarning
        DeliveryStatus.DA_LAY_HANG -> UthPrimary
        DeliveryStatus.DA_DEN_KHACH_HANG -> UthWarning
        else -> UthOnSurfaceVariant
    }
    val statusContainerColor = when (order.status) {
        DeliveryStatus.DA_CHAP_NHAN -> UthPrimaryContainer
        DeliveryStatus.DA_DEN_NHA_HANG -> UthWarningContainer
        DeliveryStatus.DA_LAY_HANG -> UthPrimaryContainer
        DeliveryStatus.DA_DEN_KHACH_HANG -> UthWarningContainer
        else -> UthOnSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "#GD-${order.id}",
                        color = UthOnSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Đơn đang thực hiện",
                        color = UthOnSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
                StatusPill(
                    text = statusText,
                    containerColor = statusContainerColor,
                    contentColor = statusColor,
                    dotColor = statusColor
                )
            }
            Spacer(modifier = Modifier.size(13.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(UthPrimary)
                )
                Spacer(modifier = Modifier.width(9.dp))
                Text(
                    text = order.pickupAddress.ifEmpty { "Chưa có địa chỉ lấy hàng" },
                    color = UthOnSurface,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.size(7.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(UthSuccess)
                )
                Spacer(modifier = Modifier.width(9.dp))
                Text(
                    text = order.deliveryAddress.ifEmpty { "Chưa có địa chỉ giao" },
                    color = UthOnSurface,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            if (history != null) {
                Spacer(modifier = Modifier.size(13.dp))
                val dateFormat = remember { java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()) }
                val timeString = dateFormat.format(java.util.Date(history.timestamp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Lịch sử cập nhật",
                        tint = UthOnSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cập nhật lúc $timeString: ${history.note ?: "Chuyển sang ${history.toStatus.name}"}",
                        color = UthOnSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun DriverHomeScreenPreview() {
    Android08Theme {
        DriverHomeScreen()
    }
}

