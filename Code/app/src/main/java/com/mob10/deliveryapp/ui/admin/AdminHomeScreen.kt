package com.mob10.deliveryapp.ui.admin

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.mob10.deliveryapp.data.model.Order
import com.mob10.deliveryapp.ui.components.*
import com.mob10.deliveryapp.label
import com.mob10.deliveryapp.formatServerTimestamp
import java.util.Locale

@Composable
fun AdminHomeScreen(adminName: String, viewModel: AdminViewModel, onLogout: () -> Unit) {
    var tab by rememberSaveable { mutableStateOf(0) }
    var profileDialogVisible by rememberSaveable { mutableStateOf(false) }
    var selected by remember { mutableStateOf<Order?>(null) }
    var orderQuery by rememberSaveable { mutableStateOf("") }
    var orderStatus by rememberSaveable { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val users by viewModel.users.collectAsStateWithLifecycle()
    val drivers by viewModel.drivers.collectAsStateWithLifecycle()
    val alerts by viewModel.driverAlerts.collectAsStateWithLifecycle()
    val loading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.errorMessage.collectAsStateWithLifecycle()
    val filteredOrders = remember(orders, orderQuery, orderStatus) {
        orders.filter { order ->
            (orderStatus == null || order.status.name == orderStatus) && order.matchesAdminSearch(orderQuery)
        }
    }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(lifecycle) {
        lifecycle.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) { viewModel.loadDashboardData() }
    }
    androidx.activity.compose.BackHandler(tab != 0) { tab = 0 }
    DashboardScaffold(selectedTab = tab, onTabSelected = { tab = it }, navItems = listOf(
        DashboardNavItem("Tổng quan", Icons.Default.Home), DashboardNavItem("Đơn hàng", Icons.AutoMirrored.Filled.ListAlt),
        DashboardNavItem("Người dùng", Icons.Default.Person), DashboardNavItem("Tài xế", Icons.Default.TwoWheeler),
        DashboardNavItem("Cảnh báo", Icons.Default.Warning)
    ), header = { GoDropHeader(roleLabel = "Trung tâm quản trị", name = adminName,
        subtitle = "Quản lý hoạt động toàn hệ thống",
        onProfileClick = { profileDialogVisible = true }, onLogout = onLogout) }) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = viewModel::loadDashboardData, enabled = !loading) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Làm mới")
            }
        }
        if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        error?.let { Text(it, color = MaterialTheme.colorScheme.error); TextButton(onClick = viewModel::loadDashboardData) { Text("Thử lại") } }
        when (tab) {
            0 -> {
                DashboardHeroCard("Toàn hệ thống", "${orders.size} đơn hàng", "${users.size} tài khoản • ${drivers.size} tài xế", Icons.Default.Inventory)
                QuickActionCard("Đơn hàng", "Xem trạng thái, khách hàng và tài xế", Icons.AutoMirrored.Filled.ListAlt) { tab = 1 }
                QuickActionCard("Người dùng", "Danh sách khách hàng, tài xế và quản trị viên", Icons.Default.Person) { tab = 2 }
                QuickActionCard("Tài xế", "Trạng thái làm việc và điểm tin cậy", Icons.Default.TwoWheeler) { tab = 3 }
                QuickActionCard("${alerts.size} cảnh báo", "Tài xế cần kiểm tra", Icons.Default.Warning) { tab = 4 }
            }
            1 -> {
                SectionTitle("Tìm kiếm và lọc đơn")
                OutlinedTextField(
                    value = orderQuery,
                    onValueChange = { orderQuery = it.take(100) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Mã đơn, khách hàng hoặc tài xế") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (orderQuery.isNotEmpty()) IconButton(onClick = { orderQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Xóa tìm kiếm")
                        }
                    },
                    singleLine = true
                )
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(selected = orderStatus == null, onClick = { orderStatus = null }, label = { Text("Tất cả") })
                    com.mob10.deliveryapp.data.model.DeliveryStatus.entries.forEach { status ->
                        FilterChip(
                            selected = orderStatus == status.name,
                            onClick = { orderStatus = status.name },
                            label = { Text(status.label()) }
                        )
                    }
                }
                Text("Hiển thị ${filteredOrders.size}/${orders.size} đơn", style = MaterialTheme.typography.labelLarge)
                if (!loading && orders.isEmpty()) Text("Chưa có đơn hàng.")
                else if (!loading && filteredOrders.isEmpty()) Text("Không tìm thấy đơn phù hợp. Hãy đổi từ khóa hoặc trạng thái.")
                filteredOrders.forEach { order -> QuickActionCard("#${order.id} • ${order.status.label()}",
                    "${order.client?.fullName.orEmpty()} → ${order.deliveryPerson?.fullName ?: "Chưa có tài xế"}", Icons.Default.Inventory) { selected = order } }
            }
            2 -> {
                SectionTitle("Người dùng (${users.size})")
                if (!loading && users.isEmpty()) Text("Chưa có người dùng.")
                users.forEach { user -> AdminInfoCard(user.fullName ?: user.username,
                    "${user.username} • ${roleLabel(user.role)}\n${user.phoneNumber.orEmpty()}\n${if (user.active) "Đang hoạt động" else "Ngừng hoạt động"}") }
            }
            else -> {
                val list = if (tab == 4) alerts else drivers
                SectionTitle(if (tab == 4) "Cảnh báo tài xế" else "Tài xế (${list.size})")
                if (!loading && list.isEmpty()) Text(if (tab == 4) "Không có cảnh báo." else "Chưa có tài xế.")
                list.forEach { driver -> AdminInfoCard(driver.user.fullName ?: driver.user.username,
                    "${availabilityLabel(driver.user.availability)} • ${driver.user.licensePlate.orEmpty()}\n" +
                    "Điểm tin cậy: ${driver.statistics?.reliabilityScore ?: 100.0}/100\n" +
                    "Đã nhận: ${driver.statistics?.totalAccepted ?: 0} • Từ chối: ${driver.statistics?.totalRejected ?: 0}" +
                    if (driver.statistics?.isLocked == true) "\nĐang bị giới hạn nhận đơn đến ${formatServerTimestamp(driver.statistics?.lockedUntil)}" else "") }
            }
        }
    }
    selected?.let { order -> AlertDialog(onDismissRequest = { selected = null }, title = { Text("Đơn #${order.id}") },
        text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(order.status.label()); Text("Lấy: ${order.pickupAddress}\nGiao: ${order.deliveryAddress}")
            Text("Người gửi: ${order.senderName} • ${order.senderPhone}\nNgười nhận: ${order.recipientName} • ${order.recipientPhone}")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { openDialer(context, order.senderPhone) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Phone, contentDescription = null); Spacer(Modifier.width(4.dp)); Text("Gọi gửi")
                }
                OutlinedButton(onClick = { openDialer(context, order.recipientPhone) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Phone, contentDescription = null); Spacer(Modifier.width(4.dp)); Text("Gọi nhận")
                }
            }
            order.packages.forEach { Text("${it.name} • ${it.weightKg} kg × ${it.quantity}") }
            Text("Phí: ${String.format(Locale.forLanguageTag("vi-VN"), "%,.0f", order.totalCost)}đ\nCập nhật: ${formatServerTimestamp(order.updatedAt)}")
        } }, confirmButton = { TextButton(onClick = { selected = null }) { Text("Đóng") } }) }
    if (profileDialogVisible) {
        AlertDialog(
            onDismissRequest = { profileDialogVisible = false },
            icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null) },
            title = { Text("Tài khoản quản trị") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(adminName, style = MaterialTheme.typography.titleMedium)
                    Text("Vai trò: Quản trị viên", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Có quyền xem tổng quan, đơn hàng, người dùng, tài xế và cảnh báo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = { TextButton(onClick = { profileDialogVisible = false }) { Text("Đóng") } }
        )
    }
}

private fun Order.matchesAdminSearch(rawQuery: String): Boolean {
    val query = rawQuery.trim().normalizeForSearch()
    if (query.isEmpty()) return true
    val searchable = buildList {
        add(id.toString()); add("gd-$id"); add("#gd-$id"); add("#$id")
        add(pickupAddress); add(deliveryAddress); add(senderName); add(senderPhone)
        add(recipientName); add(recipientPhone)
        client?.let { add(it.fullName); add(it.phoneNumber.orEmpty()) }
        deliveryPerson?.let { add(it.fullName); add(it.phoneNumber.orEmpty()); add(it.licensePlate.orEmpty()) }
        packages.forEach { add(it.name); add(it.packageType.orEmpty()) }
    }.joinToString(" ").normalizeForSearch()
    return searchable.contains(query)
}

private fun String.normalizeForSearch(): String = java.text.Normalizer.normalize(lowercase(), java.text.Normalizer.Form.NFD)
    .replace(Regex("\\p{M}+"), "").replace('đ', 'd')

private fun openDialer(context: Context, phone: String) {
    val cleanPhone = phone.filter { it.isDigit() || it == '+' }
    if (cleanPhone.isBlank()) return
    val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", cleanPhone, null))
    if (intent.resolveActivity(context.packageManager) != null) context.startActivity(intent)
    else android.widget.Toast.makeText(context, "Thiết bị không có ứng dụng gọi điện", android.widget.Toast.LENGTH_LONG).show()
}

private fun roleLabel(role: String): String = when (role.uppercase()) {
    "CLIENT" -> "Khách hàng"
    "DELIVERY" -> "Tài xế"
    "ADMIN" -> "Quản trị viên"
    else -> role
}

private fun availabilityLabel(availability: String?): String = when (availability?.uppercase()) {
    "AVAILABLE" -> "Sẵn sàng"
    "BUSY" -> "Đang bận"
    "OFFLINE" -> "Tạm nghỉ"
    else -> "Chưa cập nhật"
}

@Composable
private fun AdminInfoCard(title: String, detail: String) {
    Card(
        Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    } }
}
