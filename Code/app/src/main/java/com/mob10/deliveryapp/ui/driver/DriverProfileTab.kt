package com.mob10.deliveryapp.ui.driver

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mob10.deliveryapp.data.local.entity.UserEntity
import com.mob10.deliveryapp.ui.components.SectionTitle
import com.mob10.deliveryapp.ui.theme.*

@Composable
fun DriverProfileTab(
    currentUser: UserEntity?,
    isAvailable: Boolean,
    onAvailabilityChanged: (Boolean) -> Unit,
    onLogout: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

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

    // 1. Personal Information
    SectionTitle(title = "Thông tin cá nhân")
    ProfileInfoCard(currentUser)
    
    // 2. Working Status
    SectionTitle(title = "Trạng thái làm việc")
    DriverAvailabilityCard(isAvailable = isAvailable, onAvailabilityChanged = onAvailabilityChanged)
    
    // 4. Account Functions
    SectionTitle(title = "Tài khoản")
    ProfileMenuCard()
    
    Spacer(modifier = Modifier.height(16.dp))
    
    // 5. Logout Button
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
        Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "Đăng xuất", fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ProfileInfoCard(currentUser: UserEntity?) {
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
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))
            ProfileDetailRow(label = "Họ và tên", value = currentUser?.fullName ?: "")
            Spacer(modifier = Modifier.height(12.dp))
            ProfileDetailRow(label = "Số điện thoại", value = currentUser?.phoneNumber ?: "")
            Spacer(modifier = Modifier.height(12.dp))
            ProfileDetailRow(label = "Tên đăng nhập", value = currentUser?.username ?: "")
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
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ProfileMenuCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            ProfileMenuItem(icon = Icons.Default.Lock, title = "Đổi mật khẩu")
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))
            ProfileMenuItem(icon = Icons.Default.Notifications, title = "Cài đặt thông báo")
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))
            ProfileMenuItem(icon = Icons.Default.HelpOutline, title = "Trợ giúp")
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))
            ProfileMenuItem(icon = Icons.Default.Info, title = "Về ứng dụng")
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
