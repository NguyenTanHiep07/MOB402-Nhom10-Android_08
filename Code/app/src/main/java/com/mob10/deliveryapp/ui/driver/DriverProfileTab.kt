package com.mob10.deliveryapp.ui.driver

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mob10.deliveryapp.data.local.entity.UserEntity
import com.mob10.deliveryapp.ui.theme.*

@Composable
fun DriverProfileTab(
    currentUser: UserEntity?,
    isAvailable: Boolean,
    onAvailabilityChanged: (Boolean) -> Unit,
    onLogout: () -> Unit,
    onUpdateProfile: (String, String, String, String) -> Unit = { _, _, _, _ -> }
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(text = "Đăng xuất", fontWeight = FontWeight.Bold) },
            text = { Text(text = "Bạn có chắc muốn đăng xuất?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) {
                    Text("Đăng xuất", color = UthError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Hủy", color = UthOnSurfaceVariant)
                }
            },
            containerColor = Color.White
        )
    }

    if (showEditDialog && currentUser != null) {
        EditProfileDialog(
            currentUser = currentUser,
            onDismiss = { showEditDialog = false },
            onSave = { fullName, phone, username, license ->
                onUpdateProfile(fullName, phone, username, license)
                showEditDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Thông tin cá nhân", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = UthOnSurface)
        Spacer(modifier = Modifier.height(12.dp))
        ProfileInfoCard(currentUser = currentUser, onEditClick = { showEditDialog = true })
        
        Text(text = "Trạng thái làm việc", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = UthOnSurface)
        Spacer(modifier = Modifier.height(12.dp))
        DriverAvailabilityCard(isAvailable = isAvailable, onAvailabilityChanged = onAvailabilityChanged)
        
        Text(text = "Tài khoản", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = UthOnSurface)
        Spacer(modifier = Modifier.height(12.dp))
        ProfileMenuCard()
        
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { showLogoutDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(53.dp),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = UthSecondaryContainer,
                contentColor = UthError
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Đăng xuất", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ProfileInfoCard(currentUser: UserEntity?, onEditClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
                IconButton(onClick = onEditClick, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Sửa hồ sơ", tint = UthPrimary, modifier = Modifier.size(20.dp))
                }
            }
            
            // Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(UthPrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = UthPrimary,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = currentUser?.fullName ?: "Tài xế",
                color = UthOnSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Nhân viên giao hàng",
                color = UthOnSurfaceVariant,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Detail rows
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(16.dp))
            ProfileDetailRow(label = "Họ và tên", value = currentUser?.fullName ?: "")
            Spacer(modifier = Modifier.height(16.dp))
            ProfileDetailRow(label = "Số điện thoại", value = currentUser?.phoneNumber ?: "")
            Spacer(modifier = Modifier.height(16.dp))
            ProfileDetailRow(label = "Tên đăng nhập", value = currentUser?.username ?: "")
            Spacer(modifier = Modifier.height(16.dp))
            ProfileDetailRow(label = "Biển số xe", value = currentUser?.licensePlate ?: "Chưa cập nhật")
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ProfileDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = UthOnSurfaceVariant,
            fontSize = 13.sp
        )
        Text(
            text = value,
            color = UthOnSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ProfileMenuCard() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val notImplementedMsg = "Chức năng đang được phát triển"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            ProfileMenuItem(
                icon = Icons.Default.Lock, 
                title = "Đổi mật khẩu",
                onClick = { android.widget.Toast.makeText(context, notImplementedMsg, android.widget.Toast.LENGTH_SHORT).show() }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
            ProfileMenuItem(
                icon = Icons.Default.Notifications, 
                title = "Cài đặt thông báo",
                onClick = { android.widget.Toast.makeText(context, notImplementedMsg, android.widget.Toast.LENGTH_SHORT).show() }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
            ProfileMenuItem(
                icon = Icons.Default.HelpOutline, 
                title = "Trợ giúp",
                onClick = { android.widget.Toast.makeText(context, notImplementedMsg, android.widget.Toast.LENGTH_SHORT).show() }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
            ProfileMenuItem(
                icon = Icons.Default.Info, 
                title = "Về ứng dụng",
                onClick = { android.widget.Toast.makeText(context, notImplementedMsg, android.widget.Toast.LENGTH_SHORT).show() }
            )
        }
    }
}

@Composable
private fun ProfileMenuItem(icon: ImageVector, title: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(UthPrimaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = UthPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            color = UthOnSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = UthOnSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun EditProfileDialog(
    currentUser: UserEntity,
    onDismiss: () -> Unit,
    onSave: (fullName: String, phone: String, username: String, license: String) -> Unit
) {
    var fullName by remember { mutableStateOf(currentUser.fullName) }
    var phone by remember { mutableStateOf(currentUser.phoneNumber) }
    var username by remember { mutableStateOf(currentUser.username) }
    var license by remember { mutableStateOf(currentUser.licensePlate ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chỉnh sửa thông tin", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Họ và tên") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Số điện thoại") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Tên đăng nhập") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = license,
                    onValueChange = { license = it },
                    label = { Text("Biển số xe") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fullName.isNotBlank() && phone.isNotBlank() && username.isNotBlank()) {
                        onSave(fullName.trim(), phone.trim(), username.trim(), license.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = UthPrimary)
            ) {
                Text("Lưu", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = UthOnSurfaceVariant)
            }
        },
        containerColor = Color.White
    )
}
