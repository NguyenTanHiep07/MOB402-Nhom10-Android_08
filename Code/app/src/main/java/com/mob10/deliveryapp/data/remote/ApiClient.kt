package com.mob10.deliveryapp.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Client Retrofit DUY NHẤT dùng chung cho toàn bộ app (Rating, Order, Admin, Auth...).
 * Không tạo thêm Retrofit.Builder() ở nơi khác — mọi ApiService mới đều thêm vào đây.
 */
object ApiClient {
    // TODO: thay bằng địa chỉ backend thật do Thịnh cung cấp.
    // Nếu chạy backend local + test trên emulator, dùng "http://10.0.2.2:PORT/"
    private const val BASE_URL = "https://TODO-thay-bang-domain-that.com/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // TODO: khi có API đăng nhập trả JWT, thêm 1 Interceptor ở đây để tự động
    // gắn header "Authorization: Bearer <token>" vào mọi request.
    // Ví dụ:
    // private val authInterceptor = Interceptor { chain ->
    //     val token = runBlocking { sessionStorage.getToken() }
    //     val request = chain.request().newBuilder()
    //         .apply { if (token != null) addHeader("Authorization", "Bearer $token") }
    //         .build()
    //     chain.proceed(request)
    // }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        // .addInterceptor(authInterceptor) // bật khi có JWT
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // --- Danh sách API Service dùng chung ---
    val ratingApiService: RatingApiService by lazy {
        retrofit.create(RatingApiService::class.java)
    }

    // TODO: thêm khi có OrderApiService từ Thịnh
    // val orderApiService: OrderApiService by lazy {
    //     retrofit.create(OrderApiService::class.java)
    // }

    // TODO: thêm khi làm Admin API
    val adminApiService: AdminApiService by lazy {
        retrofit.create(AdminApiService::class.java)
    }

    // TODO: thêm khi có API đăng nhập
    // val authApiService: AuthApiService by lazy {
    //     retrofit.create(AuthApiService::class.java)
    // }
}