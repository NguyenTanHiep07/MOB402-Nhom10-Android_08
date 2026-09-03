package com.mob10.deliveryapp

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mob10.deliveryapp.ui.theme.UthBackground
import com.mob10.deliveryapp.ui.theme.UthError
import com.mob10.deliveryapp.ui.theme.UthErrorContainer
import com.mob10.deliveryapp.ui.theme.UthOnSurface
import com.mob10.deliveryapp.ui.theme.UthOnSurfaceVariant
import com.mob10.deliveryapp.ui.theme.UthPrimary
import com.mob10.deliveryapp.ui.theme.UthPrimaryContainer
import com.mob10.deliveryapp.ui.theme.UthSurface

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
                title = {
                    Column {
                        Text("Xác nhận đơn hàng", color = TextPrimary, fontWeight = FontWeight.ExtraBold)
                        Text("Bước 2/2 • Kiểm tra trước khi đặt", color = TextSecondary, fontSize = 12.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))
                
                CardSection("Lộ trình giao hàng") {
                    // Lấy hàng
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier.size(10.dp).clip(androidx.compose.foundation.shape.CircleShape).background(PrimaryBlue)
                            )
                            Box(
                                modifier = Modifier.width(2.dp).height(40.dp).background(UthPrimaryContainer)
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Điểm lấy: ${pendingOrder.senderAddress}",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "${pendingOrder.senderName} · ${pendingOrder.senderPhone}",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Giao hàng
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier.size(10.dp).clip(androidx.compose.foundation.shape.CircleShape).background(com.mob10.deliveryapp.ui.theme.UthSuccess)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Điểm giao: ${pendingOrder.receiverAddress}",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "${pendingOrder.receiverName} · ${pendingOrder.receiverPhone}",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                
                CardSection("Chi tiết hàng hóa & dịch vụ") {
                    ConfirmationDetailRow("Khoảng cách", "${pendingOrder.distanceKm} km")
                    ConfirmationDetailRow("Trọng lượng", "${pendingOrder.weightKg} kg")
                    ConfirmationDetailRow("Dịch vụ", pendingOrder.packageType.displayName)
                }
                
                CardSection("Chi tiết thanh toán") {
                    ConfirmationFeeRow("Phí cơ bản", pendingOrder.feeQuote.baseFee)
                    ConfirmationFeeRow("Phí quãng đường", pendingOrder.feeQuote.distanceFee)
                    ConfirmationFeeRow("Phí trọng lượng", pendingOrder.feeQuote.weightFee)
                    if (pendingOrder.feeQuote.serviceFee > 0) {
                        ConfirmationFeeRow("Phụ phí dịch vụ", pendingOrder.feeQuote.serviceFee)
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Tổng cộng", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(String.format("%,.0fđ", pendingOrder.feeQuote.totalFee.toDouble()), color = PrimaryBlue, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    }
                }
                
                submission.errorMessage?.let { error ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(UthErrorContainer)
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Lỗi",
                                tint = UthError,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = error,
                                color = UthError,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Buttons at the bottom
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkBackground)
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = orderViewModel::confirmOrder,
                    enabled = !submission.isSubmitting,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        if (submission.isSubmitting) "Đang tạo đơn..." else "Xác nhận đặt đơn", 
                        fontSize = 16.sp, 
                        fontWeight = FontWeight.Bold, 
                        color = Color.White
                    )
                }
                OutlinedButton(
                    onClick = onBackToEdit,
                    enabled = !submission.isSubmitting,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = UthOnSurfaceVariant
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) { 
                    Text("Quay lại chỉnh sửa", fontWeight = FontWeight.SemiBold, color = TextPrimary) 
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ConfirmationDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), 
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(label, color = TextSecondary, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            value, 
            color = TextPrimary, 
            fontSize = 14.sp, 
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ConfirmationFeeRow(label: String, amount: Long) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextSecondary, fontSize = 14.sp)
        Text(String.format("%,.0fđ", amount.toDouble()), color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}
