package com.mob10.deliveryapp.data.repository

import com.mob10.deliveryapp.data.remote.api.AdminApiService
import com.mob10.deliveryapp.data.remote.dto.*
import com.mob10.deliveryapp.data.util.NetworkResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class AdminRepositoryTest {

    private lateinit var fakeAdminApi: FakeAdminApiService
    private lateinit var repository: AdminRepository

    @Before
    fun setUp() {
        fakeAdminApi = FakeAdminApiService()
        repository = AdminRepository(fakeAdminApi)
    }

    @Test
    fun testGetUsersSuccess() = runBlocking {
        val result = repository.getUsers()
        assertTrue(result is NetworkResult.Success)
        val users = (result as NetworkResult.Success).data
        assertEquals(2, users.size)
        assertEquals("admin1", users[0].username)
        assertEquals("CLIENT", users[1].role)
    }

    @Test
    fun testGetDriversSuccess() = runBlocking {
        val result = repository.getDrivers()
        assertTrue(result is NetworkResult.Success)
        val drivers = (result as NetworkResult.Success).data
        assertEquals(1, drivers.size)
        assertEquals("shipper1", drivers[0].user.username)
        assertEquals(95.0, drivers[0].statistics!!.reliabilityScore, 0.01)
    }

    @Test
    fun testGetDriverAlertsSuccess() = runBlocking {
        val result = repository.getDriverAlerts()
        assertTrue(result is NetworkResult.Success)
        val alerts = (result as NetworkResult.Success).data
        assertEquals(1, alerts.size)
        assertEquals(55.0, alerts[0].statistics!!.reliabilityScore, 0.01)
        assertTrue(alerts[0].statistics!!.isWarning)
    }

    // ── Fake Implementation ──────────────────────────────────────────

    class FakeAdminApiService : AdminApiService {
        override suspend fun getUsers(): Response<List<AdminUserResponseDto>> {
            val user1 = AdminUserResponseDto(1L, "admin1", "Admin System", "0900000000", "ADMIN", null, null, true, "2026-01-01T00:00:00Z")
            val user2 = AdminUserResponseDto(2L, "client1", "Khach Hang A", "0911111111", "CLIENT", null, null, true, "2026-01-02T00:00:00Z")
            return Response.success(listOf(user1, user2))
        }

        override suspend fun getDrivers(): Response<List<AdminDriverResponseDto>> {
            val driverUser = AdminUserResponseDto(3L, "shipper1", "Tai Xe B", "0922222222", "DELIVERY", "59A-12345", "AVAILABLE", true, "2026-01-03T00:00:00Z")
            val stats = DriverStatisticsResponseDto(3L, 20, 1, 0, 95.0, null, false, false)
            return Response.success(listOf(AdminDriverResponseDto(driverUser, stats)))
        }

        override suspend fun getDriverAlerts(): Response<List<AdminDriverResponseDto>> {
            val alertUser = AdminUserResponseDto(4L, "shipper2", "Tai Xe C", "0933333333", "DELIVERY", "59B-67890", "OFFLINE", true, "2026-01-04T00:00:00Z")
            val alertStats = DriverStatisticsResponseDto(4L, 5, 8, 4, 55.0, "2026-09-04T10:00:00Z", true, true)
            return Response.success(listOf(AdminDriverResponseDto(alertUser, alertStats)))
        }

        override suspend fun getAllOrders(): Response<List<OrderResponseDto>> {
            return Response.success(emptyList())
        }
    }
}
