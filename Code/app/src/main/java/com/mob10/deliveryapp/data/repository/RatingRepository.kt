package com.mob10.deliveryapp.data.repository

import com.mob10.deliveryapp.data.remote.RemoteDataSource
import com.mob10.deliveryapp.data.remote.api.RatingApiService
import com.mob10.deliveryapp.data.remote.dto.DriverRatingSummaryDto
import com.mob10.deliveryapp.data.remote.dto.RatingRequest
import com.mob10.deliveryapp.data.remote.dto.RatingResponse
import com.mob10.deliveryapp.data.util.NetworkResult
import com.mob10.deliveryapp.data.util.mapData

/**
 * Repository REST cho Rating API — sử dụng RetrofitClient chung.
 *
 * Đã chuyển sang dùng RemoteDataSource.safeApiCall + NetworkResult
 * thay vì try-catch thủ công, nhất quán với các repository khác.
 */
class RatingRepository(private val api: RatingApiService) {

    /**
     * Gửi đánh giá tài xế.
     *
     * Server kiểm tra:
     * - Đơn phải ở trạng thái DA_GIAO.
     * - Chỉ chủ đơn được đánh giá.
     * - Mỗi đơn chỉ được đánh giá một lần.
     */
    suspend fun submitRating(
        deliveryRequestId: Long,
        clientId: Long,
        driverId: Long,
        stars: Int,
        comment: String?
    ): NetworkResult<RatingResponse> {
        val result = RemoteDataSource.safeApiCall {
            api.submitRating(
                RatingRequest(
                    deliveryRequestId = deliveryRequestId,
                    clientId = clientId,
                    driverId = driverId,
                    stars = stars,
                    comment = comment
                )
            )
        }
        // Cung cấp thông báo thân thiện cho lỗi thường gặp
        if (result is NetworkResult.Error) {
            return when (result.code) {
                "RATING_ALREADY_EXISTS" -> result.copy(
                    message = "Bạn đã đánh giá đơn hàng này rồi."
                )
                "ORDER_NOT_DELIVERED" -> result.copy(
                    message = "Đơn hàng chưa được giao thành công, không thể đánh giá."
                )
                else -> result
            }
        }
        return result
    }

    /**
     * Kiểm tra đơn hàng đã được đánh giá chưa.
     *
     * Server trả 404 RATING_NOT_FOUND nếu chưa có rating.
     */
    suspend fun getExistingRating(deliveryRequestId: Long): NetworkResult<RatingResponse> =
        RemoteDataSource.safeApiCall {
            api.getRatingByOrder(deliveryRequestId)
        }

    /**
     * Lấy tổng quan sao trung bình của tài xế.
     */
    suspend fun getDriverSummary(driverId: Long): NetworkResult<DriverRatingSummaryDto> =
        RemoteDataSource.safeApiCall {
            api.getDriverRatingSummary(driverId)
        }
}