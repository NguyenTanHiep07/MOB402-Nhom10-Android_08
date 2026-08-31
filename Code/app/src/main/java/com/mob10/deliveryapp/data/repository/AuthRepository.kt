package com.mob10.deliveryapp.data.repository

import com.mob10.deliveryapp.data.remote.RemoteDataSource
import com.mob10.deliveryapp.data.remote.api.AuthApiService
import com.mob10.deliveryapp.data.remote.dto.LoginRequest
import com.mob10.deliveryapp.data.remote.dto.LoginResponse
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
    private val tokenManager: TokenManager
) {

    /**
     * Đăng nhập bằng username/password.
     * @return NetworkResult.Success chứa LoginResponse (có accessToken và user info)
     */
    suspend fun login(username: String, password: String): NetworkResult<LoginResponse> {
        val result = RemoteDataSource.safeApiCall {
            authApi.login(LoginRequest(username = username, password = password))
        }

        // Session/current user chỉ được lưu sau khi AuthViewModel ánh xạ user thành công.
        if (result is NetworkResult.Success) {
            val loginResponse = result.data
            tokenManager.saveToken(
                token = loginResponse.accessToken,
                type = loginResponse.tokenType,
                expiresInMs = loginResponse.expiresInMs
            )
        }

        return result
    }

    /** Đăng xuất khỏi API — xóa access token. */
    suspend fun logout() {
        tokenManager.clearToken()
    }

    /** Kiểm tra đã đăng nhập chưa (token còn hạn). */
    fun isLoggedIn(): Boolean = tokenManager.isTokenValid

}
