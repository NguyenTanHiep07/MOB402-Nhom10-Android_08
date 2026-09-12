package com.mob10.deliveryapp.ui.driver

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mob10.deliveryapp.data.model.RejectionReason
import com.mob10.deliveryapp.ui.theme.UthError
import com.mob10.deliveryapp.ui.theme.UthOnSurface
import com.mob10.deliveryapp.ui.theme.UthOnSurfaceVariant
import com.mob10.deliveryapp.ui.theme.UthOutlineVariant
import com.mob10.deliveryapp.ui.theme.UthPrimary
import com.mob10.deliveryapp.ui.theme.UthSurfaceContainerLow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RejectReasonBottomSheet(
    orderId: Int,
    rejectionReasons: List<RejectionReason> = emptyList(),
    isSubmitting: Boolean = false,
    errorMessage: String? = null,
    onDismiss: () -> Unit,
    onConfirmReject: (orderId: Int, reason: String, note: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val availableReasons = rejectionReasons
    var selectedReasonCode by rememberSaveable(orderId) {
        mutableStateOf(availableReasons.firstOrNull()?.code)
    }
    var customNote by rememberSaveable(orderId) { mutableStateOf("") }
    val selectedReason = availableReasons.firstOrNull { it.code == selectedReasonCode }
    val missingRequiredNote = selectedReason?.requiresNote == true && customNote.isBlank()

    ModalBottomSheet(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = null,
                    tint = UthError,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Từ chối đơn hàng #$orderId",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = UthOnSurface
                    )
                    Text(
                        text = "Đơn sẽ giữ trong hệ thống để tài xế khác nhận",
                        style = MaterialTheme.typography.bodySmall,
                        color = UthOnSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notice Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = UthSurfaceContainerLow,
                border = androidx.compose.foundation.BorderStroke(1.dp, UthOutlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = UthError,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Từ chối nhiều đơn liên tục có thể làm giảm điểm tin cậy của bạn.",
                        style = MaterialTheme.typography.bodySmall,
                        color = UthOnSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Chọn lý do từ chối:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = UthOnSurface
            )

            Spacer(modifier = Modifier.height(8.dp))
            if (availableReasons.isEmpty()) {
                Text("Chưa tải được lý do từ máy chủ. Đóng và bấm Tải lại để thử lại.", color = UthError)
            }
            errorMessage?.let { Text(it, color = UthError) }

            // Radio Options
            availableReasons.forEach { reason ->
                val isSelected = selectedReasonCode == reason.code
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable(enabled = !isSubmitting) { selectedReasonCode = reason.code },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) UthPrimary else UthOutlineVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            enabled = !isSubmitting,
                            onClick = { selectedReasonCode = reason.code },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = UthPrimary,
                                unselectedColor = UthOnSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = reason.label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = UthOnSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Additional note input
            OutlinedTextField(
                value = customNote,
                onValueChange = { customNote = it.take(500) },
                enabled = !isSubmitting,
                label = { Text("Ghi chú thêm (tùy chọn)") },
                placeholder = { Text("Nhập chi tiết lý do (nếu cần)...") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                shape = RoundedCornerShape(12.dp),
                isError = missingRequiredNote,
                supportingText = if (missingRequiredNote) {
                    { Text("Lý do này yêu cầu nhập ghi chú.") }
                } else {
                    null
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSubmitting
                ) {
                    Text("Đóng")
                }

                Button(
                    onClick = {
                        selectedReason?.let { onConfirmReject(orderId, it.code, customNote) }
                    },
                    modifier = Modifier.weight(1.5f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = UthError),
                    enabled = !isSubmitting && selectedReason != null && !missingRequiredNote
                ) {
                    Text("Xác nhận từ chối", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
