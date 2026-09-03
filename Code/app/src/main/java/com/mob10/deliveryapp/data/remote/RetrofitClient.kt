package com.mob10.deliveryapp.data.remote

import android.content.Context
import com.mob10.deliveryapp.BuildConfig
import com.mob10.deliveryapp.data.remote.api.AuthApiService
import com.mob10.deliveryapp.data.remote.api.DriverApiService
import com.mob10.deliveryapp.data.remote.api.OrderApiService
import com.mob10.deliveryapp.data.remote.interceptor.AuthInterceptor
import com.mob10.deliveryapp.data.session.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit client singleton — cung cấp tất cả API service cho ứng dụng.
 *
 * - OkHttp với AuthInterceptor (tự gắn Bearer token)
 * - Logging interceptor (debug)
 * - Timeout: connect 30s, read 30s, write 30s
 *
 * Sử dụng: `RetrofitClient.init(context)` trong Application/Activity,
 *          sau đó truy cập `RetrofitClient.driverApi`, `RetrofitClient.authApi`, v.v.
 */
object RetrofitClient {
    private const val CONNECT_TIMEOUT = 30L
    private const val READ_TIMEOUT = 30L
    private const val WRITE_TIMEOUT = 30L

    @Volatile
    private var initialized = false
    private lateinit var tokenManager: TokenManager
    private lateinit var retrofit: Retrofit

    /** Phải gọi trước khi sử dụng bất kỳ API service nào. */
    @Synchronized
    fun init(context: Context) {
        if (initialized) return

        tokenManager = TokenManager(context.applicationContext)

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val authInterceptor = AuthInterceptor(tokenManager)

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(normalizeBaseUrl(BuildConfig.API_BASE_URL))
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        initialized = true
    }

    private fun normalizeBaseUrl(value: String): String =
        value.trim().let { if (it.endsWith('/')) it else "$it/" }

    fun getTokenManager(): TokenManager = tokenManager

    /** Auth API — login, không cần token. */
    val authApi: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    /** Driver/Shipper API — Open Pool, Accept, Reject, Status, Statistics, v.v. */
    val driverApi: DriverApiService by lazy {
        retrofit.create(DriverApiService::class.java)
    }

    /** Order API chung — danh sách đơn, chi tiết, lịch sử, hủy đơn. */
    val orderApi: OrderApiService by lazy {
        retrofit.create(OrderApiService::class.java)
    }

    /** Rating API — giữ tương thích với code cũ. */
    val ratingApi: RatingApiService by lazy {
        retrofit.create(RatingApiService::class.java)
    }
}
