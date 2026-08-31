package com.mob10.deliveryapp.data.remote

import android.content.Context
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

    // Emulator: 10.0.2.2 = localhost của máy host
    // Device thật trên cùng WiFi: thay bằng IP LAN (ví dụ: 192.168.1.x)
    private const val BASE_URL = "http://10.0.2.2:8080/api/"

    private const val CONNECT_TIMEOUT = 30L
    private const val READ_TIMEOUT = 30L
    private const val WRITE_TIMEOUT = 30L

    private lateinit var tokenManager: TokenManager
    private lateinit var retrofit: Retrofit

    /** Phải gọi trước khi sử dụng bất kỳ API service nào. */
    fun init(context: Context) {
        tokenManager = TokenManager(context)

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
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
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

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
