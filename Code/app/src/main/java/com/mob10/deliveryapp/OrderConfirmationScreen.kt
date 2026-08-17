package com.mob10.deliveryapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderConfirmationScreen(
    orderViewModel: OrderViewModel,
    onBackToEdit: () -> Unit,
    onConfirmSuccess: () -> Unit
) {
    val pendingOrder by orderViewModel.pendingOrder.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Xác nhận đơn hàng", color = TextPrimary, fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // LỘ TRÌNH GIAO HÀNG
                CardSection(title = "LỘ TRÌNH GIAO HÀNG") {
                    Text("📍 Điểm lấy: ${pendingOrder.senderAddress.ifEmpty { "Chưa nhập" }}", color = TextPrimary)
                    Text("👤 ${pendingOrder.senderName} - ${pendingOrder.senderPhone}", color = TextSecondary, fontSize = 13.sp)
                    HorizontalDivider(color = Color(0xFF334155))
                    Text("🎯 Điểm giao: ${pendingOrder.receiverAddress.ifEmpty { "Chưa nhập" }}", color = TextPrimary)
                    Text("👤 ${pendingOrder.receiverName} - ${pendingOrder.receiverPhone}", color = TextSecondary, fontSize = 13.sp)
                }

                // CHI TIẾT THANH TOÁN
                CardSection(title = "CHI TIẾT THANH TOÁN") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Phí cơ bản:", color = TextSecondary)
                        Text("${formatMoney(pendingOrder.shippingFee)} đ", color = TextPrimary)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Trọng lượng (${pendingOrder.weight.ifEmpty { "1" }} kg):", color = TextSecondary)
                        Text(pendingOrder.packageType, color = TextPrimary)
                    }

                    if (pendingOrder.extraFee > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Phụ phí hàng / trọng lượng:", color = TextSecondary)
                            Text("+${formatMoney(pendingOrder.extraFee)} đ", color = Color(0xFFEAB308))
                        }
                    }

                    HorizontalDivider(color = Color(0xFF334155))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tổng cộng:", color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${formatMoney(pendingOrder.totalFee)} đ",
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        orderViewModel.confirmOrder()
                        onConfirmSuccess()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Xác nhận đặt đơn", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                OutlinedButton(
                    onClick = onBackToEdit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Hủy / Sửa lại", color = TextSecondary)
                }
            }
        }
    }
}