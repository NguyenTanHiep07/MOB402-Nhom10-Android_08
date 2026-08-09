package com.mob10.deliveryapp.ui.customer

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.mob10.deliveryapp.ui.theme.UthPrimary
import com.mob10.deliveryapp.ui.theme.UthSecondary

@Composable
fun CustomerHomeScreen(customerName: String = "Khách hàng") {
    var selectedTab by remember { mutableStateOf(0) }

    DashboardScaffold(
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it },
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
                subtitle = "Quản lý giao hàng của bạn hôm nay"
            )
        }
    ) {
        SectionTitle(title = "Tổng quan đơn hàng", actionLabel = "Tháng này")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                modifier = Modifier.weight(1f),
                label = "Đang xử lý",
                value = "03",
                helper = "Cần theo dõi",
                icon = Icons.Default.PendingActions
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                label = "Đã hoàn tất",
                value = "12",
                helper = "+8% so với tháng trước",
                icon = Icons.Default.TaskAlt,
                highlighted = true
            )
        }

        CreateDeliveryCard()

        SectionTitle(title = "Lối tắt")
        QuickActionCard(
            title = "Danh sách đơn của tôi",
            subtitle = "Xem và quản lý các yêu cầu giao hàng",
            icon = Icons.Default.ListAlt
        )
        QuickActionCard(
            title = "Theo dõi trạng thái",
            subtitle = "Kiểm tra vị trí và tiến độ đơn hàng",
            icon = Icons.Default.LocalShipping
        )
        QuickActionCard(
            title = "Lịch sử hoạt động",
            subtitle = "Xem lại các đơn hàng đã hoàn tất",
            icon = Icons.Default.History
        )

        SectionTitle(title = "Đơn gần đây", actionLabel = "Xem tất cả")
        RecentOrderCard()
    }
}

@Composable
private fun CreateDeliveryCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = UthPrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 17.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(25.dp)
                )
            }
            Spacer(modifier = Modifier.width(13.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Tạo yêu cầu giao hàng",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.size(3.dp))
                Text(
                    text = "Gửi hàng nhanh chóng chỉ trong vài bước",
                    color = Color.White.copy(alpha = 0.76f),
                    fontSize = 11.sp
                )
            }
            Surface(
                shape = RoundedCornerShape(11.dp),
                color = Color.White
            ) {
                Text(
                    text = "Bắt đầu",
                    color = UthPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun RecentOrderCard() {
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
                        text = "#GD-1024",
                        color = UthOnSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Hôm nay, 09:40",
                        color = UthOnSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
                StatusPill(text = "Đang giao")
            }
            Spacer(modifier = Modifier.size(14.dp))
            OrderRouteRow(
                icon = Icons.Default.LocationOn,
                text = "12 Nguyễn Trãi, Quận 5",
                iconColor = UthPrimary
            )
            Spacer(modifier = Modifier.size(7.dp))
            OrderRouteRow(
                icon = Icons.Default.LocalShipping,
                text = "85 Lê Lợi, Quận 1",
                iconColor = UthSecondary
            )
            Spacer(modifier = Modifier.size(13.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Phí giao hàng",
                    color = UthOnSurfaceVariant,
                    fontSize = 11.sp
                )
                Text(
                    text = "55.000đ",
                    color = UthOnSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun OrderRouteRow(icon: ImageVector, text: String, iconColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(17.dp)
        )
        Spacer(modifier = Modifier.width(9.dp))
        Text(
            text = text,
            color = UthOnSurface,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun CustomerHomeScreenPreview() {
    Android08Theme {
        CustomerHomeScreen()
    }
}
