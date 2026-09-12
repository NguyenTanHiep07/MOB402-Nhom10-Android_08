package com.mob10.deliveryapp.ui.driver

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mob10.deliveryapp.data.repository.DeliveryPhotoRepository
import com.mob10.deliveryapp.data.util.NetworkResult
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.ByteArrayOutputStream

data class PhotoState(val image: String? = null, val busy: Boolean = false, val error: String? = null, val completed: Boolean = false)

class DeliveryPhotoViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = DeliveryPhotoRepository()
    private val mutable = MutableStateFlow(PhotoState())
    val state = mutable.asStateFlow()
    private fun file(id: Long) = File(getApplication<Application>().filesDir, "delivery_photos/order_$id.jpg").apply { parentFile?.mkdirs() }
    fun uri(id: Long) = FileProvider.getUriForFile(getApplication(), "${getApplication<Application>().packageName}.deliveryphotos", file(id))
    fun cameraError() { mutable.value = mutable.value.copy(error = "Không mở được máy ảnh. Hãy kiểm tra ứng dụng máy ảnh trên thiết bị.") }
    fun discard(id: Long) { file(id).delete(); mutable.value = PhotoState() }
    fun restore(id: Long) { if (mutable.value.image == null && !mutable.value.busy && !mutable.value.completed && file(id).length() > 0) captured(id, true) }
    fun captured(id: Long, success: Boolean) {
        if (!success) { mutable.value = mutable.value.copy(error = "Chưa chụp ảnh. Bạn có thể thử lại."); return }
        viewModelScope.launch {
            mutable.value = mutable.value.copy(busy = true, error = null)
            try {
                val encoded = withContext(Dispatchers.IO) {
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeFile(file(id).path, bounds)
                    require(bounds.outWidth > 0 && bounds.outHeight > 0)
                    var sample = 1
                    while (maxOf(bounds.outWidth, bounds.outHeight) / sample > 1200) sample *= 2
                    val raw = BitmapFactory.decodeFile(file(id).path, BitmapFactory.Options().apply { inSampleSize = sample }) ?: error("image")
                    val orientation = android.media.ExifInterface(file(id).path).getAttributeInt(android.media.ExifInterface.TAG_ORIENTATION, 1)
                    val matrix = android.graphics.Matrix().apply {
                        when (orientation) {
                            2 -> setScale(-1f, 1f)
                            3 -> setRotate(180f)
                            4 -> { setRotate(180f); postScale(-1f, 1f) }
                            5 -> { setRotate(90f); postScale(-1f, 1f) }
                            6 -> setRotate(90f)
                            7 -> { setRotate(-90f); postScale(-1f, 1f) }
                            8 -> setRotate(-90f)
                        }
                    }
                    val bitmap = Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, matrix, true)
                    try {
                        ByteArrayOutputStream().use { out ->
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, out)
                            require(out.size() <= 500000)
                            Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
                        }
                    } finally { if (raw !== bitmap) raw.recycle(); bitmap.recycle() }
                }
                mutable.value = PhotoState(image = encoded)
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { mutable.value = PhotoState(error = "Ảnh không đọc được hoặc quá lớn. Hãy chụp lại.") }
        }
    }
    fun load(id: Long) {
        if (mutable.value.busy) return
        viewModelScope.launch {
            mutable.value = mutable.value.copy(busy = true, error = null)
            when (val result = repo.load(id)) {
                is NetworkResult.Success -> mutable.value = PhotoState(image = result.data.image)
                is NetworkResult.Error -> mutable.value = PhotoState(error = result.message)
                else -> mutable.value = PhotoState()
            }
        }
    }
    fun complete(id: Long) {
        val photo = mutable.value.image ?: return
        if (mutable.value.busy || mutable.value.completed) return
        viewModelScope.launch {
            mutable.value = mutable.value.copy(busy = true, error = null)
            when (val result = repo.complete(id, photo)) {
                is NetworkResult.Success -> { file(id).delete(); mutable.value = PhotoState(completed = true) }
                is NetworkResult.Error -> mutable.value = mutable.value.copy(busy = false, error = result.message)
                else -> mutable.value = mutable.value.copy(busy = false, error = "Chưa nhận được xác nhận. Hãy thử lại.")
            }
        }
    }
}

@Composable
fun DeliveryPhotoPanel(orderId: Long, capture: Boolean = false, onCompleted: () -> Unit = {}) {
    val vm: DeliveryPhotoViewModel = viewModel(key = "photo_${orderId}_$capture")
    val state by vm.state.collectAsStateWithLifecycle()
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { vm.captured(orderId, it) }
    LaunchedEffect(orderId, capture) { if (!capture) vm.load(orderId) else vm.restore(orderId) }
    val callback by rememberUpdatedState(onCompleted)
    LaunchedEffect(state.completed) { if (state.completed) callback() }
    var bitmap by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    LaunchedEffect(state.image) {
        bitmap = withContext(Dispatchers.Default) { runCatching {
            state.image?.let { Base64.decode(it, Base64.DEFAULT) }?.let { BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap() }
        }.getOrNull() }
    }
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .35f))) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Ảnh xác nhận giao hàng", style = MaterialTheme.typography.titleMedium)
            if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            bitmap?.let { Image(it, "Kiện hàng đã giao", Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp)), contentScale = ContentScale.Fit) }
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            if (capture) {
                Text("Chụp kiện hàng tại điểm giao, tránh chụp khuôn mặt hoặc giấy tờ cá nhân.", style = MaterialTheme.typography.bodySmall)
                OutlinedButton(onClick = { try { camera.launch(vm.uri(orderId)) } catch (_: Exception) { vm.cameraError() } }, enabled = !state.busy && !state.completed, modifier = Modifier.fillMaxWidth()) {
                    Text(if (state.image == null) "Chụp ảnh kiện hàng" else "Chụp lại ảnh")
                }
                if (state.image != null) TextButton(onClick = { vm.discard(orderId) }, enabled = !state.busy && !state.completed) { Text("Bỏ ảnh vừa chụp") }
                Button(onClick = { vm.complete(orderId) }, enabled = state.image != null && !state.busy && !state.completed, modifier = Modifier.fillMaxWidth()) {
                    Text(if (state.busy) "Đang xác nhận…" else "Xác nhận giao thành công")
                }
            } else if (state.image == null && !state.busy && state.error == null) Text("Đơn này chưa có ảnh xác nhận.", style = MaterialTheme.typography.bodySmall)
            if (!capture && state.error != null) TextButton(onClick = { vm.load(orderId) }) { Text("Tải lại ảnh") }
        }
    }
}
