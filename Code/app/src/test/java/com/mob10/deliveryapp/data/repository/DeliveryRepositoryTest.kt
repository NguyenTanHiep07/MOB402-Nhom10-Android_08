package com.mob10.deliveryapp.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.mob10.deliveryapp.data.local.AppDatabase
import com.mob10.deliveryapp.data.local.entity.FeeRuleEntity
import com.mob10.deliveryapp.data.local.entity.UserEntity
import com.mob10.deliveryapp.data.model.DeliveryStatus
import com.mob10.deliveryapp.data.model.Role
import kotlinx.coroutines.flow.first
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
    private var testDriver2Id: Int = 0

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

            testDriver2Id = db.userDao().insert(
                UserEntity(
                    username = "driver_test_2",
                    password = "123",
                    fullName = "Tài Xế Test 2",
                    phoneNumber = "0922345678",
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

    // ─── Helper ──────────────────────────────────────────────

    private suspend fun createTestRequest(): Int {
        return repository.createRequest(
            clientId = testClientId,
            pickupAddress = "12 Nguyễn Trãi, Q5",
            deliveryAddress = "85 Lê Lợi, Q1",
            senderName = "Nguyễn Văn Gửi",
            senderPhone = "0901111222",
            recipientName = "Trần Thị Nhận",
            recipientPhone = "0903333444",
            distanceKm = 5.0,
            packages = listOf(
                NewPackageInfo(name = "Laptop Dell", packageType = "Điện tử", weightKg = 2.0, quantity = 1, isFragile = true),
                NewPackageInfo(name = "Sách", packageType = "Tài liệu", weightKg = 1.0, quantity = 2)
            ),
            note = "Giao giờ hành chính"
        ).toInt()
    }

    // ─── Existing Tests (updated for AcceptResult) ───────────

    @Test
    fun testCreateRequest_AtomicTransaction_Success() {
        runBlocking {
            val requestId = createTestRequest()
            assertTrue(requestId > 0)

            // 1. Verify DeliveryRequestEntity
            val request = repository.getRequestById(requestId)
            assertNotNull(request)
            assertEquals(testClientId, request!!.clientId)
            assertNull("Đơn mới phải có deliveryPersonId = null", request.deliveryPersonId)
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
            val requestId = createTestRequest()

            // Driver accepts the order
            val acceptResult = repository.acceptRequest(requestId, testDriverId)
            assertTrue("Accept phải trả về Success", acceptResult is AcceptResult.Success)

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
            val requestId = createTestRequest()

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

    // ─── NEW: Open Pool Tests ────────────────────────────────

    /**
     * TC-POOL-01: Hai tài xế không thể Accept cùng một đơn.
     * Driver 1 accept thành công → Driver 2 accept cùng đơn → AlreadyTaken.
     */
    @Test
    fun testTwoDrivers_CannotAcceptSameOrder() {
        runBlocking {
            val requestId = createTestRequest()

            // Driver 1 accepts → Success
            val result1 = repository.acceptRequest(requestId, testDriverId)
            assertTrue("Driver 1 phải accept thành công", result1 is AcceptResult.Success)

            // Driver 2 tries to accept same order → phải thất bại
            // Có thể là InvalidStatus (status đã đổi) hoặc AlreadyTaken (race condition)
            val result2 = repository.acceptRequest(requestId, testDriver2Id)
            assertTrue(
                "Driver 2 phải thất bại (AlreadyTaken hoặc InvalidStatus), nhận được: $result2",
                result2 is AcceptResult.AlreadyTaken || result2 is AcceptResult.InvalidStatus
            )

            // Verify order belongs to Driver 1
            val request = repository.getRequestById(requestId)
            assertEquals(testDriverId, request!!.deliveryPersonId)
            assertEquals(DeliveryStatus.DA_CHAP_NHAN, request.status)
        }
    }

    /**
     * TC-POOL-02: Open Pool query chỉ trả về đơn có deliveryPersonId == null
     * && status == CHO_TIEP_NHAN.
     */
    @Test
    fun testOpenPoolQuery_OnlyUnassignedPendingOrders() {
        runBlocking {
            // Tạo 2 đơn mới (cả 2 đều CHO_TIEP_NHAN, deliveryPersonId = null)
            val req1Id = createTestRequest()
            val req2Id = createTestRequest()

            // Pending requests phải có 2 đơn
            var pendingOrders = db.deliveryRequestDao().getPendingRequests().first()
            assertEquals("Phải có 2 đơn trong Open Pool", 2, pendingOrders.size)

            // Driver 1 accept đơn 1
            repository.acceptRequest(req1Id, testDriverId)

            // Giờ Open Pool chỉ còn 1 đơn (đơn 2)
            pendingOrders = db.deliveryRequestDao().getPendingRequests().first()
            assertEquals("Sau accept, Open Pool chỉ còn 1 đơn", 1, pendingOrders.size)
            assertEquals(req2Id, pendingOrders[0].id)
            assertNull(pendingOrders[0].deliveryPersonId)

            // PendingCount cũng phải khớp
            val count = db.deliveryRequestDao().getPendingCount().first()
            assertEquals(1, count)
        }
    }

    /**
     * TC-POOL-03: Chỉ driver sở hữu đơn mới được update trạng thái.
     * Driver 2 cố update đơn của Driver 1 → thất bại.
     */
    @Test
    fun testOwnerOnly_CanUpdateStatus() {
        runBlocking {
            val requestId = createTestRequest()
            repository.acceptRequest(requestId, testDriverId) // Driver 1 owns order

            // Driver 2 cố update đơn của Driver 1 → PHẢI thất bại
            val resultByOther = repository.updateRequestStatus(
                requestId = requestId,
                newStatus = DeliveryStatus.DA_DEN_NHA_HANG,
                updatedBy = testDriver2Id,
                note = "Driver 2 cố update"
            )
            assertFalse("Driver khác không được phép update", resultByOther)

            // Verify status không thay đổi
            val request = repository.getRequestById(requestId)
            assertEquals(DeliveryStatus.DA_CHAP_NHAN, request!!.status)

            // Driver 1 (owner) update → thành công
            val resultByOwner = repository.updateRequestStatus(
                requestId = requestId,
                newStatus = DeliveryStatus.DA_DEN_NHA_HANG,
                updatedBy = testDriverId,
                note = "Driver 1 update"
            )
            assertTrue("Owner phải update thành công", resultByOwner)

            val updatedRequest = repository.getRequestById(requestId)
            assertEquals(DeliveryStatus.DA_DEN_NHA_HANG, updatedRequest!!.status)
        }
    }

    /**
     * TC-POOL-04: Full status flow với StatusHistory đầy đủ updatedBy + timestamp.
     * CHO_TIEP_NHAN → DA_CHAP_NHAN → DA_DEN_NHA_HANG → DA_LAY_HANG → DA_DEN_KHACH_HANG → DA_GIAO
     */
    @Test
    fun testFullStatusFlow_WithHistory() {
        runBlocking {
            val requestId = createTestRequest()

            // 1. Accept
            val acceptResult = repository.acceptRequest(requestId, testDriverId)
            assertTrue(acceptResult is AcceptResult.Success)

            // 2. DA_DEN_NHA_HANG
            assertTrue(repository.updateRequestStatus(requestId, DeliveryStatus.DA_DEN_NHA_HANG, updatedBy = testDriverId))

            // 3. DA_LAY_HANG
            assertTrue(repository.updateRequestStatus(requestId, DeliveryStatus.DA_LAY_HANG, updatedBy = testDriverId))

            // 4. DA_DEN_KHACH_HANG
            assertTrue(repository.updateRequestStatus(requestId, DeliveryStatus.DA_DEN_KHACH_HANG, updatedBy = testDriverId))

            // 5. DA_GIAO
            assertTrue(repository.updateRequestStatus(requestId, DeliveryStatus.DA_GIAO, updatedBy = testDriverId))

            // Verify final state
            val request = repository.getRequestById(requestId)
            assertEquals(DeliveryStatus.DA_GIAO, request!!.status)
            assertNotNull("DA_GIAO phải có actualDeliveryTime", request.actualDeliveryTime)

            // Verify full history: 6 entries (create + accept + 4 updates)
            val history = repository.getRequestHistory(requestId)
            assertEquals(6, history.size)

            // Verify mỗi entry history đều có updatedBy và timestamp
            history.forEach { entry ->
                assertNotNull("History phải có updatedBy", entry.updatedBy)
                assertTrue("Timestamp phải > 0", entry.timestamp > 0)
            }

            // Verify order of status transitions
            val statusSequence = history.map { it.toStatus }
            assertEquals(
                listOf(
                    DeliveryStatus.CHO_TIEP_NHAN,
                    DeliveryStatus.DA_CHAP_NHAN,
                    DeliveryStatus.DA_DEN_NHA_HANG,
                    DeliveryStatus.DA_LAY_HANG,
                    DeliveryStatus.DA_DEN_KHACH_HANG,
                    DeliveryStatus.DA_GIAO
                ),
                statusSequence
            )
        }
    }

    /**
     * TC-POOL-05: Accept đơn không tồn tại → NotFound.
     * Accept đơn đã giao (terminal state) → InvalidStatus.
     */
    @Test
    fun testAcceptRequest_EdgeCases() {
        runBlocking {
            // Accept đơn không tồn tại
            val notFoundResult = repository.acceptRequest(99999, testDriverId)
            assertTrue("Phải trả NotFound", notFoundResult is AcceptResult.NotFound)

            // Accept đơn đã được accept rồi (status != CHO_TIEP_NHAN)
            val requestId = createTestRequest()
            repository.acceptRequest(requestId, testDriverId) // Accept thành công

            // Chính driver đó accept lại → InvalidStatus
            val invalidResult = repository.acceptRequest(requestId, testDriverId)
            assertTrue(
                "Accept lại đơn đã nhận phải trả InvalidStatus hoặc AlreadyTaken, nhận: $invalidResult",
                invalidResult is AcceptResult.InvalidStatus || invalidResult is AcceptResult.AlreadyTaken
            )
        }
    }
}
