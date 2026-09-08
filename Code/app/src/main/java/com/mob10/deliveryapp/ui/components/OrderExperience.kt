package com.mob10.deliveryapp.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mob10.deliveryapp.data.model.*

private val stages = listOf(
    DeliveryStatus.CHO_TIEP_NHAN to "Chờ tài xế",
    DeliveryStatus.DA_CHAP_NHAN to "Đã nhận đơn",
    DeliveryStatus.DA_DEN_NHA_HANG to "Đến điểm lấy",
    DeliveryStatus.DA_LAY_HANG to "Đã lấy hàng",
    DeliveryStatus.DANG_VAN_CHUYEN to "Đang vận chuyển",
    DeliveryStatus.DA_DEN_KHACH_HANG to "Đến điểm giao",
    DeliveryStatus.DA_GIAO to "Giao thành công"
)

private fun timeLabel(value: String?) = runCatching {
    val parser = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", java.util.Locale.US)
    val date = parser.parse(requireNotNull(value).replace(Regex("\\.\\d+(?=Z$)"), ""))
    java.text.SimpleDateFormat("HH:mm · dd/MM", java.util.Locale.getDefault()).format(requireNotNull(date))
}.getOrDefault("Chưa có thời gian xác nhận")

@Composable
fun OrderJourney(order: Order, history: List<StatusHistory>) {
    val primary = MaterialTheme.colorScheme.primary
    val current = stages.indexOfFirst { it.first == order.status }
    val progress by animateFloatAsState(if (current < 0) 0f else current / 6f, label = "journey")
    var avatar by remember(order.id) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    LaunchedEffect(order.id, order.deliveryPerson?.id) {
        if (order.deliveryPerson != null) {
            val result = com.mob10.deliveryapp.data.repository.DeliveryPhotoRepository().avatar(order.id)
            if (result is com.mob10.deliveryapp.data.util.NetworkResult.Success) {
                avatar = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) { runCatching {
                    result.data.image?.let { android.util.Base64.decode(it, android.util.Base64.DEFAULT) }?.let {
                        android.graphics.BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap()
                    }
                }.getOrNull() }
            }
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Surface(color = primary.copy(alpha = .08f), shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(if (order.status == DeliveryStatus.DA_HUY) "Đơn hàng đã hủy" else stages.getOrNull(current)?.second.orEmpty(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (current >= 0) LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape))
                Text("Cập nhật từ trạng thái đơn hàng", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        order.deliveryPerson?.let { driver ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = CircleShape, color = primary.copy(alpha = .12f)) {
                    Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                        if (avatar != null) androidx.compose.foundation.Image(avatar!!, "Ảnh tài xế", Modifier.size(48.dp).clip(CircleShape), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                        else Text(driver.fullName.trim().split(" ").takeLast(2).mapNotNull { it.firstOrNull()?.toString() }.joinToString(""), color = primary, fontWeight = FontWeight.Bold)
                    }
                }
                Column {
                    Text(driver.fullName, style = MaterialTheme.typography.titleSmall)
                    Text(driver.licensePlate ?: "Tài xế phụ trách", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (order.status == DeliveryStatus.DA_HUY) {
            Text("Hành trình đã kết thúc. Xem lịch sử bên dưới để biết thời điểm hủy.", color = MaterialTheme.colorScheme.error)
        } else stages.forEachIndexed { index, (status, label) ->
            val event = history.lastOrNull { it.toStatus == status }
            val reached = event != null || index <= current
            val active = index == current
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = if (reached) primary else MaterialTheme.colorScheme.surfaceVariant) {
                    Box(Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                        if (reached && !active) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        else Text("${index + 1}", color = if (reached) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                    }
                }
                Column(Modifier.weight(1f).padding(vertical = 3.dp)) {
                    Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal, color = if (active) primary else MaterialTheme.colorScheme.onSurface)
                    Text(if (event != null) timeLabel(event.timestamp) else if (index == 0) timeLabel(order.createdAt) else if (reached) "Đã qua bước này" else "Chưa thực hiện", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (active) Text("HIỆN TẠI", style = MaterialTheme.typography.labelSmall, color = primary)
            }
        }
    }
}

@Composable
fun CurrentTripCard(order: Order, onOpen: () -> Unit) {
    val context = LocalContext.current
    val pickup = order.status in listOf(DeliveryStatus.DA_CHAP_NHAN, DeliveryStatus.DA_DEN_NHA_HANG)
    val address = if (pickup) order.pickupAddress else order.deliveryAddress
    val phone = if (pickup) order.senderPhone else order.recipientPhone
    val color = Color(0xFF006B53)
    Surface(shape = RoundedCornerShape(24.dp), shadowElevation = 3.dp) {
        Column(Modifier.background(Brush.linearGradient(listOf(color, Color(0xFF009B79)))).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("CHUYẾN GIAO HIỆN TẠI", color = Color.White.copy(alpha = .8f), style = MaterialTheme.typography.labelMedium)
                Text("#GD-${order.id}", color = Color.White, style = MaterialTheme.typography.labelLarge)
            }
            Text(if (pickup) "Đến điểm lấy hàng" else "Giao đến người nhận", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(address, color = Color.White.copy(alpha = .95f), style = MaterialTheme.typography.bodyMedium)
            HorizontalDivider(color = Color.White.copy(alpha = .2f))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stages.firstOrNull { it.first == order.status }?.second.orEmpty(), color = Color.White, style = MaterialTheme.typography.labelMedium)
                Text(String.format(java.util.Locale.forLanguageTag("vi-VN"), "%,.0f đ", order.totalCost), color = Color.White, fontWeight = FontWeight.Bold)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    val lat = if (pickup) order.pickupLatitude else order.deliveryLatitude
                    val lng = if (pickup) order.pickupLongitude else order.deliveryLongitude
                    val target = if (lat != null && lng != null && lat in -90.0..90.0 && lng in -180.0..180.0) "$lat,$lng" else address
                    val uri = Uri.parse("https://www.google.com/maps/dir/").buildUpon().appendQueryParameter("api", "1").appendQueryParameter("destination", target).appendQueryParameter("dir_action", "navigate").build()
                    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }.onFailure { android.widget.Toast.makeText(context, "Không có ứng dụng mở bản đồ", android.widget.Toast.LENGTH_SHORT).show() }
                }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = color)) { Text("Chỉ đường") }
                Button(onClick = {
                    runCatching { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone.filter { it.isDigit() || it == '+' }, null))) }.onFailure { android.widget.Toast.makeText(context, "Không mở được màn quay số", android.widget.Toast.LENGTH_SHORT).show() }
                }, enabled = phone.isNotBlank(), modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = .15f), contentColor = Color.White)) { Text("Liên hệ") }
            }
            TextButton(onClick = onOpen, modifier = Modifier.fillMaxWidth()) { Text("Mở đơn · Cập nhật tiến trình →", color = Color.White) }
        }
    }
}

@Composable
fun OrderLoadingSkeleton() {
    val transition = rememberInfiniteTransition(label = "loading")
    val alpha by transition.animateFloat(.25f, .65f, infiniteRepeatable(tween(850), RepeatMode.Reverse), label = "pulse")
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Đang tải đơn hàng…", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        repeat(3) {
            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.fillMaxWidth(.45f).height(18.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = alpha)))
                    Box(Modifier.fillMaxWidth().height(12.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)))
                    Box(Modifier.fillMaxWidth(.7f).height(12.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)))
                }
            }
        }
    }
}
