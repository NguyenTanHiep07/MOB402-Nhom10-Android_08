package com.mob10.deliveryapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientHomeScreen(
    orderViewModel: OrderViewModel,
    onCreateRequestClick: () -> Unit,
    onOrderListClick: () -> Unit
) {
    // Lấy danh sách đơn hàng thực tế
    val orderList by orderViewModel.orderHistory.collectAsState()

    // Đếm số đơn thực tế (Tự động cập nhật khi tạo đơn mới)
    val pendingCount = orderList.count { it.status == "Đang xử lý" }
    val completedCount = orderList.count { it.status == "Đã hoàn tất" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("≡  UTH Delivery", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("👤", fontSize = 18.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // LỜI CHÀO
            Column {
                Text("Xin chào, Khách hàng 👋", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Quản lý giao hàng của bạn hôm nay", color = TextSecondary, fontSize = 13.sp)
            }

            // 2 KHUNG THỐNG KÊ (Hiển thị số liệu thực tế)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Đơn đang xử lý", color = TextSecondary, fontSize = 12.sp)
                            Text("🕒", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("$pendingCount", color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Đơn hoàn tất", color = TextSecondary, fontSize = 12.sp)
                            Text("☑", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("$completedCount", color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // NÚT TẠO ĐƠN
            Button(
                onClick = onCreateRequestClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("+ Tạo yêu cầu giao hàng", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            // TRUY CẬP NHANH
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("TRUY CẬP NHANH", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                QuickAccessCard(
                    title = "Danh sách đơn của tôi",
                    subtitle = "Xem tất cả các yêu cầu",
                    onClick = onOrderListClick
                )

                QuickAccessCard(
                    title = "Theo dõi trạng thái",
                    subtitle = "Định vị trực tiếp đơn hàng",
                    onClick = onOrderListClick
                )

                QuickAccessCard(
                    title = "Lịch sử trạng thái",
                    subtitle = "Báo cáo & thống kê sự cố",
                    onClick = onOrderListClick
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAccessCard(title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, color = TextSecondary, fontSize = 12.sp)
            }
            Text(">", color = TextSecondary, fontSize = 14.sp)
        }
    }
}