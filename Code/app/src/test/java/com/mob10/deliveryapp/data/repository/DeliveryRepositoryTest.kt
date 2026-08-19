package com.mob10.deliveryapp.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.mob10.deliveryapp.data.local.AppDatabase
import com.mob10.deliveryapp.data.local.entity.FeeRuleEntity
import com.mob10.deliveryapp.data.local.entity.UserEntity
import com.mob10.deliveryapp.data.model.DeliveryStatus
import com.mob10.deliveryapp.data.model.Role
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class DeliveryRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: DeliveryRepository
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

            // Seed users
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
                    phoneNumber = "0912345678",
                    role = Role.DELIVERY
                )
            ).toInt()

            // Seed FeeRule
            db.feeRuleDao().insert(
                FeeRuleEntity(
                    ruleName = "Bảng giá tiêu chuẩn 2026",
                    baseFee = 15_000.0,
                    pricePerKm = 5_000.0,
                    pricePerKg = 3_000.0,
                    fragileFee = 5_000.0,
                    isActive = true
                )
            )
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testCreateRequest_AtomicTransaction_Success() {
        runBlocking {
            val packages = listOf(
                NewPackageInfo(
                    name = "Laptop Dell",
                    packageType = "Điện tử",
                    weightKg = 2.0,
                    quantity = 1,
                    notes = "Hàng đắt tiền",
                    isFragile = true
                ),
                NewPackageInfo(
                    name = "Sách",
                    packageType = "Tài liệu",
                    weightKg = 1.0,
                    quantity = 2,
                    isFragile = false
                )
            )

            val requestIdLong = repository.createRequest(
                clientId = testClientId,
                pickupAddress = "12 Nguyễn Trãi, Q5",
                deliveryAddress = "85 Lê Lợi, Q1",
                senderName = "Nguyễn Văn Gửi",
                senderPhone = "0901111222",
                recipientName = "Trần Thị Nhận",
                recipientPhone = "0903333444",
                distanceKm = 5.0,
                packages = packages,
                note = "Giao giờ hành chính"
            )

            val requestId = requestIdLong.toInt()
            assertTrue(requestId > 0)

            // 1. Verify DeliveryRequestEntity
            val request = repository.getRequestById(requestId)
            assertNotNull(request)
            assertEquals(testClientId, request!!.clientId)
            assertEquals("12 Nguyễn Trãi, Q5", request.pickupAddress)
            assertEquals("85 Lê Lợi, Q1", request.deliveryAddress)
            assertEquals("Nguyễn Văn Gửi", request.senderName)
            assertEquals("Trần Thị Nhận", request.recipientName)
            assertEquals(DeliveryStatus.CHO_TIEP_NHAN, request.status)

            // Phí: Base(15k) + Distance(5km * 5k = 25k) + Weight(3kg * 3k = 9k) + Fragile(5k) = 54k
            assertEquals(15_000.0, request.baseFee, 0.01)
            assertEquals(25_000.0, request.distanceFee, 0.01)
            assertEquals(9_000.0, request.weightFee, 0.01)
            assertEquals(5_000.0, request.fragileCharge, 0.01)
            assertEquals(54_000.0, request.totalCost, 0.01)

            // 2. Verify Packages
            val savedPackages = repository.getRequestPackages(requestId)
            assertEquals(2, savedPackages.size)
            assertEquals("Laptop Dell", savedPackages[0].name)
            assertTrue(savedPackages[0].isFragile)
            assertEquals("Sách", savedPackages[1].name)

            // 3. Verify StatusHistoryEntity (First status record)
            val historyList = repository.getRequestHistory(requestId)
            assertEquals(1, historyList.size)
            val firstHistory = historyList[0]
            assertNull(firstHistory.fromStatus)
            assertEquals(DeliveryStatus.CHO_TIEP_NHAN, firstHistory.toStatus)
            assertEquals(testClientId, firstHistory.updatedBy)
            assertEquals("Đơn hàng được tạo", firstHistory.note)
        }
    }

    @Test
    fun testAcceptRequest_DriverAssignment_And_StatusHistory() {
        runBlocking {
            val requestId = repository.createRequest(
                clientId = testClientId,
                pickupAddress = "A",
                deliveryAddress = "B",
                senderName = "Sender",
                senderPhone = "0901234567",
                recipientName = "Recipient",
                recipientPhone = "0909876543",
                distanceKm = 2.0,
                packages = listOf(NewPackageInfo(name = "Item", weightKg = 1.0))
            ).toInt()

            // Driver accepts the order
            val acceptResult = repository.acceptRequest(requestId, testDriverId)
            assertTrue(acceptResult)

            // Verify updated request
            val request = repository.getRequestById(requestId)
            assertNotNull(request)
            assertEquals(DeliveryStatus.DA_CHAP_NHAN, request!!.status)
            assertEquals(testDriverId, request.deliveryPersonId)

            // Verify status history has 2 entries
            val historyList = repository.getRequestHistory(requestId)
            assertEquals(2, historyList.size)

            val secondHistory = historyList[1]
            assertEquals(DeliveryStatus.CHO_TIEP_NHAN, secondHistory.fromStatus)
            assertEquals(DeliveryStatus.DA_CHAP_NHAN, secondHistory.toStatus)
            assertEquals(testDriverId, secondHistory.updatedBy)
            assertEquals("Tài xế đã nhận đơn", secondHistory.note)
        }
    }

    @Test
    fun testInvalidStatusTransition_ReturnsFalse() {
        runBlocking {
            val requestId = repository.createRequest(
                clientId = testClientId,
                pickupAddress = "A",
                deliveryAddress = "B",
                senderName = "Sender",
                senderPhone = "0901234567",
                recipientName = "Recipient",
                recipientPhone = "0909876543",
                distanceKm = 2.0,
                packages = listOf(NewPackageInfo(name = "Item", weightKg = 1.0))
            ).toInt()

            // Direct transition CHO_TIEP_NHAN -> DA_GIAO is INVALID
            val result = repository.updateRequestStatus(requestId, DeliveryStatus.DA_GIAO)
            assertFalse(result)

            val request = repository.getRequestById(requestId)
            assertEquals(DeliveryStatus.CHO_TIEP_NHAN, request!!.status)
        }
    }

    @Test
    fun testFeeRuleDao_GetActiveRule() {
        runBlocking {
            val activeRule = repository.getActiveFeeRuleSync()
            assertNotNull(activeRule)
            assertEquals(15_000.0, activeRule!!.baseFee, 0.01)
            assertEquals(5_000.0, activeRule.pricePerKm, 0.01)
            assertEquals(3_000.0, activeRule.pricePerKg, 0.01)
        }
    }
}
