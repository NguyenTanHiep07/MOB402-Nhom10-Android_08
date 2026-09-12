package com.mob10.deliveryapp.data.session

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.asStateFlow

/**
 * Lưu trữ access token bằng SharedPreferences (đọc đồng bộ).
 *
 * OkHttp Interceptor chạy trên background thread và KHÔNG hỗ trợ suspend.
 * SharedPreferences cho phép đọc token đồng bộ trong Interceptor,
 * trong khi DataStore (async) vẫn được dùng cho session userId.
 */
// Quản lý lưu trữ Access Token bằng SharedPreferences để Interceptor đọc đồng bộ
class TokenManager(context: Context) {
    private val _expired = kotlinx.coroutines.flow.MutableStateFlow(false)
    val expired = _expired.asStateFlow()
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Access token hiện tại (null nếu chưa đăng nhập)
    var accessToken: String?
        get() = prefs.getString(KEY_ACCESS_TOKEN, null)
        set(value) { prefs.edit().putString(KEY_ACCESS_TOKEN, value).apply() }

    // Loại token (mặc định Bearer)
    var tokenType: String?
        get() = prefs.getString(KEY_TOKEN_TYPE, "Bearer")
        set(value) { prefs.edit().putString(KEY_TOKEN_TYPE, value).apply() }

    // Thời điểm hết hạn token tính theo milliseconds
    var tokenExpiresAt: Long
        get() = prefs.getLong(KEY_EXPIRES_AT, 0L)
        set(value) { prefs.edit().putLong(KEY_EXPIRES_AT, value).apply() }

    // Kiểm tra token có hợp lệ và còn hạn không
    val isTokenValid: Boolean
        get() {
            val token = accessToken
            return !token.isNullOrBlank() && (tokenExpiresAt == 0L || System.currentTimeMillis() < tokenExpiresAt)
        }

    // Lưu access token và thời gian sống sau khi đăng nhập thành công
    @Synchronized
    fun saveToken(token: String, type: String = "Bearer", expiresInMs: Long = 0L) {
        require(token.isNotBlank() && type.equals("Bearer", ignoreCase = true))
        _expired.value = false
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, token)
            .putString(KEY_TOKEN_TYPE, type)
            .putLong(KEY_EXPIRES_AT, if (expiresInMs > 0) System.currentTimeMillis() + expiresInMs else 0L)
            .apply()
    }

    // Xóa token khỏi SharedPreferences khi đăng xuất
    @Synchronized
    fun clearToken() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_TOKEN_TYPE)
            .remove(KEY_EXPIRES_AT)
            .apply()
    }

    // Hủy bỏ token và phát tín hiệu hết hạn nếu token bị máy chủ từ chối (401)
    @Synchronized
    fun invalidateIfCurrent(rejectedToken: String) {
        if (accessToken == rejectedToken) {
            clearToken()
            _expired.value = true
        }
    }

    companion object {
        private const val PREFS_NAME = "auth_token_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_TOKEN_TYPE = "token_type"
        private const val KEY_EXPIRES_AT = "token_expires_at"
    }
}
