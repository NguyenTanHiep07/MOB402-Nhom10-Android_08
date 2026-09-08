package com.mob10.deliveryapp.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SecretField(value: String, onChange: (String) -> Unit, label: String, enabled: Boolean = true) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(value, onChange, Modifier.fillMaxWidth(), label = { Text(label) }, singleLine = true,
        enabled = enabled, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = { IconButton(onClick = { visible = !visible }) {
            Icon(if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility, if (visible) "Ẩn mật khẩu" else "Hiện mật khẩu")
        } })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecoveryScreen(onBack: () -> Unit, vm: RecoveryViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val phone by vm.phone.collectAsStateWithLifecycle()
    val codeStep by vm.codeStep.collectAsStateWithLifecycle()
    val resendAt by vm.resendAt.collectAsStateWithLifecycle()
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(resendAt) { while (now < resendAt) { kotlinx.coroutines.delay(1000); now = System.currentTimeMillis() } }
    var code by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    LaunchedEffect(state.success) { if (state.success) { code = ""; password = ""; confirm = "" } }
    Scaffold(topBar = { TopAppBar(title = { Text("Khôi phục mật khẩu") }, navigationIcon = {
        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại đăng nhập") }
    }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(if (state.success) Icons.Default.CheckCircle else Icons.Default.LockReset, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
            Text(if (state.success) "Mật khẩu đã được cập nhật" else if (codeStep) "Xác minh và đặt mật khẩu mới" else "Lấy lại quyền truy cập", style = MaterialTheme.typography.headlineSmall)
            if (state.success) {
                Text(state.message.orEmpty()); Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Về đăng nhập") }
            } else {
                Text("Nhập số điện thoại đã đăng ký. Mã được gửi qua email bảo mật đã xác minh trong Hồ sơ, không gửi qua SMS.", style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(phone, { vm.phone(it) }, Modifier.fillMaxWidth(), enabled = !state.busy && !codeStep,
                    label = { Text("Số điện thoại đăng ký") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                if (codeStep) {
                    TextButton(onClick = { vm.step(false); code = "" }, enabled = !state.busy) { Text("Sửa số điện thoại") }
                    OutlinedTextField(code, { code = it.filter(Char::isDigit).take(6) }, Modifier.fillMaxWidth(), label = { Text("Mã xác minh 6 số") }, enabled = !state.busy,
                        singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword))
                    SecretField(password, { password = it.take(64) }, "Mật khẩu mới", !state.busy)
                    SecretField(confirm, { confirm = it.take(64) }, "Nhập lại mật khẩu mới", !state.busy)
                    Text("12–64 ký tự, có chữ và số. Mã dùng một lần, có hiệu lực 10 phút; tối đa 5 lần nhập sai.", style = MaterialTheme.typography.bodySmall)
                }
                state.message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
                Button(onClick = { if (codeStep) vm.reset(code, password, confirm) else vm.request() },
                    enabled = !state.busy && phone.isNotBlank() && (codeStep || now >= resendAt), modifier = Modifier.fillMaxWidth()) {
                    Text(if (state.busy) "Đang xử lý…" else if (codeStep) "Đổi mật khẩu" else "Gửi mã qua email")
                }
                if (codeStep) TextButton(onClick = vm::request, enabled = !state.busy && now >= resendAt) {
                    Text(if (now < resendAt) "Gửi lại sau ${(resendAt - now + 999) / 1000} giây" else "Gửi lại mã")
                } else TextButton(onClick = { vm.step(true) }, enabled = !state.busy && phone.isNotBlank()) { Text("Tôi đã có mã xác minh") }
                Text("Chưa liên kết email hoặc không còn truy cập được email? Hãy liên hệ quản trị viên để được hỗ trợ xác minh tài khoản.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
