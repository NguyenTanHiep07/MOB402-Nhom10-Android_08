package com.mob10.deliveryapp.ui.customer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mob10.deliveryapp.data.local.entity.DeliveryRequestEntity
import com.mob10.deliveryapp.data.model.DeliveryStatus
import com.mob10.deliveryapp.ui.rating.RatingDialog
import com.mob10.deliveryapp.ui.rating.RatingViewModel
import com.mob10.deliveryapp.ui.rating.RatingViewModelFactory

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

                        // --- Nút Đánh giá: chỉ hiện khi đơn đã giao xong và có tài xế ---
                        val driverId = currentOrder.deliveryPersonId
                        if (currentOrder.status == DeliveryStatus.DA_GIAO && driverId != null) {
                            if (ratingUiState.alreadyRated) {
                                Text(
                                    "Bạn đã đánh giá đơn này. Cảm ơn bạn!",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            } else {
                                Button(
                                    onClick = { showRatingDialog = true },
                                    enabled = !ratingUiState.isLoading
                                ) {
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