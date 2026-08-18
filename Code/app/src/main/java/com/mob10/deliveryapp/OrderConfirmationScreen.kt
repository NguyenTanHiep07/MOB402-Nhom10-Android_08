package com.mob10.deliveryapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
    val submission by orderViewModel.submissionState.collectAsState()
    LaunchedEffect(submission.createdRequestId) {
        if (submission.createdRequestId != null) {
            orderViewModel.acknowledgeSubmission()
            onConfirmSuccess()
        }
    }
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
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                CardSection("LỘ TRÌNH GIAO HÀNG") {
                    Text("Điểm lấy: ${pendingOrder.senderAddress}", color = TextPrimary)
                    Text("${pendingOrder.senderName} · ${pendingOrder.senderPhone}", color = TextSecondary, fontSize = 13.sp)
                    Text("Điểm giao: ${pendingOrder.receiverAddress}", color = TextPrimary)
                    Text("${pendingOrder.receiverName} · ${pendingOrder.receiverPhone}", color = TextSecondary, fontSize = 13.sp)
                }
                CardSection("CHI TIẾT THANH TOÁN") {
                    ConfirmationFeeRow("Phí cơ bản", pendingOrder.feeQuote.baseFee)
                    ConfirmationFeeRow("Phí quãng đường", pendingOrder.feeQuote.distanceFee)
                    ConfirmationFeeRow("Phí trọng lượng", pendingOrder.feeQuote.weightFee)
                    if (pendingOrder.feeQuote.serviceFee > 0) ConfirmationFeeRow("Phụ phí dịch vụ", pendingOrder.feeQuote.serviceFee)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tổng cộng", color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text("${formatMoney(pendingOrder.feeQuote.totalFee)} đ", color = PrimaryBlue, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
                submission.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp) }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = orderViewModel::confirmOrder,
                    enabled = !submission.isSubmitting,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (submission.isSubmitting) "Đang tạo đơn..." else "Xác nhận đặt đơn", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                OutlinedButton(
                    onClick = onBackToEdit,
                    enabled = !submission.isSubmitting,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue)
                ) { Text("Hủy / Sửa lại") }
            }
        }
    }
}

@Composable
private fun ConfirmationFeeRow(label: String, amount: Long) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextSecondary)
        Text("${formatMoney(amount)} đ", color = TextPrimary)
    }
}
