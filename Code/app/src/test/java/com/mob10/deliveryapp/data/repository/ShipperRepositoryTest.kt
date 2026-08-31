package com.mob10.deliveryapp.data.repository

import com.mob10.deliveryapp.data.model.DeliveryStatus
import com.mob10.deliveryapp.data.remote.api.DriverApiService
import com.mob10.deliveryapp.data.remote.api.OrderApiService
import com.mob10.deliveryapp.data.remote.dto.*
import com.mob10.deliveryapp.data.util.NetworkResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class ShipperRepositoryTest {

    private val driverApi: DriverApiService = mockk()
    private val orderApi: OrderApiService = mockk()
    private lateinit var repository: ShipperRepository

    @Before
    fun setUp() {
        repository = ShipperRepository(driverApi, orderApi)
    }

    @Test
    fun `getOpenOrders returns Success with domain orders when API succeeds`() = runBlocking {
        val sampleDto = OrderResponseDto(
            id = 1L, client = null, deliveryPerson = null,
            pickupAddress = "A", deliveryAddress = "B",
            pickupLatitude = 10.0, pickupLongitude = 106.0,
            deliveryLatitude = 10.1, deliveryLongitude = 106.1,
            senderName = "S", senderPhone = "01",
            recipientName = "R", recipientPhone = "02",
            distanceKm = 5.0, baseFee = 15000.0, distanceFee = 25000.0,
            weightFee = 3000.0, fragileCharge = 0.0, totalCost = 43000.0,
            status = "CHO_TIEP_NHAN", scheduledPickupTime = null,
            actualDeliveryTime = null, note = null, createdAt = null,
            updatedAt = null, packages = emptyList()
        )

        coEvery { driverApi.getOpenOrders() } returns Response.success(listOf(sampleDto))

        val result = repository.getOpenOrders()

        assertTrue(result is NetworkResult.Success)
        val orders = (result as NetworkResult.Success).data
        assertEquals(1, orders.size)
        assertEquals(1L, orders.first().id)
        assertEquals(DeliveryStatus.CHO_TIEP_NHAN, orders.first().status)
    }

    @Test
    fun `getOpenOrders returns Empty when API returns empty list`() = runBlocking {
        coEvery { driverApi.getOpenOrders() } returns Response.success(emptyList())

        val result = repository.getOpenOrders()

        assertTrue(result is NetworkResult.Empty)
    }

    @Test
    fun `acceptOrder returns Success when API accepts order`() = runBlocking {
        val acceptedDto = OrderResponseDto(
            id = 10L, client = null, deliveryPerson = null,
            pickupAddress = "A", deliveryAddress = "B",
            pickupLatitude = 10.0, pickupLongitude = 106.0,
            deliveryLatitude = 10.1, deliveryLongitude = 106.1,
            senderName = "S", senderPhone = "01",
            recipientName = "R", recipientPhone = "02",
            distanceKm = 2.0, baseFee = 15000.0, distanceFee = 10000.0,
            weightFee = 0.0, fragileCharge = 0.0, totalCost = 25000.0,
            status = "DA_CHAP_NHAN", scheduledPickupTime = null,
            actualDeliveryTime = null, note = null, createdAt = null,
            updatedAt = null, packages = emptyList()
        )

        coEvery { driverApi.acceptOrder(10L) } returns Response.success(acceptedDto)

        val result = repository.acceptOrder(10L)

        assertTrue(result is NetworkResult.Success)
        val order = (result as NetworkResult.Success).data
        assertEquals(10L, order.id)
        assertEquals(DeliveryStatus.DA_CHAP_NHAN, order.status)
    }

    @Test
    fun `acceptOrder handles 409 ORDER_ALREADY_TAKEN conflict gracefully`() = runBlocking {
        val errorJson = """
            {
                "timestamp": "2026-08-31T09:00:00Z",
                "status": 409,
                "code": "ORDER_ALREADY_TAKEN",
                "message": "Tài xế khác đã nhận trước",
                "path": "/api/driver/orders/10/accept",
                "fields": {}
            }
        """.trimIndent()
        val errorBody = errorJson.toResponseBody("application/json".toMediaType())

        coEvery { driverApi.acceptOrder(10L) } returns Response.error(409, errorBody)

        val result = repository.acceptOrder(10L)

        assertTrue(result is NetworkResult.Error)
        val error = result as NetworkResult.Error
        assertEquals(409, error.httpCode)
        assertEquals("ORDER_ALREADY_TAKEN", error.code)
        assertTrue(error.isConflict)
        assertTrue(error.message.lowercase().contains("tài xế khác"))
    }

    @Test
    fun `rejectOrder returns Success with updated statistics`() = runBlocking {
        val statsDto = DriverStatisticsResponseDto(
            driverId = 2L, totalAccepted = 10, totalRejected = 3,
            penalizedRejections = 2, reliabilityScore = 90.0,
            lockedUntil = null, locked = false, warning = false
        )
        val rejectResultDto = RejectResultDto(
            message = "Đã từ chối đơn #15",
            penaltyApplied = true,
            statistics = statsDto
        )

        coEvery { driverApi.rejectOrder(15L, any()) } returns Response.success(rejectResultDto)

        val result = repository.rejectOrder(15L, "VEHICLE_ISSUE", "Thủng lốp")

        assertTrue(result is NetworkResult.Success)
        val info = (result as NetworkResult.Success).data
        assertEquals("Đã từ chối đơn #15", info.message)
        assertTrue(info.penaltyApplied)
        assertNotNull(info.statistics)
        assertEquals(90.0, info.statistics!!.reliabilityScore, 0.01)
    }

    @Test
    fun `updateOrderStatus returns Success with new status`() = runBlocking {
        val updatedDto = OrderResponseDto(
            id = 20L, client = null, deliveryPerson = null,
            pickupAddress = "A", deliveryAddress = "B",
            pickupLatitude = 10.0, pickupLongitude = 106.0,
            deliveryLatitude = 10.1, deliveryLongitude = 106.1,
            senderName = "S", senderPhone = "01",
            recipientName = "R", recipientPhone = "02",
            distanceKm = 2.0, baseFee = 15000.0, distanceFee = 10000.0,
            weightFee = 0.0, fragileCharge = 0.0, totalCost = 25000.0,
            status = "DA_LAY_HANG", scheduledPickupTime = null,
            actualDeliveryTime = null, note = "Đã nhận đủ 2 món", createdAt = null,
            updatedAt = null, packages = emptyList()
        )

        coEvery { driverApi.updateOrderStatus(20L, any()) } returns Response.success(updatedDto)

        val result = repository.updateOrderStatus(20L, DeliveryStatus.DA_LAY_HANG, "Đã nhận đủ 2 món")

        assertTrue(result is NetworkResult.Success)
        val order = (result as NetworkResult.Success).data
        assertEquals(DeliveryStatus.DA_LAY_HANG, order.status)
    }

    @Test
    fun `getMyStatistics returns Success with reliability score`() = runBlocking {
        val statsDto = DriverStatisticsResponseDto(
            driverId = 1L, totalAccepted = 50, totalRejected = 1,
            penalizedRejections = 0, reliabilityScore = 98.0,
            lockedUntil = null, locked = false, warning = false
        )

        coEvery { driverApi.getMyStatistics() } returns Response.success(statsDto)

        val result = repository.getMyStatistics()

        assertTrue(result is NetworkResult.Success)
        val stats = (result as NetworkResult.Success).data
        assertEquals(98.0, stats.reliabilityScore, 0.01)
        assertFalse(stats.isLocked)
    }
}
