package com.mob10.deliveryapp.ui.driver

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.repeatOnLifecycle
import com.mob10.deliveryapp.R
import com.mob10.deliveryapp.formatServerTimestamp
import com.mob10.deliveryapp.data.local.entity.UserEntity
import com.mob10.deliveryapp.data.model.DeliveryStatus
import com.mob10.deliveryapp.data.model.Order
import com.mob10.deliveryapp.data.model.StatusHistory
import com.mob10.deliveryapp.ui.components.DashboardNavItem
import com.mob10.deliveryapp.ui.components.DashboardScaffold
import com.mob10.deliveryapp.ui.components.DashboardHeroCard
import com.mob10.deliveryapp.ui.components.GoDropHeader
import com.mob10.deliveryapp.ui.components.LottieOverlay
import com.mob10.deliveryapp.ui.components.MetricCard
import com.mob10.deliveryapp.ui.components.QuickActionCard
import com.mob10.deliveryapp.ui.components.SectionTitle
import com.mob10.deliveryapp.ui.components.StatusPill
import com.mob10.deliveryapp.ui.theme.Android08Theme
import com.mob10.deliveryapp.ui.theme.UthError
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
    onLogout: () -> Unit = {}
) {
    var selectedTab by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(0) }
    var notificationOrderId by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf<Long?>(null) }
    var showLoginAnimation by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val viewModel: DriverViewModel = viewModel(
        key = "driver_${currentUser?.id}",
        factory = DriverViewModelFactory(context)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Hiển thị Snackbar khi có thông báo (Accept / Reject / Error)
    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearAcceptMessage()
        }
    }
    LaunchedEffect(notifications.firstOrNull()?.id) {
        notifications.firstOrNull { !it.isRead }?.let { notification ->
            snackbarHostState.showSnackbar(notification.message)
        }
    }

    LaunchedEffect(currentUser?.id) {
        if (currentUser != null) {
            viewModel.loadDriverData()
        }
    }
    val lifecycle = androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(lifecycle) {
        lifecycle.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
            while (true) { viewModel.refreshData(); kotlinx.coroutines.delay(15_000) }
        }
    }
    androidx.activity.compose.BackHandler(selectedTab != 0) { selectedTab = 0 }

    LaunchedEffect(uiState.isSessionExpired) {
        if (uiState.isSessionExpired) onLogout()
    }

    val headerStatusText = when (uiState.driverStatus) {
        DriverWorkingStatus.AVAILABLE -> "Đang trực tuyến"
        DriverWorkingStatus.BUSY -> "Đang bận giao"
        DriverWorkingStatus.OFFLINE -> "Tạm nghỉ"
    }

    val headerStatusColor = when (uiState.driverStatus) {
        DriverWorkingStatus.AVAILABLE -> UthSuccess
        DriverWorkingStatus.BUSY -> UthWarning
        DriverWorkingStatus.OFFLINE -> UthOnSurfaceVariant
    }

    Box(modifier = Modifier.fillMaxSize()) {
        DashboardScaffold(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            navItems = listOf(
                DashboardNavItem("Trang chủ", Icons.Default.Home),
                DashboardNavItem("Đơn chờ", Icons.AutoMirrored.Filled.ListAlt),
                DashboardNavItem("Đang giao", Icons.Default.TwoWheeler),
                DashboardNavItem("Hồ sơ", Icons.Default.Person)
            ),
            header = {
                val headerSubtitle = when (selectedTab) {
                    1 -> if (uiState.driverStatus == DriverWorkingStatus.BUSY) "Đang thực hiện chuyến giao" else "Danh sách đơn hàng chờ nhận"
                    2 -> "Tiến trình đơn hàng đang giao"
                    3 -> "Thông tin tài khoản và hiệu suất"
                    4 -> "Lịch sử giao hàng và thu nhập"
                    else -> if (uiState.driverStatus == DriverWorkingStatus.BUSY) "Đang trong tiến trình giao hàng" else "Sẵn sàng làm việc hôm nay"
                }
                GoDropHeader(
                    roleLabel = "Khu vực tài xế",
                    name = currentUser?.fullName ?: "Tài xế",
                    subtitle = headerSubtitle,
                    statusLabel = headerStatusText,
                    statusColor = headerStatusColor,
                    showNotifications = true,
                    notifications = notifications,
                onNotificationsOpened = viewModel::markNotificationsRead,
                    onNotificationClick = {
                        viewModel.markNotificationRead(it.id)
                        notificationOrderId = it.orderId
                        selectedTab = 1
                    },
                    onProfileClick = { selectedTab = 3 },
                    onLogout = onLogout,
                    onRefresh = viewModel::refreshData
                )
            }
        ) {
            if (uiState.isRefreshing || uiState.actionInProgress) androidx.compose.material3.LinearProgressIndicator(Modifier.fillMaxWidth())
            uiState.errorMessage?.let { Text(it, color = UthError) }

            // Cảnh báo khi điểm tin cậy dưới 60
            if (uiState.statistics?.isWarning == true || uiState.statistics?.isLocked == true) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = UthWarningContainer,
                    border = BorderStroke(1.dp, UthWarning)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = UthWarning,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Điểm tin cậy thấp (${uiState.reliabilityScore}/100)",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge,
                                color = UthOnSurface
                            )
                            Text(
                                text = if (uiState.statistics?.isLocked == true) "Đang giới hạn nhận đơn đến ${formatServerTimestamp(uiState.statistics?.lockedUntil)}"
                                    else "Bạn đã từ chối ${uiState.rejectedCount} đơn. Hãy kiểm tra lý do trước khi từ chối.",
                                style = MaterialTheme.typography.bodySmall,
                                color = UthOnSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            if (selectedTab == 0) {
                uiState.activeOrders.firstOrNull()?.let { order ->
                    com.mob10.deliveryapp.ui.components.CurrentTripCard(order, onOpen = { selectedTab = 2 })
                }
                // ── Hero thu nhập ────────────────────────────────────
                DashboardHeroCard(
                    eyebrow = "Thu nhập hôm nay",
                    value = "${String.format(java.util.Locale.forLanguageTag("vi-VN"), "%,.0f", uiState.todayEarnings)}đ",
                    supportingText = "${uiState.deliveredTodayCount} chuyến hoàn thành • ${uiState.reliabilityScore}/100 tin cậy",
                    icon = Icons.Default.AccountBalanceWallet,
                    actionLabel = "Lịch sử",
                    onActionClick = { selectedTab = 4 }
                )

                // ── Hiệu suất ────────────────────────────────────────
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
                        value = String.format(java.util.Locale.forLanguageTag("vi-VN"), "%02d", uiState.pendingCount),
                        helper = if (uiState.pendingCount > 0) "Có thể nhận ngay" else "Không có đơn mới",
                        icon = Icons.Default.Inventory,
                        highlighted = uiState.pendingCount > 0
                    )
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        label = "Đã giao hôm nay",
                        value = String.format(java.util.Locale.forLanguageTag("vi-VN"), "%02d", uiState.deliveredTodayCount),
                        helper = "Hoàn thành trong ngày",
                        icon = Icons.Default.CheckCircle
                    )
                }

                // ── Trạng thái làm việc ──────────────────────────────
                Spacer(modifier = Modifier.height(4.dp))
                DriverShiftSelectorCard(
                    currentStatus = uiState.driverStatus,
                    onStatusChanged = { viewModel.setWorkingStatus(it) }
                )

                // ── Đơn đang thực hiện ───────────────────────────────
                val firstActiveOrder = uiState.activeOrders.firstOrNull()
                if (firstActiveOrder == null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
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

                // ── Thao tác nhanh ───────────────────────────────────
                SectionTitle(title = "Thao tác nhanh")
                QuickActionCard(
                    title = "Đơn đang chờ",
                    subtitle = "Xem ${uiState.newOrders.size} đơn trong khu vực của bạn",
                    icon = Icons.AutoMirrored.Filled.ListAlt,
                    onClick = { selectedTab = 1 }
                )
                QuickActionCard(
                    title = "Đơn của tôi (${uiState.activeOrders.size})",
                    subtitle = "Quản lý và cập nhật tiến trình đơn đang giao",
                    icon = Icons.AutoMirrored.Filled.Assignment,
                    onClick = { selectedTab = 2 }
                )
                QuickActionCard(
                    title = "Lịch sử giao hàng và thu nhập",
                    subtitle = "Xem lại tổng thu nhập và ${uiState.completedCount} đơn đã giao",
                    icon = Icons.Default.History,
                    onClick = { selectedTab = 4 }
                )
            } else if (selectedTab == 1) {
                if (uiState.isLoading) {
                    DriverLoadingState()
                } else {
                    notificationOrderId?.let { id ->
                        Text("Đơn từ thông báo #GD-$id", style = MaterialTheme.typography.titleSmall)
                        if (uiState.newOrders.none { it.id == id }) Text("Đơn này không còn chờ nhận. Có thể đã được tài xế khác tiếp nhận.", style = MaterialTheme.typography.bodySmall)
                        androidx.compose.material3.TextButton(onClick = { notificationOrderId = null }) { Text("Xem tất cả đơn chờ") }
                    }
                    NewOrdersTab(
                        newOrders = uiState.newOrders.filter { notificationOrderId == null || it.id == notificationOrderId },
                        rejectionReasons = uiState.rejectionReasons,
                        onAcceptOrder = { orderId -> viewModel.acceptOrder(orderId) },
                        onRejectOrder = { orderId, reason, note -> viewModel.rejectOrder(orderId, reason, note) },
                        driverStatus = uiState.driverStatus,
                        actionInProgress = uiState.actionInProgress,
                        rejectedOrderId = uiState.rejectedOrderId,
                        errorMessage = uiState.errorMessage
                    )
                }
            } else if (selectedTab == 2) {
                if (uiState.isLoading) {
                    DriverLoadingState()
                } else {
                    ActiveOrderTab(
                        activeOrders = uiState.activeOrders,
                        actionInProgress = uiState.actionInProgress,
                        onDelivered = viewModel::refreshData,
                        onUpdateStatus = { orderId, newStatus ->
                            viewModel.updateOrderStatus(orderId, newStatus)
                        }
                    )
                }
            } else if (selectedTab == 3) {
                DriverProfileTab(
                    currentUser = currentUser,
                    driverStatus = uiState.driverStatus,
                    onStatusChanged = { viewModel.setWorkingStatus(it) },
                    reliabilityScore = uiState.reliabilityScore.toInt(),
                    completedCount = uiState.completedCount,
                    rejectedCount = uiState.rejectedCount,
                    onLogout = onLogout
                )
            } else if (selectedTab == 4) {
                DeliveryHistoryTab(
                    historyOrders = uiState.historyOrders,
                    historiesByOrder = uiState.historiesByOrder,
                    totalEarnings = uiState.totalEarnings,
                    todayEarnings = uiState.todayEarnings,
                    completedCount = uiState.completedCount,
                    deliveredTodayCount = uiState.deliveredTodayCount
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 92.dp)
        )

        // Overlay animation chào mừng khi login thành công
        LottieOverlay(
            visible = showLoginAnimation,
            animationResId = R.raw.online_delivery_service,
            title = "Chào mừng, ${currentUser?.fullName ?: "Tài xế"}!",
            subtitle = "Chúc bạn có một ngày giao hàng hiệu quả",
            onDismiss = { showLoginAnimation = false }
        )
    } // end Box
} // end fun DriverHomeScreen

@Composable
private fun ActiveDeliveryCard(
    order: Order,
    history: StatusHistory? = null,
    onClick: () -> Unit = {}
) {
    val statusText = driverStatusLabel(order.status)
    val statusColor = when (order.status) {
        DeliveryStatus.DA_CHAP_NHAN -> UthPrimary
        DeliveryStatus.DA_DEN_NHA_HANG -> UthWarning
        DeliveryStatus.DA_LAY_HANG -> UthPrimary
        DeliveryStatus.DANG_VAN_CHUYEN -> UthPrimary
        DeliveryStatus.DA_DEN_KHACH_HANG -> UthWarning
        else -> UthOnSurfaceVariant
    }
    val statusContainerColor = when (order.status) {
        DeliveryStatus.DA_CHAP_NHAN -> UthPrimaryContainer
        DeliveryStatus.DA_DEN_NHA_HANG -> UthWarningContainer
        DeliveryStatus.DA_LAY_HANG -> UthPrimaryContainer
        DeliveryStatus.DANG_VAN_CHUYEN -> UthPrimaryContainer
        DeliveryStatus.DA_DEN_KHACH_HANG -> UthWarningContainer
        else -> UthOnSurfaceVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "#GD-${order.id}",
                        color = UthOnSurface,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Đơn đang thực hiện",
                        color = UthOnSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
                StatusPill(
                    text = statusText,
                    containerColor = statusContainerColor,
                    contentColor = statusColor,
                    dotColor = statusColor
                )
            }
            Spacer(modifier = Modifier.size(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(UthPrimary)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = order.pickupAddress.ifEmpty { "Chưa có địa chỉ lấy hàng" },
                    color = UthOnSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.size(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(UthSuccess)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = order.deliveryAddress.ifEmpty { "Chưa có địa chỉ giao" },
                    color = UthOnSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
            if (history != null) {
                Spacer(modifier = Modifier.size(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Lịch sử cập nhật",
                        tint = UthOnSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cập nhật: ${history.note ?: history.toStatus?.let(::driverStatusLabel).orEmpty()}",
                        color = UthOnSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

private fun driverStatusLabel(status: DeliveryStatus): String = when (status) {
    DeliveryStatus.CHO_TIEP_NHAN -> "Chờ tiếp nhận"
    DeliveryStatus.DA_CHAP_NHAN -> "Đã nhận đơn"
    DeliveryStatus.DA_DEN_NHA_HANG -> "Đã đến điểm lấy"
    DeliveryStatus.DA_LAY_HANG -> "Đã lấy hàng"
    DeliveryStatus.DANG_VAN_CHUYEN -> "Đang vận chuyển"
    DeliveryStatus.DA_DEN_KHACH_HANG -> "Đã đến điểm giao"
    DeliveryStatus.DA_GIAO -> "Đã giao"
    DeliveryStatus.DA_HUY -> "Đã hủy"
}

@Composable
private fun DriverLoadingState() {
    com.mob10.deliveryapp.ui.components.OrderLoadingSkeleton()
}

@Composable
private fun LegacyDriverLoadingState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            androidx.compose.material3.CircularProgressIndicator(color = UthPrimary)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Đang tải dữ liệu...",
                color = UthOnSurfaceVariant,
                fontSize = 13.sp
            )
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
