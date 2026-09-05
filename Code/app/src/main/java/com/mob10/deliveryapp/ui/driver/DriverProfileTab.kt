package com.mob10.deliveryapp.ui.driver

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
    onLogout: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

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

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))
        
        SectionTitle(title = "Thông tin cá nhân")
        currentUser?.let { com.mob10.deliveryapp.ui.auth.AccountPanel(it.id) }
        
        SectionTitle(title = "Điểm tin cậy và hiệu suất")
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
        
        SectionTitle(title = "Hướng dẫn hoạt động")
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "• Bật \"Sẵn sàng\" để nhận đơn hàng mới tự động từ hệ thống.\n• Sau khi nhận đơn, cập nhật đúng tiến trình: Đến quán → Lấy hàng → Đến khách → Đã giao.\n• Tra cứu các chuyến đã hoàn thành và thu nhập tại mục Lịch sử.",
                    style = MaterialTheme.typography.bodySmall,
                    color = UthOnSurfaceVariant,
                    lineHeight = 22.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        Button(
            onClick = { showLogoutDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = UthError.copy(alpha = 0.08f),
                contentColor = UthError
            ),
            border = BorderStroke(1.dp, UthError.copy(alpha = 0.25f)),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Đăng xuất", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(24.dp))
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Điểm tin cậy",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = UthOnSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = ratingText,
                        style = MaterialTheme.typography.bodySmall,
                        color = scoreColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = scoreColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "$reliabilityScore/100",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = scoreColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { reliabilityScore / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = scoreColor,
                trackColor = UthOutlineVariant.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Stats Breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$completedCount",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = UthSuccess
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Đã hoàn thành",
                        style = MaterialTheme.typography.labelSmall,
                        color = UthOnSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$rejectedCount",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (rejectedCount > 3) UthError else UthOnSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
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
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = UthPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Chọn chế độ làm việc hiện tại:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = UthOnSurface
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Sẵn sàng
                val isAvailable = currentStatus == DriverWorkingStatus.AVAILABLE
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onStatusChanged(DriverWorkingStatus.AVAILABLE) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isAvailable) UthSuccessContainer else UthBackground,
                    border = BorderStroke(if (isAvailable) 1.5.dp else 1.dp, if (isAvailable) UthSuccess else UthOutlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 11.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Sẵn sàng",
                            fontSize = 13.5.sp,
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
                    border = BorderStroke(if (isBusy) 1.5.dp else 1.dp, if (isBusy) UthWarning else UthOutlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 11.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Đang bận",
                            fontSize = 13.5.sp,
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
                    border = BorderStroke(if (isOffline) 1.5.dp else 1.dp, if (isOffline) UthOnSurfaceVariant else UthOutlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 11.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Nghỉ",
                            fontSize = 13.5.sp,
                            fontWeight = if (isOffline) FontWeight.Bold else FontWeight.Medium,
                            color = if (isOffline) UthOnSurface else UthOnSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
