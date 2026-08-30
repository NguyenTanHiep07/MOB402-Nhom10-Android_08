package com.mob10.deliveryapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mob10.deliveryapp.ui.theme.UthBackground
import com.mob10.deliveryapp.ui.theme.UthError
import com.mob10.deliveryapp.ui.theme.UthErrorContainer
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
                title = {
                    Column {
                        Text("Tạo đơn giao hàng", color = TextPrimary, fontWeight = FontWeight.ExtraBold)
                        Text("Bước 1/2 • Nhập thông tin", color = TextSecondary, fontSize = 12.sp)
                    }
                },
                navigationIcon = { 
                    TextButton(onClick = onBack) { 
                        Text("Hủy", color = TextSecondary, fontWeight = FontWeight.Bold) 
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            
            CardSection("Thông tin người gửi") {
                CustomTextField(state.senderName, viewModel::onSenderNameChanged, "Họ tên người gửi", icon = Icons.Default.Person)
                CustomTextField(state.senderPhone, viewModel::onSenderPhoneChanged, "Số điện thoại", KeyboardType.Phone, icon = Icons.Default.Phone)
                CustomTextField(state.pickupAddress, viewModel::onPickupAddressChanged, "Địa chỉ lấy hàng", icon = Icons.Default.LocationOn)
            }
            
            CardSection("Thông tin người nhận") {
                CustomTextField(state.receiverName, viewModel::onReceiverNameChanged, "Họ tên người nhận", icon = Icons.Default.Person)
                CustomTextField(state.receiverPhone, viewModel::onReceiverPhoneChanged, "Số điện thoại", KeyboardType.Phone, icon = Icons.Default.Phone)
                CustomTextField(state.deliveryAddress, viewModel::onDeliveryAddressChanged, "Địa chỉ giao hàng", icon = Icons.Default.LocationOn)
            }
            
            CardSection("Chi tiết hàng hóa") {
                CustomTextField(state.weight, viewModel::onWeightChanged, "Trọng lượng (kg)", KeyboardType.Decimal, icon = Icons.Default.Scale)
                CustomTextField(state.distanceKm, viewModel::onDistanceChanged, "Khoảng cách dự kiến (km)", KeyboardType.Decimal, icon = Icons.Default.Route)
                
                Text(
                    text = "Loại dịch vụ",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PackageType.entries.forEach { type ->
                        FilterChip(
                            selected = state.selectedService == type.displayName,
                            onClick = { viewModel.onServiceSelected(type.displayName) },
                            label = { Text(type.displayName, fontSize = 13.sp, fontWeight = FontWeight.Medium) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = UthPrimary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
            
            if (state.feeQuote.totalFee > 0) {
                CardSection("Tạm tính phí giao hàng") {
                    FeeRow("Phí cơ bản", state.feeQuote.baseFee)
                    FeeRow("Phí quãng đường", state.feeQuote.distanceFee)
                    FeeRow("Phí trọng lượng", state.feeQuote.weightFee)
                    if (state.feeQuote.serviceFee > 0) FeeRow("Phụ phí dịch vụ", state.feeQuote.serviceFee)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tổng cộng", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(String.format("%,.0fđ", state.feeQuote.totalFee.toDouble()), color = UthPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    }
                }
            }
            
            state.formError?.let { error ->
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
            
            Button(
                onClick = { if (viewModel.validateForm()) onContinueToConfirmation() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) { 
                Text("Tiếp tục xác nhận", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White) 
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FeeRow(label: String, amount: Long) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextSecondary, fontSize = 14.sp)
        Text(String.format("%,.0fđ", amount.toDouble()), color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun CardSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            content()
        }
    }
}

@Composable
fun CustomTextField(
    value: String, 
    onValueChange: (String) -> Unit, 
    label: String, 
    keyboardType: KeyboardType = KeyboardType.Text,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextSecondary, fontSize = 14.sp) },
        leadingIcon = icon?.let { { Icon(it, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp)) } },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryBlue,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedLabelColor = PrimaryBlue,
            unfocusedLabelColor = TextSecondary,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        )
    )
}
