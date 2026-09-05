package com.mob10.deliveryapp.ui.rating

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.clickable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mob10.deliveryapp.ui.theme.UthPrimary

@Composable
fun RatingScreen(
    deliveryRequestId: Long,
    clientId: Long,
    driverId: Long,
    onDone: () -> Unit,
    viewModel: RatingViewModel = viewModel(factory = RatingViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var selectedStars by rememberSaveable(deliveryRequestId) { mutableIntStateOf(0) }
    var comment by rememberSaveable(deliveryRequestId) { mutableStateOf("") }

    LaunchedEffect(deliveryRequestId) {
        viewModel.checkExistingRating(deliveryRequestId)
    }

    LaunchedEffect(uiState.submitSuccess) {
        if (uiState.submitSuccess) onDone()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Đánh giá tài xế", fontSize = 20.sp, color = UthPrimary)
        Spacer(Modifier.height(16.dp))

        if (uiState.alreadyRated) {
            Text("Bạn đã đánh giá đơn hàng này rồi.")
            return@Column
        }

        Row {
            for (i in 1..5) {
                Icon(
                    imageVector = if (i <= selectedStars) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Sao $i",
                    tint = UthPrimary,
                    modifier = Modifier
                        .size(36.dp)
                        .clickable(enabled = !uiState.isSubmitting) { selectedStars = i }
                        .padding(4.dp)
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = comment,
            onValueChange = { comment = it.take(1000) },
            label = { Text("Nhận xét (tuỳ chọn)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        if (uiState.errorMessage != null) {
            Text(uiState.errorMessage ?: "", color = Color.Red)
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = {
                viewModel.submitRating(
                    deliveryRequestId = deliveryRequestId,
                    clientId = clientId,
                    driverId = driverId,
                    stars = selectedStars,
                    comment = comment.ifBlank { null }
                )
            },
            enabled = selectedStars in 1..5 && !uiState.isSubmitting
        ) {
            if (uiState.isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Text("Gửi đánh giá")
            }
        }
    }
}
