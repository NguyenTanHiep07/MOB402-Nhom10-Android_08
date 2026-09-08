package com.mob10.deliveryapp.data.remote

import android.content.Context
import com.mob10.deliveryapp.BuildConfig
import com.mob10.deliveryapp.data.remote.api.AdminApiService
import com.mob10.deliveryapp.data.remote.api.AuthApiService
import com.mob10.deliveryapp.data.remote.api.DriverApiService
import com.mob10.deliveryapp.data.remote.api.LocationApiService
import com.mob10.deliveryapp.data.remote.api.OrderApiService
import com.mob10.deliveryapp.data.remote.api.RatingApiService as RatingApi
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
            // Even BASIC logs full URLs, including address searches. Keep customer input out of logcat.
            level = HttpLoggingInterceptor.Level.NONE
        }

        val authInterceptor = AuthInterceptor(tokenManager)

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .callTimeout(45, TimeUnit.SECONDS)
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

    /** API tài xế — đơn chờ, nhận đơn, từ chối, trạng thái và thống kê. */
    val driverApi: DriverApiService by lazy {
        retrofit.create(DriverApiService::class.java)
    }
    val deliveryPhotoApi: com.mob10.deliveryapp.data.remote.api.DeliveryPhotoApi by lazy {
        retrofit.create(com.mob10.deliveryapp.data.remote.api.DeliveryPhotoApi::class.java)
    }

    /** Order API chung — danh sách đơn, chi tiết, lịch sử, hủy đơn. */
    val orderApi: OrderApiService by lazy {
        retrofit.create(OrderApiService::class.java)
    }

    /** Location API — autocomplete địa chỉ và ước lượng tuyến đường. */
    val locationApi: LocationApiService by lazy {
        retrofit.create(LocationApiService::class.java)
    }

    /** Admin API — quản lý tài khoản, tài xế, đơn hàng. */
    val adminApi: AdminApiService by lazy {
        retrofit.create(AdminApiService::class.java)
    }

    val recoveryApi: com.mob10.deliveryapp.data.remote.api.RecoveryApiService by lazy {
        retrofit.create(com.mob10.deliveryapp.data.remote.api.RecoveryApiService::class.java)
    }

    /** Rating API — đánh giá tài xế. */
    val ratingApi: RatingApi by lazy {
        retrofit.create(RatingApi::class.java)
    }
}
