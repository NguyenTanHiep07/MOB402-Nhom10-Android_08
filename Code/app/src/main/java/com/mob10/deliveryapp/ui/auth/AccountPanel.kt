package com.mob10.deliveryapp.ui.auth

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mob10.deliveryapp.data.remote.api.AccountProfile

val LocalAccountUpdated = staticCompositionLocalOf<(AccountProfile) -> Unit> { {} }

@Composable
fun AccountPanel(userId: Int, vm: AccountViewModel = viewModel(key = "account_$userId")) {
    val state by vm.state.collectAsStateWithLifecycle()
    val updated = LocalAccountUpdated.current
    val context = LocalContext.current.applicationContext
    LaunchedEffect(vm) { vm.load() }
    LaunchedEffect(state.profile) { state.profile?.let(updated) }
    var editing by rememberSaveable { mutableStateOf(false) }
    var linking by rememberSaveable { mutableStateOf(false) }
    var currentPassword by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    LaunchedEffect(state.message) {
        if (state.message?.startsWith("Đã lưu") == true) { editing = false; currentPassword = "" }
        if (state.message?.startsWith("Đã xác minh") == true) { linking = false; currentPassword = ""; code = "" }
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { vm.selectImage(context, it) }
    }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        state.message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        val p = state.profile
        if (p == null) {
            if (!state.busy) OutlinedButton(onClick = vm::load) { Text("Tải lại hồ sơ") }
        } else {
            var username by rememberSaveable(p.username) { mutableStateOf(p.username) }
            var name by rememberSaveable(p.fullName) { mutableStateOf(p.fullName) }
            var phone by rememberSaveable(p.phoneNumber) { mutableStateOf(p.phoneNumber) }
            var email by rememberSaveable(p.email) { mutableStateOf(p.email.orEmpty()) }
            val encoded = if (state.avatarChanged) state.avatarDraft else p.avatarBase64
            val bitmap = remember(encoded) { runCatching {
                encoded?.let { Base64.decode(it, Base64.DEFAULT) }?.let { BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap() }
            }.getOrNull() }
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        if (bitmap != null) Image(bitmap, "Ảnh đại diện", Modifier.size(72.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                        else Icon(Icons.Default.Person, "Chưa có ảnh đại diện", Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                        Column(Modifier.weight(1f)) {
                            Text(p.fullName, style = MaterialTheme.typography.titleLarge)
                            Text(if (p.role == "DELIVERY") "Tài xế GoDrop" else "Khách hàng GoDrop", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    if (editing) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, enabled = !state.busy) { Text("Chọn ảnh") }
                            TextButton(onClick = vm::removeAvatar, enabled = !state.busy) { Text("Xóa ảnh") }
                        }
                        OutlinedTextField(name, { name = it.take(120) }, Modifier.fillMaxWidth(), label = { Text("Họ và tên") }, singleLine = true, enabled = !state.busy)
                        OutlinedTextField(username, { username = it.take(80) }, Modifier.fillMaxWidth(), label = { Text("Tên đăng nhập") }, singleLine = true, enabled = !state.busy)
                        OutlinedTextField(phone, { phone = it.take(20) }, Modifier.fillMaxWidth(), label = { Text("Số điện thoại") }, singleLine = true,
                            enabled = !state.busy, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                        Text("Sau khi đổi số điện thoại, dùng số mới để khôi phục mật khẩu. Email bảo mật vẫn được giữ nguyên.", style = MaterialTheme.typography.bodySmall)
                        SecretField(currentPassword, { currentPassword = it.take(72) }, "Mật khẩu hiện tại để xác nhận", !state.busy)
                        Button(onClick = { vm.save(username, name, phone, currentPassword) }, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) { Text("Lưu thay đổi") }
                        TextButton(onClick = { editing = false; vm.discardAvatar(); currentPassword = ""; username = p.username; name = p.fullName; phone = p.phoneNumber }, enabled = !state.busy) { Text("Hủy chỉnh sửa") }
                    } else {
                        Text("Tên đăng nhập: ${p.username}")
                        Text("Số điện thoại: ${p.phoneNumber}")
                        p.licensePlate?.let { Text("Biển số xe: $it") }
                        OutlinedButton(onClick = { editing = true; linking = false; currentPassword = "" }, enabled = !state.busy) { Text("Chỉnh sửa hồ sơ") }
                    }
                }
            }
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Email bảo mật", style = MaterialTheme.typography.titleMedium)
                    Text(if (p.emailVerified) "${p.email}\nĐã xác minh" else "Chưa liên kết email đã xác minh")
                    Text("Email này nhận mã khi bạn quên mật khẩu. Hãy liên kết khi bạn vẫn đăng nhập được.", style = MaterialTheme.typography.bodySmall)
                    if (!linking) OutlinedButton(onClick = { linking = true; editing = false; vm.discardAvatar(); currentPassword = "" }, enabled = !state.busy) {
                        Text(if (p.emailVerified) "Thay đổi email bảo mật" else "Liên kết email")
                    } else {
                        OutlinedTextField(email, { email = it.take(254) }, Modifier.fillMaxWidth(), label = { Text("Email bạn có thể truy cập") }, singleLine = true,
                            enabled = !state.busy, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
                        SecretField(currentPassword, { currentPassword = it.take(72) }, "Mật khẩu tài khoản GoDrop hiện tại", !state.busy)
                        var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
                        LaunchedEffect(state.resendAt) { while (now < state.resendAt) { kotlinx.coroutines.delay(1000); now = System.currentTimeMillis() } }
                        OutlinedButton(onClick = { vm.link(email, currentPassword); code = "" }, enabled = !state.busy && now >= state.resendAt) {
                            Text(if (now < state.resendAt) "Gửi lại sau ${(state.resendAt-now+999)/1000} giây" else "Gửi mã xác minh email")
                        }
                        Text("Mở hộp thư vừa nhập, lấy mã 6 số rồi xác nhận bên dưới. Email cũ vẫn có hiệu lực cho đến khi xác minh email mới thành công.", style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = vm::emailStatus, enabled = !state.busy) { Text("Chưa nhận được mã? Kiểm tra gửi thư") }
                        OutlinedTextField(code, { code = it.filter(Char::isDigit).take(6) }, Modifier.fillMaxWidth(), label = { Text("Mã xác minh email") }, singleLine = true,
                            enabled = !state.busy, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword))
                        Button(onClick = { vm.verify(code, currentPassword) }, enabled = !state.busy && code.length == 6) { Text("Xác nhận liên kết email") }
                        TextButton(onClick = { linking = false; currentPassword = ""; code = "" }, enabled = !state.busy) { Text("Đóng") }
                    }
                }
            }
            TextButton(onClick = vm::load, enabled = !state.busy && !editing && !linking) { Text("Làm mới hồ sơ") }
        }
    }
}
