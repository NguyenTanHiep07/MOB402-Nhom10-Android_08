package com.mob10.deliveryapp

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mob10.deliveryapp.data.local.AppDatabase
import com.mob10.deliveryapp.data.local.entity.UserEntity
import com.mob10.deliveryapp.data.model.DeliveryStatus
import com.mob10.deliveryapp.data.model.Role
import com.mob10.deliveryapp.data.repository.CancelResult
import com.mob10.deliveryapp.data.repository.DeliveryRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeliveryRepositoryRaceConditionTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: DeliveryRepository
    private var clientId: Int = 0
    private var deliveryPersonId: Int = 0

    @Before
    fun setup() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = DeliveryRepository(
            db,
            db.deliveryRequestDao(),
            db.packageDao(),
            db.statusHistoryDao()
        )

        // Tạo sẵn 2 user thật trong DB để thỏa khóa ngoại (updatedBy, clientId, deliveryPersonId)
        val userDao = db.userDao()
        clientId = userDao.insert(
            UserEntity(
                username = "test_client",
                password = "123456",
                fullName = "Test Client",
                phoneNumber = "0900000001",
                role = Role.CLIENT
            )
        ).toInt()
        deliveryPersonId = userDao.insert(
            UserEntity(
                username = "test_shipper",
                password = "123456",
                fullName = "Test Shipper",
                phoneNumber = "0900000002",
                role = Role.DELIVERY
            )
        ).toInt()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun cancelAndAccept_chayDongThoi_chiMotThaoTacThangCuoc() = runBlocking {
        // 1. Tạo sẵn 1 đơn hàng ở trạng thái CHO_TIEP_NHAN
        val requestId = repository.createRequest(
            clientId = clientId,
            pickupAddress = "A",
            deliveryAddress = "B",
            senderName = "Sender",
            senderPhone = "0900000000",
            recipientName = "Recipient",
            recipientPhone = "0911111111",
            distanceKm = 1.0,
            packages = emptyList()
        ).toInt()

        // 2. Chạy CÙNG LÚC 2 thao tác: Client hủy đơn, Delivery nhận đơn
        val cancelDeferred = async { repository.cancelRequestByClient(requestId, clientId) }
        val acceptDeferred = async { repository.acceptRequest(requestId, deliveryPersonId) }

        val cancelResult = cancelDeferred.await()
        val acceptSuccess = acceptDeferred.await()

        // 3. Kiểm tra trạng thái cuối cùng hợp lệ
        val finalRequest = db.deliveryRequestDao().getRequestById(requestId)!!
        val finalStatus = finalRequest.status

        assertTrue(
            "Trạng thái cuối cùng phải là DA_HUY hoặc DA_CHAP_NHAN",
            finalStatus == DeliveryStatus.DA_HUY || finalStatus == DeliveryStatus.DA_CHAP_NHAN
        )

        // 4. Đúng MỘT trong hai thao tác thắng, không phải cả hai
        val cancelWon = cancelResult is CancelResult.Success
        val acceptWon = acceptSuccess

        assertTrue("Phải có ít nhất một thao tác thắng", cancelWon || acceptWon)
        assertTrue("Không được để cả Cancel và Accept cùng thành công", !(cancelWon && acceptWon))

        if (cancelWon) assertEquals(DeliveryStatus.DA_HUY, finalStatus)
        if (acceptWon) assertEquals(DeliveryStatus.DA_CHAP_NHAN, finalStatus)
    }
}