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
import com.mob10.deliveryapp.ui.components.SectionTitle
import com.mob10.deliveryapp.ui.theme.*

@Composable
fun DriverProfileTab(
    currentUser: UserEntity?,
    driverStatus: DriverWorkingStatus = DriverWorkingStatus.AVAILABLE,
    onStatusChanged: (DriverWorkingStatus) -> Unit = {},
    reliabilityScore: Int = 100,
    completedCount: Int = 0,
    rejectedCount: Int = 0,
    onLogout: () -> Unit,
    onUpdateProfile: (String, String, String, String) -> Unit = { _, _, _, _ -> }
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(text = "Đăng xuất", fontWeight = FontWeight.Bold) },
            text = { Text(text = "Bạn có chắc muốn đăng xuất khỏi tài khoản?") },
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
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))
        
        SectionTitle(title = "Thông tin cá nhân")
        ProfileInfoCard(currentUser = currentUser, onEditClick = { showEditDialog = true })
        
        SectionTitle(title = "Điểm tin cậy & Hiệu suất")
        ReliabilityScoreCard(
            reliabilityScore = reliabilityScore,
            completedCount = completedCount,
            rejectedCount = rejectedCount
        )

        SectionTitle(title = "Trạng thái làm việc")
        DriverShiftSelectorCard(
            currentStatus = driverStatus,
            onStatusChanged = onStatusChanged
        )
        
        SectionTitle(title = "Cài đặt & Tài khoản")
        ProfileMenuCard()
        
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = { showLogoutDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = UthError.copy(alpha = 0.1f),
                contentColor = UthError
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = "Đăng xuất", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ReliabilityScoreCard(
    reliabilityScore: Int,
    completedCount: Int,
    rejectedCount: Int
) {
    val scoreColor = when {
        reliabilityScore >= 80 -> UthSuccess
        reliabilityScore >= 60 -> UthWarning
        else -> UthError
    }

    val ratingText = when {
        reliabilityScore >= 80 -> "Tuyệt vời (Ưu tiên nhận đơn)"
        reliabilityScore >= 60 -> "Khá (Cần chú ý từ chối đơn)"
        else -> "Cảnh báo (Có thể bị hạn chế nhận đơn)"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Điểm tin cậy (Reliability)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = UthOnSurface
                    )
                    Text(
                        text = ratingText,
                        style = MaterialTheme.typography.bodySmall,
                        color = scoreColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = scoreColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "$reliabilityScore/100",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = scoreColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { reliabilityScore / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = scoreColor,
                trackColor = UthOutlineVariant
            )

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = UthOutlineVariant)
            Spacer(modifier = Modifier.height(14.dp))

            // Stats Breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$completedCount",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = UthSuccess
                    )
                    Text(
                        text = "Đã hoàn thành",
                        style = MaterialTheme.typography.labelSmall,
                        color = UthOnSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$rejectedCount",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (rejectedCount > 3) UthError else UthOnSurface
                    )
                    Text(
                        text = "Đã từ chối",
                        style = MaterialTheme.typography.labelSmall,
                        color = UthOnSurfaceVariant
                    )
                }

                val totalHandled = completedCount + rejectedCount
                val rate = if (totalHandled > 0) (completedCount * 100 / totalHandled) else 100
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$rate%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = UthPrimary
                    )
                    Text(
                        text = "Tỷ lệ hoàn thành",
                        style = MaterialTheme.typography.labelSmall,
                        color = UthOnSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun DriverShiftSelectorCard(
    currentStatus: DriverWorkingStatus,
    onStatusChanged: (DriverWorkingStatus) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Chọn chế độ làm việc hiện tại:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = UthOnSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Sẵn sàng
                val isAvailable = currentStatus == DriverWorkingStatus.AVAILABLE
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onStatusChanged(DriverWorkingStatus.AVAILABLE) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isAvailable) UthSuccessContainer else UthBackground,
                    border = BorderStroke(1.dp, if (isAvailable) UthSuccess else UthOutlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Sẵn sàng",
                            fontSize = 14.sp,
                            fontWeight = if (isAvailable) FontWeight.Bold else FontWeight.Medium,
                            color = if (isAvailable) UthSuccess else UthOnSurfaceVariant
                        )
                    }
                }

                // Đang bận
                val isBusy = currentStatus == DriverWorkingStatus.BUSY
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onStatusChanged(DriverWorkingStatus.BUSY) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isBusy) UthWarningContainer else UthBackground,
                    border = BorderStroke(1.dp, if (isBusy) UthWarning else UthOutlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Đang bận",
                            fontSize = 14.sp,
                            fontWeight = if (isBusy) FontWeight.Bold else FontWeight.Medium,
                            color = if (isBusy) UthWarning else UthOnSurfaceVariant
                        )
                    }
                }

                // Ngoại tuyến
                val isOffline = currentStatus == DriverWorkingStatus.OFFLINE
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onStatusChanged(DriverWorkingStatus.OFFLINE) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isOffline) UthSurfaceContainerHighest else UthBackground,
                    border = BorderStroke(1.dp, if (isOffline) UthOnSurfaceVariant else UthOutlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Nghỉ",
                            fontSize = 14.sp,
                            fontWeight = if (isOffline) FontWeight.Bold else FontWeight.Medium,
                            color = if (isOffline) UthOnSurface else UthOnSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoCard(currentUser: UserEntity?, onEditClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
                IconButton(onClick = onEditClick, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Sửa hồ sơ", tint = UthPrimary, modifier = Modifier.size(22.dp))
                }
            }
            
            // Avatar
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(UthPrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = UthPrimary,
                    modifier = Modifier.size(46.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = currentUser?.fullName ?: "Tài xế",
                color = UthOnSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Nhân viên giao hàng",
                color = UthOnSurfaceVariant,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
            
            // Detail rows
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(16.dp))
            ProfileDetailRow(label = "Họ và tên", value = currentUser?.fullName ?: "")
            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(14.dp))
            ProfileDetailRow(label = "Số điện thoại", value = currentUser?.phoneNumber ?: "")
            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(14.dp))
            ProfileDetailRow(label = "Tên đăng nhập", value = currentUser?.username ?: "")
            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(14.dp))
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
            fontSize = 14.sp
        )
        Text(
            text = value,
            color = UthOnSurface,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ProfileMenuCard() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val notImplementedMsg = "Chức năng đang được phát triển"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
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
                title = "Trợ giúp & Hướng dẫn",
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
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(UthPrimaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = UthPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            color = UthOnSurface,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = UthOnSurfaceVariant,
            modifier = Modifier.size(22.dp)
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
