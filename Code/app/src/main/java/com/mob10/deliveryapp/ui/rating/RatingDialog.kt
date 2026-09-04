package com.mob10.deliveryapp.ui.rating

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Dialog đánh giá tài xế — chỉ được gọi hiển thị khi đơn ở trạng thái DA_GIAO.
 * Nơi gọi (Screen) chịu trách nhiệm check: if (order.status == DeliveryStatus.DA_GIAO) { ... }
 */
@Composable
fun RatingDialog(
    deliveryRequestId: Int,
    clientId: Int,
    driverId: Int,
    onDismiss: () -> Unit,
    onSubmit: (stars: Int, comment: String) -> Unit,
    isSubmitting: Boolean = false,
    errorMessage: String? = null
) {
    var selectedStars by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text("Đánh giá chuyến giao hàng") },
        text = {
            Column {
                Text("Bạn hài lòng với tài xế chứ?", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    for (i in 1..5) {
                        IconButton(onClick = { selectedStars = i }) {
                            Icon(
                                imageVector = if (i <= selectedStars) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Chọn $i sao",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Nhận xét (không bắt buộc)") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSubmitting
                )
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                if (isSubmitting) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(" Đang gửi...", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSubmit(selectedStars, comment) },
                enabled = selectedStars > 0 && !isSubmitting
            ) { Text("Gửi đánh giá") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) { Text("Để sau") }
        }
    )
}