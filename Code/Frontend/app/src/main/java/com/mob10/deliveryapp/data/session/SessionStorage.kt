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

// TH - tuần 3,4: quy định cách lưu và xoá phiên đăng nhập
interface SessionStorage {
    suspend fun getUserId(): Int?

    suspend fun saveUserId(userId: Int)

    suspend fun getAccessToken(): String?

    suspend fun saveAccessToken(accessToken: String)

    suspend fun clear()
}

class DataStoreSessionStorage(context: Context) : SessionStorage {
    private val dataStore = context.applicationContext.authSessionDataStore

    // TH - tuần 3,4: đọc id người dùng đã đăng nhập
    override suspend fun getUserId(): Int? = dataStore.data
        .map { preferences -> preferences[CURRENT_USER_ID] }
        .first()

    // TH - tuần 3,4: lưu id để mở lại app không cần đăng nhập lại
    override suspend fun saveUserId(userId: Int) {
        require(userId > 0) { "Room user id must be positive." }
        dataStore.edit { preferences ->
            preferences[CURRENT_USER_ID] = userId
        }
    }

    override suspend fun getAccessToken(): String? = dataStore.data
        .map { preferences -> preferences[ACCESS_TOKEN] }
        .first()

    override suspend fun saveAccessToken(accessToken: String) {
        require(accessToken.isNotBlank()) { "Access token must not be blank." }
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = accessToken
        }
    }

    // TH - tuần 3,4: xoá session; token là phần bổ sung sau
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
