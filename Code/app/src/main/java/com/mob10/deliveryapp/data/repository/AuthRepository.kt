package com.mob10.deliveryapp.data.repository

import com.mob10.deliveryapp.data.remote.RemoteDataSource
import com.mob10.deliveryapp.data.remote.RetrofitClient
import com.mob10.deliveryapp.data.remote.api.AuthApiService
import com.mob10.deliveryapp.data.remote.dto.LoginRequest
import com.mob10.deliveryapp.data.remote.dto.LoginResponse
import com.mob10.deliveryapp.data.remote.dto.UserSummaryDto
import com.mob10.deliveryapp.data.session.SessionStorage
import com.mob10.deliveryapp.data.session.TokenManager
import com.mob10.deliveryapp.data.util.NetworkResult

/**
 * Repository xử lý Authentication qua REST API.
 *
 * Flow login:
 * 1. Gọi POST /auth/login
 * 2. Nhận accessToken + user info
 * 3. Lưu token vào TokenManager (SharedPreferences)
 * 4. Lưu userId vào SessionStorage (DataStore)
 * 5. Trả NetworkResult cho ViewModel
 */
class AuthRepository(
    private val authApi: AuthApiService,
    private val tokenManager: TokenManager,
    private val sessionStorage: SessionStorage
) {

    /**
     * Đăng nhập bằng username/password.
     * @return NetworkResult.Success chứa LoginResponse (có accessToken và user info)
     */
    suspend fun login(username: String, password: String): NetworkResult<LoginResponse> {
        val result = RemoteDataSource.safeApiCall {
            authApi.login(LoginRequest(username = username, password = password))
        }

        // Lưu token + session khi login thành công
        if (result is NetworkResult.Success) {
            val loginResponse = result.data
            tokenManager.saveToken(
                token = loginResponse.accessToken,
                type = loginResponse.tokenType,
                expiresInMs = loginResponse.expiresInMs
            )
            sessionStorage.saveUserId(loginResponse.user.id.toInt())
        }

        return result
    }

    /** Đăng xuất — xóa token và session. */
    suspend fun logout() {
        tokenManager.clearToken()
        sessionStorage.clear()
    }

    /** Kiểm tra đã đăng nhập chưa (token còn hạn). */
    fun isLoggedIn(): Boolean = tokenManager.isTokenValid

    /** Lấy thông tin user đã lưu sau login (nếu cần lấy lại từ server, gọi API riêng). */
    suspend fun getSavedUserId(): Int? = sessionStorage.getUserId()
}
