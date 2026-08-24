package com.mob10.deliveryapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mob10.deliveryapp.data.model.DeliveryStatus
import com.mob10.deliveryapp.ui.components.DashboardNavItem
import com.mob10.deliveryapp.ui.components.DashboardScaffold
import com.mob10.deliveryapp.ui.components.GoDropHeader
import com.mob10.deliveryapp.ui.components.MetricCard
import com.mob10.deliveryapp.ui.components.QuickActionCard
import com.mob10.deliveryapp.ui.components.SectionTitle
import com.mob10.deliveryapp.ui.theme.UthPrimary

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
    val orderList by orderViewModel.orderHistory.collectAsState()
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
            DashboardNavItem("Đơn hàng", Icons.Default.ListAlt),
            DashboardNavItem("Theo dõi", Icons.Default.LocalShipping),
            DashboardNavItem("Hồ sơ", Icons.Default.Person)
        ),
        header = {
            GoDropHeader(
                roleLabel = "Khu vực khách hàng",
                name = customerName,
                subtitle = "Quản lý giao hàng của bạn hôm nay",
                onProfileClick = onProfileClick,
                onLogout = onLogout
            )
        }
    ) {
        SectionTitle(title = "Tổng quan đơn hàng", actionLabel = "Hôm nay")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                modifier = Modifier.weight(1f),
                label = "Đang xử lý",
                value = "%02d".format(pendingCount),
                helper = if (pendingCount > 0) "Cần theo dõi" else "Không có đơn chờ",
                icon = Icons.Default.PendingActions,
                highlighted = pendingCount > 0
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                label = "Đã hoàn tất",
                value = "%02d".format(completedCount),
                helper = "Tổng số đơn đã giao",
                icon = Icons.Default.TaskAlt
            )
        }
        ClientCreateDeliveryCard(onCreateRequestClick)
        SectionTitle(title = "Thao tác nhanh")
        QuickActionCard("Danh sách đơn của tôi", "Xem tất cả yêu cầu giao hàng", Icons.Default.ListAlt, onOrderListClick)
        QuickActionCard("Theo dõi trạng thái", "Kiểm tra tiến độ đơn hàng", Icons.Default.LocalShipping, onTrackingClick)
        QuickActionCard("Lịch sử hoạt động", "Xem lại các cập nhật trạng thái", Icons.Default.History, onOrderListClick)
    }
}

@Composable
private fun ClientCreateDeliveryCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = UthPrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 17.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(25.dp)) }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text("Tạo yêu cầu giao hàng", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Gửi hàng nhanh chóng chỉ trong vài bước", color = Color.White.copy(alpha = 0.76f), fontSize = 11.sp)
            }
            Surface(shape = RoundedCornerShape(11.dp), color = Color.White) {
                Text("Bắt đầu", color = UthPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp))
            }
        }
    }
}
