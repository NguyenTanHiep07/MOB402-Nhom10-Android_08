package com.mob10.deliveryapp.data.remote

import com.mob10.deliveryapp.data.remote.dto.RatingRequest
import com.mob10.deliveryapp.data.remote.dto.RatingResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface RatingApiService {

    @POST("api/ratings")
    suspend fun submitRating(@Body request: RatingRequest): Response<RatingResponse>

    // Dùng để kiểm tra đơn đã được đánh giá chưa, tránh đánh giá trùng.
    // TODO: xác nhận endpoint này với Thịnh — có thể backend trả 404 nếu chưa có rating.
    @GET("api/ratings")
    suspend fun getRatingByOrder(@Query("deliveryRequestId") deliveryRequestId: Int): Response<RatingResponse>
}