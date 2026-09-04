// LEGACY / UNUSED
package com.mob10.deliveryapp.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mob10.deliveryapp.data.local.entity.DeliveryRequestEntity
import com.mob10.deliveryapp.data.model.DeliveryStatus
import com.mob10.deliveryapp.ui.rating.RatingDialog
import com.mob10.deliveryapp.ui.rating.RatingViewModel
import com.mob10.deliveryapp.ui.rating.RatingViewModelFactory
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerOrderDetailScreen(
    orderId: Int,
    viewModel: CustomerViewModel,
    onBack: () -> Unit
) {
    var order by remember { mutableStateOf<DeliveryRequestEntity?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Tải lại chi tiết đơn mỗi khi màn hình mở, và mỗi khi có thông báo cancel mới
    LaunchedEffect(orderId) {
        isLoading = true
        errorMessage = null
        val result = viewModel.getOrderDetail(orderId)
        order = result
        isLoading = false
        if (result == null) {
            errorMessage = "Không tìm thấy đơn hàng hoặc bạn không có quyền xem đơn này."
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    // Lắng nghe kết quả Cancel
    LaunchedEffect(Unit) {
        viewModel.cancelResult.collect { result ->
            when (result) {
                is CustomerViewModel.CancelUiResult.Success -> {
                    snackbarHostState.showSnackbar("Đã hủy đơn hàng thành công.")
                    // tải lại chi tiết để cập nhật trạng thái mới nhất
                    order = viewModel.getOrderDetail(orderId)
                }
                is CustomerViewModel.CancelUiResult.Error -> {
                    snackbarHostState.showSnackbar(result.message)
                }
            }
        }
    }

    // --- Rating: ViewModel riêng cho luồng đánh giá ---
    val ratingViewModel: RatingViewModel = viewModel(factory = RatingViewModelFactory())
    val ratingUiState by ratingViewModel.uiState.collectAsState()
    var showRatingDialog by remember { mutableStateOf(false) }

    // Khi đơn đã tải xong và đã DA_GIAO, kiểm tra xem đã đánh giá chưa
    LaunchedEffect(order?.id, order?.status) {
        val currentOrder = order
        if (currentOrder != null && currentOrder.status == DeliveryStatus.DA_GIAO) {
            ratingViewModel.checkExistingRating(currentOrder.id)
        }
    }

    // Khi gửi đánh giá thành công, đóng dialog + báo snackbar
    LaunchedEffect(ratingUiState.submitSuccess) {
        if (ratingUiState.submitSuccess) {
            showRatingDialog = false
            snackbarHostState.showSnackbar("Cảm ơn bạn đã đánh giá!")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết đơn hàng", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    // LOADING STATE
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                errorMessage != null -> {
                    // ERROR STATE
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(errorMessage ?: "Có lỗi xảy ra")
                    }
                }
                order != null -> {
                    // CONTENT STATE
                    val currentOrder = order!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    "MÃ ĐƠN #${currentOrder.id}",
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    currentOrder.status.detailLabel(),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    "Theo dõi hành trình và cập nhật mới nhất của đơn hàng",
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.78f)
                                )
                            }
                        }

                        DetailCard(title = "Hành trình") {
                            DetailRow(Icons.Default.LocationOn, "Điểm lấy hàng", currentOrder.pickupAddress)
                            DetailRow(Icons.Default.LocalShipping, "Điểm giao hàng", currentOrder.deliveryAddress)
                        }

                        DetailCard(title = "Người nhận") {
                            Text(currentOrder.recipientName, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Call,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(currentOrder.recipientPhone, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(18.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Tổng phí", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${currentOrder.distanceKm} km", style = MaterialTheme.typography.labelMedium)
                                }
                                Text(
                                    "${NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")).format(currentOrder.totalCost)} đ",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        val canCancel = currentOrder.status == DeliveryStatus.CHO_TIEP_NHAN ||
                                currentOrder.status == DeliveryStatus.DA_CHAP_NHAN

                        if (canCancel) {
                            OutlinedButton(
                                onClick = { viewModel.cancelOrder(currentOrder.id) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("Hủy đơn hàng")
                            }
                        }

                        // --- Nút Đánh giá: chỉ hiện khi đơn đã giao xong và có tài xế ---
                        val driverId = currentOrder.deliveryPersonId
                        if (currentOrder.status == DeliveryStatus.DA_GIAO && driverId != null) {
                            if (ratingUiState.alreadyRated) {
                                Text(
                                    "Bạn đã đánh giá đơn này. Cảm ơn bạn!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            } else {
                                Button(
                                    onClick = { showRatingDialog = true },
                                    enabled = !ratingUiState.isLoading,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Đánh giá tài xế")
                                }
                            }
                        }
                    }
                }
            }

            // --- Dialog đánh giá ---
            if (showRatingDialog && order != null && order!!.deliveryPersonId != null) {
                RatingDialog(
                    deliveryRequestId = order!!.id,
                    clientId = viewModel.clientId,
                    driverId = order!!.deliveryPersonId!!,
                    onDismiss = {
                        showRatingDialog = false
                        ratingViewModel.clearError()
                    },
                    onSubmit = { stars, comment ->
                        ratingViewModel.submitRating(
                            deliveryRequestId = order!!.id,
                            clientId = viewModel.clientId,
                            driverId = order!!.deliveryPersonId!!,
                            stars = stars,
                            comment = comment.ifBlank { null }
                        )
                    },
                    isSubmitting = ratingUiState.isSubmitting,
                    errorMessage = ratingUiState.errorMessage
                )
            }
        }
    }
}

@Composable
private fun DetailCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            content()
        }
    }
}

@Composable
private fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun DeliveryStatus.detailLabel(): String = when (this) {
    DeliveryStatus.CHO_TIEP_NHAN -> "Đang chờ tài xế"
    DeliveryStatus.DA_CHAP_NHAN -> "Tài xế đã nhận đơn"
    DeliveryStatus.DA_DEN_NHA_HANG -> "Tài xế đã đến điểm lấy"
    DeliveryStatus.DA_LAY_HANG -> "Đã lấy hàng"
    DeliveryStatus.DA_DEN_KHACH_HANG -> "Đã đến điểm giao"
    DeliveryStatus.DA_GIAO -> "Giao hàng thành công"
    DeliveryStatus.DA_HUY -> "Đơn hàng đã hủy"
}
