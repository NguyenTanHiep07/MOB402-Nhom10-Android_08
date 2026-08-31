package com.mob10.deliveryapp.ui.driver

import com.mob10.deliveryapp.data.model.*
import com.mob10.deliveryapp.data.repository.ShipperRepository
import com.mob10.deliveryapp.data.util.NetworkResult
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DriverViewModelTest {

    private val repository: ShipperRepository = mockk(relaxed = true)
    private lateinit var viewModel: DriverViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val sampleOrder1 = Order(
        id = 101L, client = null, deliveryPerson = null,
        pickupAddress = "123 Lê Lợi, Q1", deliveryAddress = "456 Nguyễn Huệ, Q1",
        pickupLatitude = 10.77, pickupLongitude = 106.70,
        deliveryLatitude = 10.78, deliveryLongitude = 106.69,
        senderName = "Shop A", senderPhone = "0901",
        recipientName = "Khách B", recipientPhone = "0902",
        distanceKm = 3.5, baseFee = 15000.0, distanceFee = 15000.0,
        weightFee = 3000.0, fragileCharge = 0.0, totalCost = 33000.0,
        status = DeliveryStatus.CHO_TIEP_NHAN, scheduledPickupTime = null,
        actualDeliveryTime = null, note = null, createdAt = "2026-08-31T08:00:00Z",
        updatedAt = null, packages = listOf(OrderPackage(1L, "Cơm sườn", "FOOD", 0.5, 1, null, false, false))
    )

    private val sampleActiveOrder = sampleOrder1.copy(
        id = 102L,
        status = DeliveryStatus.DA_CHAP_NHAN
    )

    private val sampleStatistics = DriverStatistics(
        driverId = 1L, totalAccepted = 10, totalRejected = 1,
        penalizedRejections = 0, reliabilityScore = 95.0,
        lockedUntil = null, isLocked = false, isWarning = false
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        coEvery { repository.getOpenOrders() } returns NetworkResult.Success(listOf(sampleOrder1))
        coEvery { repository.getMyOrders() } returns NetworkResult.Success(listOf(sampleActiveOrder))
        coEvery { repository.getMyStatistics() } returns NetworkResult.Success(sampleStatistics)
        coEvery { repository.getRejectionReasons() } returns NetworkResult.Success(emptyList())

        viewModel = DriverViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadDriverData fetches open orders, my orders, and statistics`() = runTest {
        viewModel.loadDriverData(1)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.newOrders.size)
        assertEquals(101L, state.newOrders.first().id)
        assertEquals(1, state.activeOrders.size)
        assertEquals(102L, state.activeOrders.first().id)
        assertEquals(95.0, state.reliabilityScore, 0.01)
    }

    @Test
    fun `acceptOrder success updates UI state with message`() = runTest {
        coEvery { repository.acceptOrder(101L) } returns NetworkResult.Success(sampleActiveOrder)

        viewModel.acceptOrder(101)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isConflictError)
        assertNotNull(state.acceptMessage)
        assertTrue(state.acceptMessage!!.contains("Nhận đơn #101 thành công"))
    }

    @Test
    fun `acceptOrder 409 conflict sets isConflictError true`() = runTest {
        coEvery { repository.acceptOrder(101L) } returns NetworkResult.Error(
            code = "ORDER_ALREADY_TAKEN",
            message = "Đơn hàng #101 đã được tài xế khác nhận trước.",
            httpCode = 409
        )

        viewModel.acceptOrder(101)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isConflictError)
        assertNotNull(state.userMessage)
        assertTrue(state.userMessage!!.lowercase().contains("tài xế khác"))
    }

    @Test
    fun `rejectOrder success updates statistics and displays message`() = runTest {
        val updatedStats = sampleStatistics.copy(totalRejected = 2, reliabilityScore = 90.0)
        val rejectInfo = RejectInfo(
            message = "Đã từ chối đơn #101",
            penaltyApplied = true,
            statistics = updatedStats
        )
        coEvery { repository.rejectOrder(101L, "VEHICLE_ISSUE", any()) } returns NetworkResult.Success(rejectInfo)

        viewModel.rejectOrder(101, "VEHICLE_ISSUE", "Thủng lốp")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(90.0, state.reliabilityScore, 0.01)
        assertNotNull(state.userMessage)
        assertTrue(state.userMessage!!.contains("từ chối đơn #101"))
    }

    @Test
    fun `updateOrderStatus success calls repository and refreshes my orders`() = runTest {
        coEvery { repository.updateOrderStatus(102L, DeliveryStatus.DA_DEN_NHA_HANG, any()) } returns NetworkResult.Success(sampleActiveOrder.copy(status = DeliveryStatus.DA_DEN_NHA_HANG))

        viewModel.updateOrderStatus(102, DeliveryStatus.DA_DEN_NHA_HANG)
        advanceUntilIdle()

        coVerify { repository.updateOrderStatus(102L, DeliveryStatus.DA_DEN_NHA_HANG, any()) }
        assertNotNull(viewModel.uiState.value.userMessage)
    }

    @Test
    fun `setWorkingStatus calls updateAvailability and updates uiState`() = runTest {
        coEvery { repository.updateAvailability("BUSY") } returns NetworkResult.Success("BUSY")

        viewModel.setWorkingStatus(DriverWorkingStatus.BUSY)
        advanceUntilIdle()

        assertEquals(DriverWorkingStatus.BUSY, viewModel.uiState.value.driverStatus)
        assertEquals("Đang bận giao", viewModel.uiState.value.driverStatus.label)
    }

    @Test
    fun `clearAcceptMessage resets userMessage and isConflictError`() = runTest {
        coEvery { repository.acceptOrder(101L) } returns NetworkResult.Error(
            code = "ORDER_ALREADY_TAKEN",
            message = "Conflict",
            httpCode = 409
        )
        viewModel.acceptOrder(101)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isConflictError)

        viewModel.clearAcceptMessage()
        assertNull(viewModel.uiState.value.acceptMessage)
        assertNull(viewModel.uiState.value.userMessage)
        assertFalse(viewModel.uiState.value.isConflictError)
    }
}
