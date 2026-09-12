package com.mob10.deliveryapp.data.remote.api

import com.mob10.deliveryapp.data.remote.dto.DriverRatingSummaryDto
import com.mob10.deliveryapp.data.remote.dto.RatingRequest
import com.mob10.deliveryapp.data.remote.dto.RatingResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Rating API Service — đánh giá tài xế.
 *
 * Tất cả endpoint đều cần Authorization header.
 * - POST /ratings: chỉ CLIENT chủ đơn, sau khi đơn DA_GIAO.
 * - GET /ratings: xem đánh giá đơn — chủ đơn, tài xế giao hoặc Admin.
 * - GET /ratings/drivers/{driverId}/summary: tổng quan sao.
 */
interface RatingApiService {

    /** Gửi đánh giá tài xế sau giao hàng thành công. */
    @POST("ratings")
    suspend fun submitRating(@Body request: RatingRequest): Response<RatingResponse>

    /**
     * Xem đánh giá của một đơn.
     * Server trả 404 RATING_NOT_FOUND nếu đơn chưa được đánh giá.
     */
    @GET("ratings")
    suspend fun getRatingByOrder(
        @Query("deliveryRequestId") deliveryRequestId: Long
    ): Response<RatingResponse>

    /** Tổng quan sao trung bình của tài xế. */
    @GET("ratings/drivers/{driverId}/summary")
    suspend fun getDriverRatingSummary(
        @Path("driverId") driverId: Long
    ): Response<DriverRatingSummaryDto>
}
