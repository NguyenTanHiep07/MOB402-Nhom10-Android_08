package com.mob10.deliveryapp.ui.admin

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.mob10.deliveryapp.ui.theme.UthSecondary
import com.mob10.deliveryapp.ui.theme.UthSecondaryContainer
import com.mob10.deliveryapp.ui.theme.UthWarning
import com.mob10.deliveryapp.ui.theme.UthWarningContainer

@Composable
fun AdminHomeScreen(adminName: String = "Quản trị viên") {
    var selectedTab by remember { mutableStateOf(0) }

    DashboardScaffold(
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it },
        navItems = listOf(
            DashboardNavItem("Tổng quan", Icons.Default.Home),
            DashboardNavItem("Yêu cầu", Icons.Default.ListAlt),
            DashboardNavItem("Người dùng", Icons.Default.Person),
            DashboardNavItem("Cài đặt", Icons.Default.Settings)
        ),
        header = {
            GoDropHeader(
                roleLabel = "Trung tâm quản trị",
                name = adminName,
                subtitle = "Tổng quan hệ thống ngày hôm nay"
            )
        }
    ) {
        SectionTitle(title = "Sức khoẻ hệ thống", actionLabel = "Cập nhật vừa xong")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                modifier = Modifier.weight(1f),
                label = "Tổng yêu cầu",
                value = "1,248",
                helper = "+12% tuần này",
                icon = Icons.Default.Inventory
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                label = "Người dùng",
                value = "8,592",
                helper = "+5 hôm nay",
                icon = Icons.Default.Person
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                modifier = Modifier.weight(1f),
                label = "Chờ phân công",
                value = "42",
                helper = "Cần xử lý ngay",
                icon = Icons.Default.PendingActions,
                highlighted = true
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                label = "Tài xế hoạt động",
                value = "156",
                helper = "Đang trực tuyến",
                icon = Icons.Default.LocalShipping
            )
        }

        AdminAttentionCard()

        SectionTitle(title = "Quản trị nhanh")
        QuickActionCard(
            title = "Toàn bộ yêu cầu",
            subtitle = "Theo dõi và xử lý các đơn giao hàng",
            icon = Icons.Default.ListAlt
        )
        QuickActionCard(
            title = "Quản lý người dùng",
            subtitle = "Khách hàng, tài xế và phân quyền",
            icon = Icons.Default.Person
        )
        QuickActionCard(
            title = "Phân công đơn",
            subtitle = "Điều phối đơn cho tài xế phù hợp",
            icon = Icons.Default.Assignment
        )
        QuickActionCard(
            title = "Quy tắc tính phí",
            subtitle = "Cấu hình bảng giá và khu vực giao",
            icon = Icons.Default.Info
        )
    }
}

@Composable
private fun AdminAttentionCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(19.dp),
        colors = CardDefaults.cardColors(containerColor = UthWarningContainer),
        border = BorderStroke(1.dp, Color(0xFFFDE68A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(UthSecondaryContainer, RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PendingActions,
                    contentDescription = null,
                    tint = UthSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "42 yêu cầu đang chờ phân công",
                    color = UthOnSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Kiểm tra để đảm bảo giao đúng thời gian",
                    color = UthOnSurfaceVariant,
                    fontSize = 11.sp
                )
            }
            StatusPill(
                text = "Ưu tiên",
                containerColor = Color.White.copy(alpha = 0.65f),
                contentColor = UthWarning,
                dotColor = UthWarning
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun AdminHomeScreenPreview() {
    Android08Theme {
        AdminHomeScreen()
    }
}
