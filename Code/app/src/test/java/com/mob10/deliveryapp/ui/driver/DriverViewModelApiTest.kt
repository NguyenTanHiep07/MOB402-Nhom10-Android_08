package com.mob10.deliveryapp.ui.driver

import com.mob10.deliveryapp.data.model.DeliveryStatus
import com.mob10.deliveryapp.data.remote.api.DriverApiService
import com.mob10.deliveryapp.data.remote.api.OrderApiService
import com.mob10.deliveryapp.data.remote.dto.*
import com.mob10.deliveryapp.data.repository.ShipperRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Response

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class DriverViewModelApiTest {

    private lateinit var fakeDriverApi: FakeDriverApiService
    private lateinit var fakeOrderApi: FakeOrderApiService
    private lateinit var repository: ShipperRepository
    private lateinit var viewModel: DriverViewModel

    @Before
    fun setUp() {
        fakeDriverApi = FakeDriverApiService()
        fakeOrderApi = FakeOrderApiService()
        repository = ShipperRepository(fakeDriverApi, fakeOrderApi)
        viewModel = DriverViewModel(repository)
    }

    @Test
    fun testWorkingStatusTransitions() = runBlocking {
        assertEquals(DriverWorkingStatus.AVAILABLE, viewModel.uiState.value.driverStatus)

        viewModel.setWorkingStatus(DriverWorkingStatus.BUSY)
        assertEquals(DriverWorkingStatus.BUSY, viewModel.uiState.value.driverStatus)
        assertEquals("Đang bận giao", viewModel.uiState.value.driverStatus.label)

        viewModel.setWorkingStatus(DriverWorkingStatus.OFFLINE)
        assertEquals(DriverWorkingStatus.OFFLINE, viewModel.uiState.value.driverStatus)
        assertEquals("Ngoại tuyến", viewModel.uiState.value.driverStatus.label)
    }

    @Test
    fun testClearAcceptMessage() {
        viewModel.clearAcceptMessage()
        assertFalse(viewModel.uiState.value.isConflictError)
        assertNull(viewModel.uiState.value.acceptMessage)
        assertNull(viewModel.uiState.value.userMessage)
    }

    @Test
    fun testAcceptOrderFlow() = runBlocking {
        viewModel.acceptOrder(101)
        
        val state = viewModel.uiState.value
        assertEquals("Nhận đơn #101 thành công!", state.acceptMessage)
        assertEquals(DriverWorkingStatus.BUSY, state.driverStatus)
        assertFalse(state.isConflictError)
    }

    @Test
    fun testRejectOrderFlowWithPenalty() = runBlocking {
        viewModel.rejectOrder(102, reason = "TOO_FAR", note = "Khoảng cách xa")

        val state = viewModel.uiState.value
        assertNotNull(state.userMessage)
        assertTrue(state.userMessage!!.contains("102"))
        assertEquals(90.0, state.reliabilityScore, 0.01)
        assertEquals(1, state.rejectedCount)
    }

    @Test
    fun testUpdateOrderStatusFlow() = runBlocking {
        viewModel.updateOrderStatus(103, DeliveryStatus.DA_DEN_NHA_HANG)
        val state = viewModel.uiState.value
        assertNotNull(state.userMessage)
        assertTrue(state.userMessage!!.contains("103"))
    }

    // ── Fake Implementations ──────────────────────────────────────────

    class FakeDriverApiService : DriverApiService {
        override suspend fun getOpenOrders(): Response<List<OrderResponseDto>> {
            return Response.success(emptyList())
        }

        override suspend fun getMyOrders(): Response<List<OrderResponseDto>> {
            return Response.success(emptyList())
        }

        override suspend fun acceptOrder(orderId: Long): Response<OrderResponseDto> {
            val dummyOrder = OrderResponseDto(
                id = orderId,
                client = null,
                deliveryPerson = null,
                pickupAddress = "123 Lê Lợi, Q1",
                deliveryAddress = "456 Nguyễn Huệ, Q1",
                pickupLatitude = 10.7769,
                pickupLongitude = 106.7009,
                deliveryLatitude = 10.7745,
                deliveryLongitude = 106.7032,
                senderName = "Người gửi A",
                senderPhone = "0901234567",
                recipientName = "Người nhận B",
                recipientPhone = "0907654321",
                distanceKm = 3.5,
                baseFee = 15000.0,
                distanceFee = 15000.0,
                weightFee = 6000.0,
                fragileCharge = 0.0,
                totalCost = 36000.0,
                status = "DA_CHAP_NHAN",
                scheduledPickupTime = null,
                actualDeliveryTime = null,
                note = null,
                createdAt = "2026-09-01T12:00:00Z",
                updatedAt = "2026-09-01T12:00:00Z",
                packages = emptyList()
            )
            return Response.success(dummyOrder)
        }

        override suspend fun rejectOrder(orderId: Long, request: RejectOrderRequestDto): Response<RejectResultDto> {
            val stats = DriverStatisticsResponseDto(
                driverId = 1L,
                totalAccepted = 5,
                totalRejected = 1,
                penalizedRejections = 1,
                reliabilityScore = 90.0,
                lockedUntil = null,
                locked = false,
                warning = false
            )
            val result = RejectResultDto(
                message = "Đã từ chối đơn hàng",
                penaltyApplied = true,
                statistics = stats
            )
            return Response.success(result)
        }

        override suspend fun updateOrderStatus(orderId: Long, request: UpdateStatusRequestDto): Response<OrderResponseDto> {
            val dummyOrder = OrderResponseDto(
                id = orderId,
                client = null,
                deliveryPerson = null,
                pickupAddress = "123 Lê Lợi",
                deliveryAddress = "456 Nguyễn Huệ",
                pickupLatitude = null,
                pickupLongitude = null,
                deliveryLatitude = null,
                deliveryLongitude = null,
                senderName = "A",
                senderPhone = "0901",
                recipientName = "B",
                recipientPhone = "0902",
                distanceKm = 2.0,
                baseFee = 15000.0,
                distanceFee = 10000.0,
                weightFee = 0.0,
                fragileCharge = 0.0,
                totalCost = 25000.0,
                status = request.status,
                scheduledPickupTime = null,
                actualDeliveryTime = null,
                note = request.note,
                createdAt = "2026-09-01T12:00:00Z",
                updatedAt = "2026-09-01T12:00:00Z",
                packages = emptyList()
            )
            return Response.success(dummyOrder)
        }

        override suspend fun getRejectionReasons(): Response<List<RejectionReasonResponseDto>> {
            return Response.success(
                listOf(
                    RejectionReasonResponseDto("TOO_FAR", "Khoảng cách quá xa", false, 10, false)
                )
            )
        }

        override suspend fun getMyStatistics(): Response<DriverStatisticsResponseDto> {
            return Response.success(
                DriverStatisticsResponseDto(1L, 5, 0, 0, 100.0, null, false, false)
            )
        }

        override suspend fun updateAvailability(request: UpdateAvailabilityRequestDto): Response<String> {
            return Response.success(request.availability)
        }
    }

    class FakeOrderApiService : OrderApiService {
        override suspend fun getOrderById(orderId: Long): Response<OrderResponseDto> {
            throw NotImplementedError()
        }

        override suspend fun getOrderHistory(orderId: Long): Response<List<HistoryResponseDto>> {
            return Response.success(emptyList())
        }

        override suspend fun getOrders(): Response<List<OrderResponseDto>> {
            return Response.success(emptyList())
        }

        override suspend fun createOrder(request: CreateOrderRequestDto): Response<OrderResponseDto> {
            throw NotImplementedError()
        }

        override suspend fun cancelOrder(orderId: Long): Response<OrderResponseDto> {
            throw NotImplementedError()
        }
    }
}
