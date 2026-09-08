package com.mob10.deliveryapp.data.repository

import com.mob10.deliveryapp.data.remote.api.OrderApiService
import com.mob10.deliveryapp.data.remote.dto.*
import com.mob10.deliveryapp.data.util.NetworkResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class ClientOrderRepositoryTest {

    private lateinit var fakeOrderApi: FakeOrderApiService
    private lateinit var repository: ClientOrderRepository

    @Before
    fun setUp() {
        fakeOrderApi = FakeOrderApiService()
        repository = ClientOrderRepository(fakeOrderApi)
    }

    @Test
    fun testCreateOrderSuccess() = runBlocking {
        val request = CreateOrderRequestDto(
            pickupAddress = "123 Le Loi",
            deliveryAddress = "456 Nguyen Hue",
            pickupLatitude = 10.7769,
            pickupLongitude = 106.7009,
            deliveryLatitude = 10.7745,
            deliveryLongitude = 106.7032,
            senderName = "Nguyen Van A",
            senderPhone = "0901234567",
            recipientName = "Tran Van B",
            recipientPhone = "0987654321",
            distanceKm = 2.5,
            packages = listOf(
                PackageInputDto(name = "Com ga", packageType = "FOOD", weightKg = 0.5)
            )
        )

        val result = repository.createOrder(request)
        assertTrue(result is NetworkResult.Success)
        val order = (result as NetworkResult.Success).data
        assertEquals(1001L, order.id)
        assertEquals("Nguyen Van A", order.senderName)
        assertEquals(1, order.packages.size)
    }

    @Test
    fun testGetMyOrdersSuccess() = runBlocking {
        val result = repository.getMyOrders()
        assertTrue(result is NetworkResult.Success)
        val list = (result as NetworkResult.Success).data
        assertEquals(1, list.size)
        assertEquals(1001L, list[0].id)
    }

    @Test
    fun testCancelOrderSuccess() = runBlocking {
        val result = repository.cancelOrder(1001L)
        assertTrue(result is NetworkResult.Success)
        val order = (result as NetworkResult.Success).data
        assertEquals("DA_HUY", order.status.name)
    }

    @Test
    fun testCancelOrderConflictError() = runBlocking {
        fakeOrderApi.shouldReturnConflictOnCancel = true
        val result = repository.cancelOrder(1001L)
        assertTrue(result is NetworkResult.Error)
        val error = result as NetworkResult.Error
        assertEquals("INVALID_STATUS_TRANSITION", error.code)
        assertEquals("Đơn hàng đã được xử lý, không thể hủy vào lúc này.", error.message)
    }

    @Test
    fun testNetworkErrorHandled() = runBlocking {
        fakeOrderApi.shouldThrowNetworkError = true
        val result = repository.getMyOrders()
        assertTrue(result is NetworkResult.Error)
        val error = result as NetworkResult.Error
        assertTrue(error.isNetworkError)
    }

    // ── Fake Implementation ──────────────────────────────────────────

    class FakeOrderApiService : OrderApiService {
        var shouldReturnConflictOnCancel = false
        var shouldThrowNetworkError = false

        private val sampleDto = OrderResponseDto(
            id = 1001L,
            client = PersonResponseDto(1L, "Nguyen Van A", "0901234567", null),
            deliveryPerson = null,
            pickupAddress = "123 Le Loi",
            deliveryAddress = "456 Nguyen Hue",
            pickupLatitude = 10.7769,
            pickupLongitude = 106.7009,
            deliveryLatitude = 10.7745,
            deliveryLongitude = 106.7032,
            senderName = "Nguyen Van A",
            senderPhone = "0901234567",
            recipientName = "Tran Van B",
            recipientPhone = "0987654321",
            distanceKm = 2.5,
            baseFee = 15000.0,
            distanceFee = 12500.0,
            weightFee = 1500.0,
            fragileCharge = 0.0,
            totalCost = 29000.0,
            status = "CHO_TIEP_NHAN",
            scheduledPickupTime = null,
            actualDeliveryTime = null,
            note = null,
            createdAt = "2026-09-03T10:00:00Z",
            updatedAt = null,
            packages = listOf(
                PackageResponseDto(1L, "Com ga", "FOOD", 0.5, 1, null, fragile = false, express = false)
            )
        )

        override suspend fun createOrder(request: CreateOrderRequestDto): Response<OrderResponseDto> {
            if (shouldThrowNetworkError) throw IOException("No network")
            return Response.success(sampleDto)
        }

        override suspend fun getOrders(): Response<List<OrderResponseDto>> {
            if (shouldThrowNetworkError) throw IOException("No network")
            return Response.success(listOf(sampleDto))
        }

        override suspend fun getOrderById(orderId: Long): Response<OrderResponseDto> {
            if (shouldThrowNetworkError) throw IOException("No network")
            return Response.success(sampleDto.copy(id = orderId))
        }

        override suspend fun getOrderHistory(orderId: Long): Response<List<HistoryResponseDto>> {
            if (shouldThrowNetworkError) throw IOException("No network")
            return Response.success(emptyList())
        }

        override suspend fun cancelOrder(orderId: Long): Response<OrderResponseDto> {
            if (shouldThrowNetworkError) throw IOException("No network")
            if (shouldReturnConflictOnCancel) {
                val errorJson = """{"status":409,"code":"INVALID_STATUS_TRANSITION","message":"Conflict"}"""
                return Response.error(409, okhttp3.ResponseBody.create(null, errorJson))
            }
            return Response.success(sampleDto.copy(status = "DA_HUY"))
        }
    }
}
