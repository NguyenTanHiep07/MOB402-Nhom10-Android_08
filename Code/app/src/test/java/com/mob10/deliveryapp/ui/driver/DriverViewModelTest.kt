package com.mob10.deliveryapp.ui.driver

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.mob10.deliveryapp.data.local.AppDatabase
import com.mob10.deliveryapp.data.local.entity.FeeRuleEntity
import com.mob10.deliveryapp.data.local.entity.UserEntity
import com.mob10.deliveryapp.data.model.DeliveryStatus
import com.mob10.deliveryapp.data.model.Role
import com.mob10.deliveryapp.data.repository.AcceptResult
import com.mob10.deliveryapp.data.repository.DeliveryRepository
import com.mob10.deliveryapp.data.repository.NewPackageInfo
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class DriverViewModelTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: DeliveryRepository
    private lateinit var viewModel: DriverViewModel
    private var testClientId: Int = 0
    private var testDriverId: Int = 0

    @Before
    fun setUp() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                .allowMainThreadQueries()
                .build()

            repository = DeliveryRepository(
                db = db,
                requestDao = db.deliveryRequestDao(),
                packageDao = db.packageDao(),
                historyDao = db.statusHistoryDao(),
                feeRuleDao = db.feeRuleDao()
            )

            testClientId = db.userDao().insert(
                UserEntity(
                    username = "client_test",
                    password = "123",
                    fullName = "Khách Hàng Test",
                    phoneNumber = "0901234567",
                    role = Role.CLIENT
                )
            ).toInt()

            testDriverId = db.userDao().insert(
                UserEntity(
                    username = "driver_test",
                    password = "123",
                    fullName = "Tài Xế Test",
                    phoneNumber = "0907654321",
                    role = Role.DELIVERY,
                    licensePlate = "59-X1 12345"
                )
            ).toInt()

            db.feeRuleDao().insert(
                FeeRuleEntity(
                    ruleName = "Bảng giá test",
                    baseFee = 15000.0,
                    pricePerKm = 5000.0,
                    pricePerKg = 3000.0,
                    fragileFee = 5000.0,
                    isActive = true
                )
            )

            viewModel = DriverViewModel(repository)
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testRejectOrder_updatesRejectionCountAndReliabilityScore() {
        // Initial state
        assertEquals(100, viewModel.uiState.value.reliabilityScore)
        assertEquals(0, viewModel.uiState.value.rejectedCount)

        // Reject an order with reason and note
        viewModel.rejectOrder(
            orderId = 101,
            reason = "Khoảng cách lấy hoặc giao hàng quá xa",
            note = "Cách 25km"
        )

        val state = viewModel.uiState.value
        assertEquals(1, state.rejectedCount)
        assertTrue(state.rejectedOrderIds.contains(101))
        assertEquals(95, state.reliabilityScore)
        assertNotNull(state.userMessage)
        assertTrue(state.userMessage!!.contains("101"))
    }

    @Test
    fun testMultipleRejections_lowersReliabilityScore() {
        viewModel.rejectOrder(orderId = 1, reason = "Hàng quá cồng kềnh")
        viewModel.rejectOrder(orderId = 2, reason = "Thời tiết xấu")
        viewModel.rejectOrder(orderId = 3, reason = "Xe hỏng")

        val state = viewModel.uiState.value
        assertEquals(3, state.rejectedCount)
        assertEquals(85, state.reliabilityScore) // 100 - (3 * 5) = 85
    }

    @Test
    fun testSetWorkingStatus() {
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
        viewModel.rejectOrder(orderId = 5, reason = "Lý do khác")
        assertNotNull(viewModel.uiState.value.userMessage)

        viewModel.clearAcceptMessage()
        assertNull(viewModel.uiState.value.userMessage)
        assertNull(viewModel.uiState.value.acceptMessage)
    }

    @Test
    fun testAcceptRequestViaRepository_atomicSuccess() = runBlocking {
        // Create an order
        val orderId = repository.createRequest(
            clientId = testClientId,
            pickupAddress = "123 Lê Lợi, Q1",
            deliveryAddress = "456 Nguyễn Huệ, Q1",
            senderName = "Cửa hàng A",
            senderPhone = "0901111111",
            recipientName = "Khách B",
            recipientPhone = "0902222222",
            distanceKm = 3.5,
            packages = listOf(
                NewPackageInfo(name = "Cơm sườn", weightKg = 1.0, quantity = 2)
            )
        ).toInt()

        val acceptResult = repository.acceptRequest(orderId, testDriverId)
        assertEquals(AcceptResult.Success, acceptResult)

        val requestAfterAccept = repository.getRequestById(orderId)
        assertNotNull(requestAfterAccept)
        assertEquals(DeliveryStatus.DA_CHAP_NHAN, requestAfterAccept!!.status)
        assertEquals(testDriverId, requestAfterAccept.deliveryPersonId)

        // Update status to DA_DEN_NHA_HANG
        val update1 = repository.updateRequestStatus(orderId, DeliveryStatus.DA_DEN_NHA_HANG, updatedBy = testDriverId)
        assertTrue(update1)
        assertEquals(DeliveryStatus.DA_DEN_NHA_HANG, repository.getRequestById(orderId)!!.status)

        // Update status to DA_LAY_HANG
        val update2 = repository.updateRequestStatus(orderId, DeliveryStatus.DA_LAY_HANG, updatedBy = testDriverId)
        assertTrue(update2)
        assertEquals(DeliveryStatus.DA_LAY_HANG, repository.getRequestById(orderId)!!.status)

        // Update status to DA_DEN_KHACH_HANG
        val update3 = repository.updateRequestStatus(orderId, DeliveryStatus.DA_DEN_KHACH_HANG, updatedBy = testDriverId)
        assertTrue(update3)
        assertEquals(DeliveryStatus.DA_DEN_KHACH_HANG, repository.getRequestById(orderId)!!.status)

        // Update status to DA_GIAO
        val update4 = repository.updateRequestStatus(orderId, DeliveryStatus.DA_GIAO, updatedBy = testDriverId)
        assertTrue(update4)
        assertEquals(DeliveryStatus.DA_GIAO, repository.getRequestById(orderId)!!.status)
    }
}
