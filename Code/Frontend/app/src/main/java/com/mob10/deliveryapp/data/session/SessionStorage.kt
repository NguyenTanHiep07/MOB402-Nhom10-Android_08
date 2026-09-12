package com.mob10.deliveryapp.data.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val AUTH_SESSION_DATA_STORE_NAME = "auth_session"

private val Context.authSessionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = AUTH_SESSION_DATA_STORE_NAME
)

// Interface quản lý lưu trữ phiên đăng nhập và token của người dùng
interface SessionStorage {
    // Lấy ID người dùng hiện tại đang đăng nhập
    suspend fun getUserId(): Int?

    // Lưu ID người dùng vào DataStore
    suspend fun saveUserId(userId: Int)

    // Lấy JWT Access Token hiện tại
    suspend fun getAccessToken(): String?

    // Lưu JWT Access Token vào DataStore
    suspend fun saveAccessToken(accessToken: String)

    // Xóa toàn bộ phiên đăng nhập (đăng xuất)
    suspend fun clear()
}

// Triển khai SessionStorage sử dụng Jetpack DataStore Preferences
class DataStoreSessionStorage(context: Context) : SessionStorage {
    private val dataStore = context.applicationContext.authSessionDataStore

    // Đọc user ID từ DataStore
    override suspend fun getUserId(): Int? = dataStore.data
        .map { preferences -> preferences[CURRENT_USER_ID] }
        .first()

    // Ghi user ID vào DataStore
    override suspend fun saveUserId(userId: Int) {
        require(userId > 0) { "Room user id must be positive." }
        dataStore.edit { preferences ->
            preferences[CURRENT_USER_ID] = userId
        }
    }

    // Đọc token từ DataStore
    override suspend fun getAccessToken(): String? = dataStore.data
        .map { preferences -> preferences[ACCESS_TOKEN] }
        .first()

    // Ghi token vào DataStore
    override suspend fun saveAccessToken(accessToken: String) {
        require(accessToken.isNotBlank()) { "Access token must not be blank." }
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = accessToken
        }
    }

    // Xóa sạch user ID và token khỏi DataStore khi logout
    override suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.remove(CURRENT_USER_ID)
            preferences.remove(ACCESS_TOKEN)
        }
    }

    private companion object {
        val CURRENT_USER_ID = intPreferencesKey("current_user_id")
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
    }
}