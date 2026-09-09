package com.mob10.deliveryapp.ui.admin

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.mob10.deliveryapp.data.model.AdminUser
import com.mob10.deliveryapp.data.model.Order
import com.mob10.deliveryapp.ui.components.*
import com.mob10.deliveryapp.ui.theme.UthPrimary
import com.mob10.deliveryapp.ui.theme.UthPrimaryContainer
import com.mob10.deliveryapp.label
import com.mob10.deliveryapp.formatServerTimestamp
import java.util.Locale

/**
 * Enum biểu diễn các vai trò có thể lọc trong tab Người dùng.
 */
private enum class UserRoleFilter(val label: String, val apiValue: String?) {
    ALL("Tất cả", null),
    CLIENT("Khách hàng", "CLIENT"),
    ADMIN("Quản trị viên", "ADMIN")
}

@Composable
fun AdminHomeScreen(adminName: String, viewModel: AdminViewModel, onLogout: () -> Unit) {
    var tab by rememberSaveable { mutableStateOf(0) }
    var profileDialogVisible by rememberSaveable { mutableStateOf(false) }
    var selected by remember { mutableStateOf<Order?>(null) }
    var orderQuery by rememberSaveable { mutableStateOf("") }
    var orderStatus by rememberSaveable { mutableStateOf<String?>(null) }
    // -- Người dùng: bộ lọc vai trò + tìm kiếm --
    var userRoleFilter by rememberSaveable { mutableStateOf(UserRoleFilter.ALL) }
    var userQuery by rememberSaveable { mutableStateOf("") }
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
    // -- Lọc người dùng theo vai trò và tìm kiếm --
    val filteredUsers = remember(users, userRoleFilter, userQuery) {
        users.filter { user ->
            // Loại bỏ tài xế (DELIVERY) khỏi tab Người dùng — tài xế có tab riêng
            user.role.uppercase() != "DELIVERY" &&
            (userRoleFilter.apiValue == null || user.role.uppercase() == userRoleFilter.apiValue) &&
            user.matchesUserSearch(userQuery)
        }
    }
    val clientCount = remember(users) { users.count { it.role.uppercase() == "CLIENT" } }
    val adminCount = remember(users) { users.count { it.role.uppercase() == "ADMIN" } }
    val nonDriverCount = remember(users) { users.count { it.role.uppercase() != "DELIVERY" } }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(lifecycle) {
        lifecycle.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) { viewModel.loadDashboardData() }
    }
    androidx.activity.compose.BackHandler(tab != 0) { tab = 0 }
    DashboardScaffold(selectedTab = tab, onTabSelected = { tab = it }, navItems = listOf(
        DashboardNavItem("Tổng quan", Icons.Default.Home), DashboardNavItem("Đơn hàng", Icons.AutoMirrored.Filled.ListAlt),
        DashboardNavItem("Người dùng", Icons.Default.Person), DashboardNavItem("Tài xế", Icons.Default.TwoWheeler),
        DashboardNavItem("Cảnh báo", Icons.Default.Warning), DashboardNavItem("Yêu cầu", Icons.Default.HowToReg)
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
                QuickActionCard("Người dùng", "$clientCount khách hàng • $adminCount quản trị viên", Icons.Default.Person) { tab = 2 }
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
                // ── Tiêu đề và tóm tắt số lượng theo vai trò ──
                SectionTitle("Người dùng ($nonDriverCount)")

                // ── Thanh tìm kiếm ──
                OutlinedTextField(
                    value = userQuery,
                    onValueChange = { userQuery = it.take(100) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Tên, tài khoản hoặc số điện thoại") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (userQuery.isNotEmpty()) IconButton(onClick = { userQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Xóa tìm kiếm")
                        }
                    },
                    singleLine = true
                )

                // ── FilterChips phân loại vai trò ──
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UserRoleFilter.entries.forEach { filter ->
                        val count = when (filter) {
                            UserRoleFilter.ALL -> nonDriverCount
                            UserRoleFilter.CLIENT -> clientCount
                            UserRoleFilter.ADMIN -> adminCount
                        }
                        FilterChip(
                            selected = userRoleFilter == filter,
                            onClick = { userRoleFilter = filter },
                            label = { Text("${filter.label} ($count)") },
                            leadingIcon = if (userRoleFilter == filter) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }

                // ── Kết quả lọc ──
                Text(
                    "Hiển thị ${filteredUsers.size}/$nonDriverCount người dùng",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!loading && nonDriverCount == 0) {
                    Text("Chưa có người dùng.")
                } else if (!loading && filteredUsers.isEmpty()) {
                    Text("Không tìm thấy người dùng phù hợp. Hãy đổi từ khóa hoặc bộ lọc.")
                }

                filteredUsers.forEach { user ->
                    UserInfoCard(user)
                }
            }
            5 -> {
                val requests by viewModel.driverRequests.collectAsStateWithLifecycle()
                SectionTitle("Yêu cầu đăng ký tài xế (${requests.size})")
                if (!loading && requests.isEmpty()) Text("Không có yêu cầu nào.")
                requests.forEach { req ->
                    Card(Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Người dùng: ${req.user?.fullName ?: req.user?.username}", style = MaterialTheme.typography.titleMedium)
                            Text("Biển số xe đăng ký: ${req.licensePlate}", style = MaterialTheme.typography.bodyMedium)
                            Text("Ngày yêu cầu: ${formatServerTimestamp(req.createdAt)}", style = MaterialTheme.typography.bodySmall)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { viewModel.approveDriverRequest(req.id) }) {
                                    Text("Phê duyệt")
                                }
                                OutlinedButton(onClick = { viewModel.rejectDriverRequest(req.id) }) {
                                    Text("Từ chối")
                                }
                            }
                        }
                    }
                }
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

// ── Tìm kiếm người dùng ──────────────────────────────────────────────
private fun AdminUser.matchesUserSearch(rawQuery: String): Boolean {
    val query = rawQuery.trim().normalizeForSearch()
    if (query.isEmpty()) return true
    val searchable = buildList {
        add(username)
        fullName?.let { add(it) }
        phoneNumber?.let { add(it) }
    }.joinToString(" ").normalizeForSearch()
    return searchable.contains(query)
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
    try {
        context.startActivity(intent)
    } catch (e: android.content.ActivityNotFoundException) {
        android.widget.Toast.makeText(context, "Thiết bị không có ứng dụng gọi điện", android.widget.Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "Không thể mở ứng dụng gọi điện", android.widget.Toast.LENGTH_LONG).show()
    }
}

private fun roleLabel(role: String): String = when (role.uppercase()) {
    "CLIENT" -> "Khách hàng"
    "DELIVERY" -> "Tài xế"
    "ADMIN" -> "Quản trị viên"
    else -> role
}

private fun roleIcon(role: String): @Composable () -> Unit = {
    when (role.uppercase()) {
        "ADMIN" -> Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(14.dp))
        "CLIENT" -> Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp))
        else -> Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp))
    }
}

private fun availabilityLabel(availability: String?): String = when (availability?.uppercase()) {
    "AVAILABLE" -> "Sẵn sàng"
    "BUSY" -> "Đang bận"
    "OFFLINE" -> "Tạm nghỉ"
    else -> "Chưa cập nhật"
}

// ── UserInfoCard — thẻ người dùng với badge vai trò và trạng thái ──
@Composable
private fun UserInfoCard(user: AdminUser) {
    val isAdmin = user.role.uppercase() == "ADMIN"
    val roleBadgeColor = if (isAdmin) Color(0xFFE65100) else UthPrimary
    val roleBadgeBg = if (isAdmin) Color(0xFFFFF3E0) else UthPrimaryContainer

    Card(
        Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Avatar placeholder
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isAdmin) Color(0xFFFFF3E0) else UthPrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isAdmin) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                    contentDescription = null,
                    tint = if (isAdmin) Color(0xFFE65100) else UthPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Tên + badge vai trò
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        user.fullName ?: user.username,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(Modifier.width(8.dp))
                    // Role badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = roleBadgeBg
                    ) {
                        Text(
                            text = roleLabel(user.role),
                            color = roleBadgeColor,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                // Username và phone
                Text(
                    text = buildString {
                        append(user.username)
                        if (!user.phoneNumber.isNullOrBlank()) append(" • ${user.phoneNumber}")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Trạng thái hoạt động
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (user.active) Color(0xFF4CAF50) else Color(0xFFBDBDBD))
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (user.active) "Đang hoạt động" else "Ngừng hoạt động",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = if (user.active) Color(0xFF388E3C) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ── AdminInfoCard — dùng cho tab Tài xế và Cảnh báo ──
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
