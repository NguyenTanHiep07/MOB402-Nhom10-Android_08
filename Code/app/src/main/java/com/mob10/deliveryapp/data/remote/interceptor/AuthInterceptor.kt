package com.mob10.deliveryapp.data.remote.interceptor

import com.mob10.deliveryapp.data.session.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp Interceptor tự động gắn Authorization header vào mọi request.
 *
 * - Bỏ qua endpoint `/auth/login` (không cần token).
 * - Nếu token hết hạn hoặc null → request đi mà không có header Authorization,
 *   server sẽ trả 401 và client xử lý ở Repository layer.
 */
class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Không gắn token cho endpoint login
        if (originalRequest.url.encodedPath.contains("/auth/login")) {
            return chain.proceed(originalRequest)
        }

        val token = tokenManager.accessToken
        if (token.isNullOrBlank()) {
            return chain.proceed(originalRequest)
        }

        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "${tokenManager.tokenType ?: "Bearer"} $token")
            .header("Content-Type", "application/json")
            .build()

        return chain.proceed(authenticatedRequest)
    }
}
