package com.mob10.deliveryapp.ui.components

import androidx.annotation.RawRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.mob10.deliveryapp.ui.theme.UthPrimary

/**
 * Overlay hiển thị Lottie animation fullscreen với background mờ.
 *
 * @param visible Điều khiển hiển thị/ẩn overlay
 * @param animationResId Resource ID của file Lottie JSON trong res/raw
 * @param title Tiêu đề hiển thị dưới animation
 * @param subtitle Phụ đề (tùy chọn)
 * @param buttonText Nội dung nút bấm (nếu null thì tự đóng sau animation)
 * @param iterations Số lần lặp animation (mặc định 1)
 * @param onDismiss Callback khi overlay đóng (animation xong hoặc user bấm nút)
 */
@Composable
fun LottieOverlay(
    visible: Boolean,
    @RawRes animationResId: Int,
    title: String,
    subtitle: String? = null,
    buttonText: String? = null,
    iterations: Int = 1,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        val composition by rememberLottieComposition(
            LottieCompositionSpec.RawRes(animationResId)
        )
        val progress by animateLottieCompositionAsState(
            composition = composition,
            iterations = iterations,
            isPlaying = true
        )

        // Tự động đóng nếu không có nút và animation chạy xong
        if (buttonText == null) {
            LaunchedEffect(progress) {
                if (progress >= 1f) {
                    onDismiss()
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
            ) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.size(260.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                subtitle?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }

                buttonText?.let {
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = UthPrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = it,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
