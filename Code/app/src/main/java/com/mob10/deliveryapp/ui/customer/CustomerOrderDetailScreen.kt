package com.mob10.deliveryapp.ui.customer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mob10.deliveryapp.data.local.entity.DeliveryRequestEntity
import com.mob10.deliveryapp.data.model.DeliveryStatus

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết đơn hàng") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("←") }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
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
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Đơn #${currentOrder.id}", style = MaterialTheme.typography.headlineSmall)
                        Text("Trạng thái: ${currentOrder.status.name}")
                        Text("Lấy hàng: ${currentOrder.pickupAddress}")
                        Text("Giao đến: ${currentOrder.deliveryAddress}")
                        Text("Người nhận: ${currentOrder.recipientName} - ${currentOrder.recipientPhone}")
                        Text("Tổng phí: ${currentOrder.totalCost}đ")

                        val canCancel = currentOrder.status == DeliveryStatus.CHO_TIEP_NHAN ||
                                currentOrder.status == DeliveryStatus.DA_CHAP_NHAN

                        if (canCancel) {
                            Button(onClick = { viewModel.cancelOrder(currentOrder.id) }) {
                                Text("Hủy đơn hàng")
                            }
                        }
                    }
                }
            }
        }
    }
}