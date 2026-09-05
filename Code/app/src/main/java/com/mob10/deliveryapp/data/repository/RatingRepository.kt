package com.mob10.deliveryapp.data.repository

import com.mob10.deliveryapp.data.remote.RatingApiService
import com.mob10.deliveryapp.data.remote.dto.RatingRequest
import com.mob10.deliveryapp.data.remote.dto.RatingResponse

sealed class RatingSubmitResult {
    data class Success(val response: RatingResponse) : RatingSubmitResult()
    data class Error(val message: String) : RatingSubmitResult()
}

sealed class RatingFetchResult {
    data class Found(val response: RatingResponse) : RatingFetchResult()
    object NotRatedYet : RatingFetchResult()
    data class Error(val message: String) : RatingFetchResult()
}

/**
 * Repository CHỈ gọi API backend cho Rating — không tạo Room entity/DAO,
 * đúng yêu cầu ticket WNEW-04.
 */
class RatingRepository(private val api: RatingApiService) {

    suspend fun submitRating(
        deliveryRequestId: Int,
        clientId: Int,
        driverId: Int,
        stars: Int,
        comment: String?
    ): RatingSubmitResult {
        return try {
            val response = api.submitRating(
                RatingRequest(
                    deliveryRequestId = deliveryRequestId,
                    clientId = clientId,
                    driverId = driverId,
                    stars = stars,
                    comment = comment
                )
            )
            if (response.isSuccessful && response.body() != null) {
                RatingSubmitResult.Success(response.body()!!)
            } else {
                RatingSubmitResult.Error("Gửi đánh giá thất bại (mã lỗi ${response.code()})")
            }
        } catch (e: Exception) {
            RatingSubmitResult.Error("Không thể kết nối máy chủ: ${e.message}")
        }
    }

    suspend fun getExistingRating(deliveryRequestId: Int): RatingFetchResult {
        return try {
            val response = api.getRatingByOrder(deliveryRequestId)
            when {
                response.isSuccessful && response.body() != null ->
                    RatingFetchResult.Found(response.body()!!)
                response.code() == 404 -> RatingFetchResult.NotRatedYet
                else -> RatingFetchResult.Error("Lỗi máy chủ (mã ${response.code()})")
            }
        } catch (e: Exception) {
            RatingFetchResult.Error("Không thể kết nối máy chủ: ${e.message}")
        }
    }
}