package com.mob10.deliveryapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mob10.deliveryapp.data.model.DeliveryStatus
import com.mob10.deliveryapp.ui.components.DashboardHeroCard
import com.mob10.deliveryapp.ui.components.DashboardNavItem
import com.mob10.deliveryapp.ui.components.DashboardScaffold
import com.mob10.deliveryapp.ui.components.GoDropHeader
import com.mob10.deliveryapp.ui.components.MetricCard
import com.mob10.deliveryapp.ui.components.QuickActionCard
import com.mob10.deliveryapp.ui.components.SectionTitle
import com.mob10.deliveryapp.ui.theme.UthOnSurface
import com.mob10.deliveryapp.ui.theme.UthOnSurfaceVariant

@Composable
fun ClientHomeScreen(
    customerName: String,
    orderViewModel: OrderViewModel,
    onCreateRequestClick: () -> Unit,
    onOrderListClick: () -> Unit,
    onTrackingClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLogout: () -> Unit
) {
    val orderList by orderViewModel.orderHistory.collectAsStateWithLifecycle()
    val notifications by orderViewModel.notifications.collectAsStateWithLifecycle()
    val pendingCount = orderList.count { it.status !in listOf(DeliveryStatus.DA_GIAO, DeliveryStatus.DA_HUY) }
    val completedCount = orderList.count { it.status == DeliveryStatus.DA_GIAO }

    DashboardScaffold(
        selectedTab = 0,
        onTabSelected = { tab ->
            when (tab) {
                1 -> onOrderListClick()
                2 -> onTrackingClick()
                3 -> onProfileClick()
            }
        },
        navItems = listOf(
            DashboardNavItem("Trang chủ", Icons.Default.Home),
            DashboardNavItem("Đơn hàng", Icons.AutoMirrored.Filled.ListAlt),
            DashboardNavItem("Theo dõi", Icons.Default.TwoWheeler),
            DashboardNavItem("Hồ sơ", Icons.Default.Person)
        ),
        header = {
            GoDropHeader(
                roleLabel = "Khu vực khách hàng",
                name = customerName,
                subtitle = "Quản lý giao hàng của bạn hôm nay",
                showNotifications = true,
                notifications = notifications,
                onNotificationsOpened = orderViewModel::markNotificationsRead,
                onNotificationClick = { orderViewModel.openNotification(it); onOrderListClick() },
                onProfileClick = onProfileClick,
                onLogout = onLogout
            )
        }
    ) {
        notifications.firstOrNull { !it.isRead }?.let { notification ->
            QuickActionCard(
                title = notification.title,
                subtitle = notification.message,
                icon = Icons.Default.Notifications,
                onClick = { orderViewModel.openNotification(notification); onOrderListClick() }
            )
        }
        // Hero Card
        DashboardHeroCard(
            eyebrow = "Gửi hàng nội thành",
            value = "Bạn gửi, GoDrop giao",
            supportingText = "Ước tính phí rõ ràng, theo dõi từng chặng",
            icon = Icons.Default.RocketLaunch,
            actionLabel = "Tạo đơn giao hàng", // Nổi bật hơn
            onActionClick = onCreateRequestClick
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        // Order Summary
        SectionTitle(title = "Tổng quan đơn hàng", actionLabel = "Hôm nay")
        if (orderList.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Bạn chưa có đơn hàng nào",
                        color = UthOnSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(
                    modifier = Modifier.weight(1f),
                    label = "Đang xử lý",
                    value = String.format(java.util.Locale.forLanguageTag("vi-VN"), "%02d", pendingCount),
                    helper = if (pendingCount > 0) "Đang giao/chuẩn bị" else "Không có đơn chờ",
                    icon = Icons.Default.PendingActions,
                    highlighted = pendingCount > 0
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    label = "Đã hoàn tất",
                    value = String.format(java.util.Locale.forLanguageTag("vi-VN"), "%02d", completedCount),
                    helper = "Tổng số đơn đã giao",
                    icon = Icons.Default.TaskAlt
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Actions
        SectionTitle(title = "Thao tác nhanh")
        QuickActionCard(
            title = "Danh sách đơn của tôi",
            subtitle = "Xem lại tất cả yêu cầu giao hàng",
            icon = Icons.AutoMirrored.Filled.ListAlt,
            onClick = onOrderListClick
        )
        if (pendingCount > 0) {
            QuickActionCard(
                title = "Theo dõi đơn hàng ($pendingCount)",
                subtitle = "Kiểm tra tiến độ đơn hàng đang giao trực tiếp",
                icon = Icons.Default.TwoWheeler,
                onClick = onTrackingClick
            )
        }
        QuickActionCard(
            title = "Lịch sử hoạt động",
            subtitle = "Xem lại các cập nhật trạng thái đã qua",
            icon = Icons.Default.History,
            onClick = onOrderListClick
        )
    }
}
