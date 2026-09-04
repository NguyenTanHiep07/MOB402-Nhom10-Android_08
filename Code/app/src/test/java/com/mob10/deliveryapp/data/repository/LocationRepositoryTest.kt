package com.mob10.deliveryapp.data.repository

import com.mob10.deliveryapp.data.remote.api.LocationApiService
import com.mob10.deliveryapp.data.remote.dto.AddressSuggestionResponseDto
import com.mob10.deliveryapp.data.remote.dto.RouteEstimateRequestDto
import com.mob10.deliveryapp.data.remote.dto.RouteEstimateResponseDto
import com.mob10.deliveryapp.data.util.NetworkResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class LocationRepositoryTest {

    private lateinit var fakeLocationApi: FakeLocationApiService
    private lateinit var repository: LocationRepository

    @Before
    fun setUp() {
        fakeLocationApi = FakeLocationApiService()
        repository = LocationRepository(fakeLocationApi)
    }

    @Test
    fun testAutocompleteSuccess() = runBlocking {
        val result = repository.autocomplete("Nguyen Trai", limit = 6)
        assertTrue(result is NetworkResult.Success)
        val list = (result as NetworkResult.Success).data
        assertEquals(1, list.size)
        assertEquals("314 Nguyen Trai", list[0].primaryText)
        assertEquals(10.7568, list[0].latitude, 0.001)
    }

    @Test
    fun testEstimateRouteSuccess() = runBlocking {
        val result = repository.estimateRoute(
            pickupLat = 10.7700,
            pickupLng = 106.6800,
            deliveryLat = 10.8000,
            deliveryLng = 106.7100,
            totalWeightKg = 2.5,
            fragile = true,
            express = false
        )

        assertTrue(result is NetworkResult.Success)
        val estimate = (result as NetworkResult.Success).data
        assertEquals(5.41, estimate.distanceKm, 0.01)
        assertEquals(8, estimate.estimatedDurationMinutes)
        assertEquals(54550.0, estimate.totalFee, 0.01)
    }

    // ── Fake Implementation ──────────────────────────────────────────

    class FakeLocationApiService : LocationApiService {
        override suspend fun autocomplete(query: String, limit: Int): Response<List<AddressSuggestionResponseDto>> {
            val suggestion = AddressSuggestionResponseDto(
                placeId = "W:189067626",
                formattedAddress = "314 Nguyen Trai, Quan 5, TPHCM",
                primaryText = "314 Nguyen Trai",
                secondaryText = "Quan 5, TPHCM",
                ward = "Phuong 8",
                district = "Quan 5",
                province = "TPHCM",
                country = "Việt Nam",
                latitude = 10.7568,
                longitude = 106.6750
            )
            return Response.success(listOf(suggestion))
        }

        override suspend fun estimateRoute(request: RouteEstimateRequestDto): Response<RouteEstimateResponseDto> {
            val response = RouteEstimateResponseDto(
                distanceKm = 5.41,
                estimatedDurationMinutes = 8,
                baseFee = 15000.0,
                distanceFee = 27050.0,
                weightFee = 7500.0,
                serviceFee = 5000.0,
                totalFee = 54550.0
            )
            return Response.success(response)
        }
    }
}
