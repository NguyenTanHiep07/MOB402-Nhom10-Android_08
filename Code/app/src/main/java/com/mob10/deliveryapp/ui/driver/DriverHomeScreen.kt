package com.mob10.deliveryapp.ui.driver

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mob10.deliveryapp.data.local.entity.UserEntity
import com.mob10.deliveryapp.ui.components.DashboardNavItem
import com.mob10.deliveryapp.ui.components.DashboardScaffold
import com.mob10.deliveryapp.ui.components.GoDropHeader
import com.mob10.deliveryapp.ui.components.MetricCard
import com.mob10.deliveryapp.ui.components.QuickActionCard
import com.mob10.deliveryapp.ui.components.SectionTitle
import com.mob10.deliveryapp.ui.components.StatusPill
import com.mob10.deliveryapp.ui.theme.Android08Theme
import com.mob10.deliveryapp.ui.theme.UthOnSurface
import com.mob10.deliveryapp.ui.theme.UthOnSurfaceVariant
import com.mob10.deliveryapp.ui.theme.UthPrimary
import com.mob10.deliveryapp.ui.theme.UthSuccess
import com.mob10.deliveryapp.ui.theme.UthSuccessContainer
import com.mob10.deliveryapp.ui.theme.UthWarning
import com.mob10.deliveryapp.ui.theme.UthWarningContainer


@Composable
fun DriverHomeScreen(currentUser: UserEntity? = null, onLogout: () -> Unit = {}) {
    var selectedTab by remember { mutableStateOf(0) }
    var isAvailable by remember { mutableStateOf(true) }

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
                subtitle = "Sẵn sàng làm việc hôm nay",
                statusLabel = if (isAvailable) "Đang trực tuyến" else "Tạm nghỉ",
                statusColor = if (isAvailable) UthSuccess else UthWarning
            )
        }
    ) {
        if (selectedTab == 0) {
            SectionTitle(title = "Hiệu suất hôm nay", actionLabel = "Chi tiết")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    modifier = Modifier.weight(1f),
                    label = "Đơn đang chờ",
                    value = "12",
                    helper = "Có thể nhận ngay",
                    icon = Icons.Default.Inventory,
                    highlighted = true
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    label = "Đã giao hôm nay",
                    value = "08",
                    helper = "+2 so với hôm qua",
                    icon = Icons.Default.CheckCircle
                )
            }

            DriverAvailabilityCard(
                isAvailable = isAvailable,
                onAvailabilityChanged = { isAvailable = it }
            )

            SectionTitle(title = "Đơn đang thực hiện")
            ActiveDeliveryCard()

            SectionTitle(title = "Thao tác nhanh")
            QuickActionCard(
                title = "Đơn đang chờ",
                subtitle = "Xem các đơn trong khu vực của bạn",
                icon = Icons.Default.ListAlt
            )
            QuickActionCard(
                title = "Đơn của tôi",
                subtitle = "Quản lý các đơn đã nhận",
                icon = Icons.Default.Assignment
            )
            QuickActionCard(
                title = "Cập nhật trạng thái",
                subtitle = "Thông báo tiến độ cho khách hàng",
                icon = Icons.Default.Update
            )
            QuickActionCard(
                title = "Lịch sử giao hàng",
                subtitle = "Xem lại hiệu suất và thu nhập",
                icon = Icons.Default.History
            )
        } else if (selectedTab == 3) {
            DriverProfileTab(
                currentUser = currentUser,
                isAvailable = isAvailable,
                onAvailabilityChanged = { isAvailable = it },
                onLogout = onLogout
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
private fun ActiveDeliveryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "#GD-1018",
                        color = UthOnSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Đơn đang trên đường giao",
                        color = UthOnSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
                StatusPill(
                    text = "Đang lấy hàng",
                    containerColor = UthWarningContainer,
                    contentColor = UthWarning,
                    dotColor = UthWarning
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
                    text = "24 Trần Hưng Đạo, Quận 1",
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
                    text = "108 Võ Văn Tần, Quận 3",
                    color = UthOnSurface,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
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
