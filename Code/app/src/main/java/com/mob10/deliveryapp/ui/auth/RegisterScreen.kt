package com.mob10.deliveryapp.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mob10.deliveryapp.R
import com.mob10.deliveryapp.data.remote.dto.RegisterRequest
import com.mob10.deliveryapp.ui.theme.UthOnSurfaceVariant
import com.mob10.deliveryapp.ui.theme.UthOutline
import com.mob10.deliveryapp.ui.theme.UthPrimary
import com.mob10.deliveryapp.ui.theme.UthSecondaryContainer

@Composable
fun RegisterScreen(
    onRegister: (RegisterRequest) -> Unit,
    onBackToLogin: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?
) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var fullName by rememberSaveable { mutableStateOf("") }
    var phoneNumber by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var showValidationError by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopEnd)
                .offset(x = 92.dp, y = (-112).dp)
                .clip(CircleShape)
                .background(UthPrimary.copy(alpha = 0.12f))
        )
        Box(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.TopStart)
                .offset(x = (-76).dp, y = 110.dp)
                .clip(CircleShape)
                .background(UthSecondaryContainer)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(R.drawable.delivery_logo),
                        contentDescription = stringResource(R.string.login_logo_description),
                        modifier = Modifier.size(width = 92.dp, height = 78.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Đăng ký",
                        color = UthPrimary,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = "Tạo tài khoản mới để trải nghiệm GoDrop",
                        color = UthOnSurfaceVariant,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(26.dp))

                    RegisterTextField(
                        value = username,
                        onValueChange = { username = it; showValidationError = false },
                        label = "Tên đăng nhập",
                        keyboardType = KeyboardType.Text,
                        leadingIcon = Icons.Default.Person
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    RegisterTextField(
                        value = fullName,
                        onValueChange = { fullName = it; showValidationError = false },
                        label = "Họ và tên",
                        keyboardType = KeyboardType.Text,
                        leadingIcon = Icons.Default.Person
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    RegisterTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it; showValidationError = false },
                        label = "Số điện thoại",
                        keyboardType = KeyboardType.Phone,
                        leadingIcon = Icons.Default.Phone
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    RegisterTextField(
                        value = password,
                        onValueChange = { password = it; showValidationError = false },
                        label = "Mật khẩu",
                        keyboardType = KeyboardType.Password,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        leadingIcon = Icons.Default.Lock,
                        trailingContent = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = UthOnSurfaceVariant
                                )
                            }
                        }
                    )
                    if (errorMessage != null || showValidationError) {
                        Text(
                            text = errorMessage ?: "Vui lòng nhập đầy đủ thông tin",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 7.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            if (username.isBlank() || password.isBlank() || fullName.isBlank() || phoneNumber.isBlank()) {
                                showValidationError = true
                            } else {
                                onRegister(RegisterRequest(username.trim(), password, fullName.trim(), phoneNumber.trim()))
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(15.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = UthPrimary,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Đăng ký",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = onBackToLogin) {
                        Text(
                            text = "Đã có tài khoản? Đăng nhập",
                            color = UthPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RoleSelectionDialog(
    onCustomerSelected: () -> Unit,
    onDriverSelected: (String) -> Unit
) {
    var showDriverForm by remember { mutableStateOf(false) }
    var licensePlate by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { /* Must select */ },
        title = { Text(if (showDriverForm) "Đăng ký trở thành Tài Xế của GoDrop" else "Chào mừng bạn đến với GoDrop!") },
        text = {
            if (showDriverForm) {
                Column {
                    Text("Vui lòng nhập biển số xe của bạn để hoàn tất hồ sơ. Quản trị viên sẽ liên hệ để phê duyệt.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = licensePlate,
                        onValueChange = { licensePlate = it },
                        label = { Text("Biển số xe") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                Text("Cảm ơn bạn đã đăng ký tài khoản. Bạn muốn sử dụng GoDrop với vai trò nào?")
            }
        },
        confirmButton = {
            if (showDriverForm) {
                Button(onClick = { onDriverSelected(licensePlate) }, enabled = licensePlate.isNotBlank()) {
                    Text("Gửi yêu cầu")
                }
            } else {
                Button(onClick = { showDriverForm = true }) {
                    Text("Tài xế mới")
                }
            }
        },
        dismissButton = {
            if (showDriverForm) {
                TextButton(onClick = { showDriverForm = false }) {
                    Text("Quay lại")
                }
            } else {
                TextButton(onClick = onCustomerSelected) {
                    Text("Khách hàng mới")
                }
            }
        }
    )
}

@Composable
private fun RegisterTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingContent: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(text = label) },
        leadingIcon = {
            Icon(imageVector = leadingIcon, contentDescription = null)
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        trailingIcon = trailingContent,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = UthPrimary,
            unfocusedBorderColor = UthOutline,
            focusedLabelColor = UthPrimary,
            unfocusedLabelColor = UthOnSurfaceVariant,
            focusedLeadingIconColor = UthPrimary,
            unfocusedLeadingIconColor = UthOnSurfaceVariant,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        )
    )
}
