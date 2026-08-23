package com.mob10.deliveryapp.data.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val AUTH_SESSION_DATA_STORE_NAME = "auth_session"

private val Context.authSessionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = AUTH_SESSION_DATA_STORE_NAME
)

/**
 * Lưu duy nhất id của tài khoản Room đang đăng nhập.
 * Role và thông tin người dùng luôn được đọc lại từ Room khi khôi phục phiên.
 */
interface SessionStorage {
    suspend fun getUserId(): Int?

    suspend fun saveUserId(userId: Int)

    suspend fun clear()
}

class DataStoreSessionStorage(context: Context) : SessionStorage {
    private val dataStore = context.applicationContext.authSessionDataStore

    override suspend fun getUserId(): Int? = dataStore.data
        .map { preferences -> preferences[CURRENT_USER_ID] }
        .first()

    override suspend fun saveUserId(userId: Int) {
        require(userId > 0) { "Room user id must be positive." }
        dataStore.edit { preferences ->
            preferences[CURRENT_USER_ID] = userId
        }
    }

    override suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.remove(CURRENT_USER_ID)
        }
    }

    private companion object {
        val CURRENT_USER_ID = intPreferencesKey("current_user_id")
    }
}
