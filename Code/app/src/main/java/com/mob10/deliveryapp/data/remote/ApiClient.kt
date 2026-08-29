package com.mob10.deliveryapp.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    // TODO: thay bằng địa chỉ backend thật do Thịnh cung cấp.
    // Nếu chạy backend local + test trên emulator, dùng "http://10.0.2.2:PORT/"
    private const val BASE_URL = "https://TODO-thay-bang-domain-that.com/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val ratingApiService: RatingApiService by lazy {
        retrofit.create(RatingApiService::class.java)
    }
}