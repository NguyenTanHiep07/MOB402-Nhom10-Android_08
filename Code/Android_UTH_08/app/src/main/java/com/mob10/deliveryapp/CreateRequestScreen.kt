package com.mob10.deliveryapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val DarkBackground = Color(0xFF0F172A)
val CardBackground = Color(0xFF1E293B)
val PrimaryBlue = Color(0xFF2563EB)
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRequestScreen(
    onNextToConfirm: (
        senderName: String, senderPhone: String, senderAddress: String,
        receiverName: String, receiverPhone: String, receiverAddress: String,
        weight: String, packageType: String
    ) -> Unit
) {
    var senderName by remember { mutableStateOf("") }
    var senderPhone by remember { mutableStateOf("") }
    var senderAddress by remember { mutableStateOf("") }

    var receiverName by remember { mutableStateOf("") }
    var receiverPhone by remember { mutableStateOf("") }
    var receiverAddress by remember { mutableStateOf("") }

    var weightInput by remember { mutableStateOf("") }
    var selectedPackageType by remember { mutableStateOf("Tiêu chuẩn") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tạo yêu cầu giao hàng", color = TextPrimary, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // THÔNG TIN NGƯỜI GỬI
            CardSection(title = "THÔNG TIN NGƯỜI GỬI") {
                CustomTextField(value = senderName, onValueChange = { senderName = it }, label = "Họ tên người gửi")
                CustomTextField(value = senderPhone, onValueChange = { senderPhone = it }, label = "Số điện thoại", keyboardType = KeyboardType.Phone)
                CustomTextField(value = senderAddress, onValueChange = { senderAddress = it }, label = "Địa chỉ lấy hàng")
            }

            // THÔNG TIN NGƯỜI NHẬN
            CardSection(title = "THÔNG TIN NGƯỜI NHẬN") {
                CustomTextField(value = receiverName, onValueChange = { receiverName = it }, label = "Họ tên người nhận")
                CustomTextField(value = receiverPhone, onValueChange = { receiverPhone = it }, label = "Số điện thoại", keyboardType = KeyboardType.Phone)
                CustomTextField(value = receiverAddress, onValueChange = { receiverAddress = it }, label = "Địa chỉ giao hàng")
            }

            // CHI TIẾT HÀNG HÓA
            CardSection(title = "CHI TIẾT HÀNG HÓA") {
                CustomTextField(value = weightInput, onValueChange = { weightInput = it }, label = "Trọng lượng (kg)", keyboardType = KeyboardType.Number)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val types = listOf("Tiêu chuẩn", "Hàng dễ vỡ", "Hỏa tốc")
                    types.forEach { label ->
                        FilterChip(
                            selected = selectedPackageType == label,
                            onClick = { selectedPackageType = label },
                            label = { Text(label, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Button(
                onClick = {
                    onNextToConfirm(
                        senderName, senderPhone, senderAddress,
                        receiverName, receiverPhone, receiverAddress,
                        weightInput, selectedPackageType
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Tiếp tục xác nhận", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun CardSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
            unfocusedBorderColor = Color(0xFF334155),
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
        )
    )
}