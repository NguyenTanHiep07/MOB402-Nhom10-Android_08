package com.mob10.deliveryapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mob10.deliveryapp.ui.theme.UthBackground
import com.mob10.deliveryapp.ui.theme.UthOnSurface
import com.mob10.deliveryapp.ui.theme.UthOnSurfaceVariant
import com.mob10.deliveryapp.ui.theme.UthPrimary
import com.mob10.deliveryapp.ui.theme.UthSurface

val DarkBackground = UthBackground
val CardBackground = UthSurface
val PrimaryBlue = UthPrimary
val TextPrimary = UthOnSurface
val TextSecondary = UthOnSurfaceVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRequestScreen(
    viewModel: CreateRequestViewModel,
    onBack: () -> Unit,
    onContinueToConfirmation: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tạo yêu cầu giao hàng", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = { TextButton(onClick = onBack) { Text("Hủy", color = TextPrimary) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CardSection("THÔNG TIN NGƯỜI GỬI") {
                CustomTextField(state.senderName, viewModel::onSenderNameChanged, "Họ tên người gửi")
                CustomTextField(state.senderPhone, viewModel::onSenderPhoneChanged, "Số điện thoại", KeyboardType.Phone)
                CustomTextField(state.pickupAddress, viewModel::onPickupAddressChanged, "Địa chỉ lấy hàng")
            }
            CardSection("THÔNG TIN NGƯỜI NHẬN") {
                CustomTextField(state.receiverName, viewModel::onReceiverNameChanged, "Họ tên người nhận")
                CustomTextField(state.receiverPhone, viewModel::onReceiverPhoneChanged, "Số điện thoại", KeyboardType.Phone)
                CustomTextField(state.deliveryAddress, viewModel::onDeliveryAddressChanged, "Địa chỉ giao hàng")
            }
            CardSection("CHI TIẾT HÀNG HÓA") {
                CustomTextField(state.weight, viewModel::onWeightChanged, "Trọng lượng (kg)", KeyboardType.Decimal)
                CustomTextField(state.distanceKm, viewModel::onDistanceChanged, "Khoảng cách dự kiến (km)", KeyboardType.Decimal)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PackageType.entries.forEach { type ->
                        FilterChip(
                            selected = state.selectedService == type.displayName,
                            onClick = { viewModel.onServiceSelected(type.displayName) },
                            label = { Text(type.displayName, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            if (state.feeQuote.totalFee > 0) {
                CardSection("TẠM TÍNH PHÍ GIAO HÀNG") {
                    FeeRow("Phí cơ bản", state.feeQuote.baseFee)
                    FeeRow("Phí quãng đường", state.feeQuote.distanceFee)
                    FeeRow("Phí trọng lượng", state.feeQuote.weightFee)
                    if (state.feeQuote.serviceFee > 0) FeeRow("Phụ phí dịch vụ", state.feeQuote.serviceFee)
                    Text("Tổng cộng: ${formatMoney(state.feeQuote.totalFee)} đ", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            }
            state.formError?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp) }
            Button(
                onClick = { if (viewModel.validateForm()) onContinueToConfirmation() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Tiếp tục xác nhận", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White) }
        }
    }
}

@Composable
private fun FeeRow(label: String, amount: Long) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextSecondary)
        Text("${formatMoney(amount)} đ", color = TextPrimary)
    }
}

@Composable
fun CardSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
            content()
        }
    }
}

@Composable
fun CustomTextField(value: String, onValueChange: (String) -> Unit, label: String, keyboardType: KeyboardType = KeyboardType.Text) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextSecondary) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryBlue,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedLabelColor = PrimaryBlue,
            unfocusedLabelColor = TextSecondary
        )
    )
}
