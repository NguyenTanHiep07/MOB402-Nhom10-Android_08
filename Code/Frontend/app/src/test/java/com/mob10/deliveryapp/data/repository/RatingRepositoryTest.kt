package com.mob10.deliveryapp.data.repository

import com.mob10.deliveryapp.data.remote.api.RatingApiService
import com.mob10.deliveryapp.data.remote.dto.DriverRatingSummaryDto
import com.mob10.deliveryapp.data.remote.dto.RatingRequest
import com.mob10.deliveryapp.data.remote.dto.RatingResponse
import com.mob10.deliveryapp.data.util.NetworkResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class RatingRepositoryTest {

    private lateinit var fakeRatingApi: FakeRatingApiService
    private lateinit var repository: RatingRepository

    @Before
    fun setUp() {
        fakeRatingApi = FakeRatingApiService()
        repository = RatingRepository(fakeRatingApi)
    }

    @Test
    fun testSubmitRatingSuccess() = runBlocking {
        val result = repository.submitRating(
            deliveryRequestId = 10L,
            clientId = 1L,
            driverId = 6L,
            stars = 5,
            comment = "Giao hang nhanh"
        )
        assertTrue(result is NetworkResult.Success)
        val response = (result as NetworkResult.Success).data
        assertEquals(10L, response.deliveryRequestId)
        assertEquals(5, response.stars)
    }

    @Test
    fun testSubmitRatingAlreadyExistsError() = runBlocking {
        fakeRatingApi.shouldReturnAlreadyExists = true
        val result = repository.submitRating(10L, 1L, 6L, 5, null)
        assertTrue(result is NetworkResult.Error)
        val error = result as NetworkResult.Error
        assertEquals("RATING_ALREADY_EXISTS", error.code)
        assertEquals("Bạn đã đánh giá đơn hàng này rồi.", error.message)
    }

    @Test
    fun testGetExistingRatingNotFound() = runBlocking {
        fakeRatingApi.shouldReturnNotFound = true
        val result = repository.getExistingRating(10L)
        assertTrue(result is NetworkResult.Error)
        val error = result as NetworkResult.Error
        assertEquals(404, error.httpCode)
    }

    @Test
    fun testGetDriverSummarySuccess() = runBlocking {
        val result = repository.getDriverSummary(6L)
        assertTrue(result is NetworkResult.Success)
        val summary = (result as NetworkResult.Success).data
        assertEquals(6L, summary.driverId)
        assertEquals(12L, summary.ratingCount)
        assertEquals(4.75, summary.averageStars, 0.01)
    }

    // ── Fake Implementation ──────────────────────────────────────────

    class FakeRatingApiService : RatingApiService {
        var shouldReturnAlreadyExists = false
        var shouldReturnNotFound = false

        override suspend fun submitRating(request: RatingRequest): Response<RatingResponse> {
            if (shouldReturnAlreadyExists) {
                val errorJson = """{"status":409,"code":"RATING_ALREADY_EXISTS","message":"Already rated"}"""
                return Response.error(409, okhttp3.ResponseBody.create(null, errorJson))
            }
            val response = RatingResponse(
                id = 1L,
                deliveryRequestId = request.deliveryRequestId,
                clientId = request.clientId,
                driverId = request.driverId,
                stars = request.stars,
                comment = request.comment,
                createdAt = "2026-09-03T01:30:00Z"
            )
            return Response.success(response)
        }

        override suspend fun getRatingByOrder(deliveryRequestId: Long): Response<RatingResponse> {
            if (shouldReturnNotFound) {
                val errorJson = """{"status":404,"code":"RATING_NOT_FOUND","message":"Not found"}"""
                return Response.error(404, okhttp3.ResponseBody.create(null, errorJson))
            }
            val response = RatingResponse(
                id = 1L,
                deliveryRequestId = deliveryRequestId,
                clientId = 1L,
                driverId = 6L,
                stars = 5,
                comment = "Good",
                createdAt = "2026-09-03T01:30:00Z"
            )
            return Response.success(response)
        }

        override suspend fun getDriverRatingSummary(driverId: Long): Response<DriverRatingSummaryDto> {
            return Response.success(DriverRatingSummaryDto(driverId = driverId, ratingCount = 12L, averageStars = 4.75))
        }
    }
}
