package com.mob10.deliveryapp.ui.auth

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mob10.deliveryapp.data.remote.api.*
import com.mob10.deliveryapp.data.repository.AccountRepository
import com.mob10.deliveryapp.data.util.NetworkResult
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.ByteArrayOutputStream

data class AccountState(val profile: AccountProfile? = null, val busy: Boolean = false,
    val error: String? = null, val message: String? = null, val avatarDraft: String? = null,
    val avatarChanged: Boolean = false, val resendAt: Long = 0L, val emailRequested: Boolean = false)

class AccountViewModel : ViewModel() {
    private val repo = AccountRepository()
    private val mutable = MutableStateFlow(AccountState())
    val state = mutable.asStateFlow()
    fun load() {
        if (mutable.value.busy) return
        viewModelScope.launch {
            mutable.value = mutable.value.copy(busy = true, error = null)
            when (val result = repo.profile()) {
                is NetworkResult.Success -> mutable.value = mutable.value.copy(profile = result.data, busy = false)
                is NetworkResult.Error -> mutable.value = mutable.value.copy(busy = false, error = result.message)
                else -> mutable.value = mutable.value.copy(busy = false, error = "Không tải được hồ sơ.")
            }
        }
    }
    fun discardAvatar() { mutable.value = mutable.value.copy(avatarChanged = false, avatarDraft = null) }
    fun emailStatus() {
        if (mutable.value.busy) return
        viewModelScope.launch {
            mutable.value = mutable.value.copy(busy = true, error = null)
            when (val result = repo.emailStatus()) {
                is NetworkResult.Success -> mutable.value = mutable.value.copy(busy = false, message = result.data.message)
                is NetworkResult.Error -> mutable.value = mutable.value.copy(busy = false, error = result.message)
                else -> mutable.value = mutable.value.copy(busy = false)
            }
        }
    }
    fun removeAvatar() { if (!mutable.value.busy) mutable.value = mutable.value.copy(avatarChanged = true, avatarDraft = null) }
    fun selectImage(context: Context, uri: Uri) {
        if (mutable.value.busy) return
        viewModelScope.launch {
            mutable.value = mutable.value.copy(busy = true, error = null)
            try {
                val encoded = withContext(Dispatchers.IO) {
                    val bitmap = if (android.os.Build.VERSION.SDK_INT >= 28) ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { decoder, info, _ ->
                        val ratio = 384.0 / maxOf(info.size.width, info.size.height)
                        if (ratio < 1) decoder.setTargetSize(maxOf(1, (info.size.width * ratio).toInt()), maxOf(1, (info.size.height * ratio).toInt()))
                        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    } else {
                        val bounds = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
                        context.contentResolver.openInputStream(uri)?.use { android.graphics.BitmapFactory.decodeStream(it, null, bounds) }
                        require(bounds.outWidth > 0 && bounds.outHeight > 0)
                        var sample = 1
                        while (maxOf(bounds.outWidth, bounds.outHeight) / sample > 768) sample *= 2
                        val options = android.graphics.BitmapFactory.Options().apply { inSampleSize = sample }
                        val raw = context.contentResolver.openInputStream(uri)?.use { android.graphics.BitmapFactory.decodeStream(it, null, options) }
                            ?: error("Invalid image")
                        val orientation = context.contentResolver.openInputStream(uri)?.use {
                            android.media.ExifInterface(it).getAttributeInt(android.media.ExifInterface.TAG_ORIENTATION, 1)
                        } ?: 1
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
                            val ratio = minOf(1f, 384f / maxOf(raw.width, raw.height))
                            postScale(ratio, ratio)
                        }
                        Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, matrix, true).also { if (it !== raw) raw.recycle() }
                    }
                    try {
                        ByteArrayOutputStream().use { out ->
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 82, out)
                            require(out.size() <= 160000)
                            Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
                        }
                    } finally { bitmap.recycle() }
                }
                mutable.value = mutable.value.copy(busy = false, avatarChanged = true, avatarDraft = encoded)
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { mutable.value = mutable.value.copy(busy = false, error = "Không đọc được ảnh. Hãy chọn ảnh khác.") }
        }
    }
    fun save(username: String, name: String, phone: String, password: String) {
        if (mutable.value.busy) return
        if (!username.matches(Regex("[A-Za-z0-9_.-]{3,80}")) || name.isBlank() || phone.isBlank() || password.isBlank()) {
            mutable.value = mutable.value.copy(error = "Tên đăng nhập cần 3–80 ký tự chữ không dấu, số, dấu chấm, gạch ngang hoặc gạch dưới. Nhập đủ thông tin và mật khẩu hiện tại."); return
        }
        viewModelScope.launch {
            val before = mutable.value
            mutable.value = before.copy(busy = true, error = null, message = null)
            handleProfile(repo.edit(AccountEdit(username, name.trim(), phone.trim(), password,
                if (before.avatarChanged) before.avatarDraft else before.profile?.avatarBase64)), "Đã lưu hồ sơ. Lần đăng nhập sau hãy dùng tên đăng nhập mới.")
        }
    }
    fun link(email: String, password: String) {
        if (mutable.value.busy || System.currentTimeMillis() < mutable.value.resendAt) return
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() || password.isBlank()) {
            mutable.value = mutable.value.copy(error = "Nhập email hợp lệ và mật khẩu hiện tại."); return
        }
        viewModelScope.launch {
            mutable.value = mutable.value.copy(busy = true, error = null, message = null)
            when (val result = repo.link(EmailLink(email.trim(), password))) {
                is NetworkResult.Success -> mutable.value = mutable.value.copy(busy = false, emailRequested = true, resendAt = System.currentTimeMillis() + 60_000, message = result.data.message)
                is NetworkResult.Error -> mutable.value = mutable.value.copy(busy = false, error = result.message)
                else -> mutable.value = mutable.value.copy(busy = false, error = "Không nhận được phản hồi. Kiểm tra hộp thư trước khi gửi lại.")
            }
        }
    }
    fun verify(code: String, password: String) {
        if (mutable.value.busy) return
        if (!code.matches(Regex("[0-9]{6}")) || password.isBlank()) {
            mutable.value = mutable.value.copy(error = "Nhập mã 6 số và mật khẩu hiện tại."); return
        }
        viewModelScope.launch {
            mutable.value = mutable.value.copy(busy = true, error = null, message = null)
            handleProfile(repo.verify(EmailVerify(code, password)), "Đã xác minh email. Bạn có thể dùng số điện thoại để khôi phục mật khẩu.")
        }
    }
    private fun handleProfile(result: NetworkResult<AccountProfile>, message: String) {
        when (result) {
            is NetworkResult.Success -> mutable.value = mutable.value.copy(profile = result.data, busy = false, message = message, avatarChanged = false, avatarDraft = null)
            is NetworkResult.Error -> mutable.value = mutable.value.copy(busy = false, error = result.message)
            else -> mutable.value = mutable.value.copy(busy = false, error = "Không nhận được phản hồi. Hãy làm mới hồ sơ để kiểm tra.")
        }
    }
}
