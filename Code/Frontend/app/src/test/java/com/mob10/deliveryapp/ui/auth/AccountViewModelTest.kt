package com.mob10.deliveryapp.ui.auth

import com.mob10.deliveryapp.data.local.dao.UserDao
import com.mob10.deliveryapp.data.local.entity.UserEntity
import com.mob10.deliveryapp.data.model.Role
import com.mob10.deliveryapp.data.remote.api.AccountEdit
import com.mob10.deliveryapp.data.remote.api.AccountProfile
import com.mob10.deliveryapp.data.repository.AccountRepository
import com.mob10.deliveryapp.data.util.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class AccountViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeUserDao: FakeUserDao
    private var callbackProfile: AccountProfile? = null

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeUserDao = FakeUserDao()
        callbackProfile = null
        runBlocking {
            fakeUserDao.insert(
                UserEntity(
                    id = 1,
                    username = "old_client",
                    password = "hashed_password",
                    fullName = "Khách Cũ",
                    phoneNumber = "0111222333",
                    role = Role.CLIENT,
                    licensePlate = null
                )
            )
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testSaveProfileSuccess_syncsToRoomDaoAndCallback() = runTest(testDispatcher) {
        val fakeRepo = object : AccountRepository() {
            override suspend fun edit(body: AccountEdit): NetworkResult<AccountProfile> {
                return NetworkResult.Success(
                    AccountProfile(
                        id = 1L,
                        username = body.username,
                        fullName = body.fullName,
                        phoneNumber = body.phoneNumber,
                        role = "CLIENT",
                        email = "client@example.com",
                        emailVerified = true,
                        licensePlate = null,
                        avatarBase64 = null
                    )
                )
            }
        }

        val vm = AccountViewModel(
            repo = fakeRepo,
            userDao = fakeUserDao,
            onProfileUpdated = { callbackProfile = it },
            ioDispatcher = testDispatcher
        )

        vm.save(
            username = "new_client",
            name = "Khách Hàng Mới",
            phone = "0999888777",
            password = "current_pass"
        )

        advanceUntilIdle()

        val updatedUser = fakeUserDao.getUserById(1)
        assertNotNull(updatedUser)
        assertEquals("new_client", updatedUser!!.username)
        assertEquals("Khách Hàng Mới", updatedUser.fullName)
        assertEquals("0999888777", updatedUser.phoneNumber)
        assertEquals("hashed_password", updatedUser.password) // Password hash preserved
        assertEquals(Role.CLIENT, updatedUser.role) // Role preserved

        assertNotNull(callbackProfile)
        assertEquals("new_client", callbackProfile!!.username)
        assertEquals("Khách Hàng Mới", callbackProfile!!.fullName)
    }

    @Test
    fun testSaveProfileFailure_doesNotModifyLocalData() = runTest(testDispatcher) {
        val fakeRepo = object : AccountRepository() {
            override suspend fun edit(body: AccountEdit): NetworkResult<AccountProfile> {
                return NetworkResult.Error(message = "Tên đăng nhập đã tồn tại")
            }
        }

        val vm = AccountViewModel(
            repo = fakeRepo,
            userDao = fakeUserDao,
            onProfileUpdated = { callbackProfile = it },
            ioDispatcher = testDispatcher
        )

        vm.save(
            username = "taken_username",
            name = "Tên Sẽ Không Được Lưu",
            phone = "0999888777",
            password = "current_pass"
        )

        advanceUntilIdle()

        val user = fakeUserDao.getUserById(1)
        assertNotNull(user)
        assertEquals("old_client", user!!.username)
        assertEquals("Khách Cũ", user.fullName)
        assertEquals("0111222333", user.phoneNumber)
        assertNull(callbackProfile)
        assertEquals("Tên đăng nhập đã tồn tại", vm.state.value.error)
    }

    // ── Fake UserDao Implementation ───────────────────────────────────

    class FakeUserDao : UserDao {
        private val users = mutableMapOf<Int, UserEntity>()

        override suspend fun login(phoneNumber: String, password: String): UserEntity? {
            return users.values.find { it.phoneNumber == phoneNumber && it.password == password }
        }

        override suspend fun getUserById(userId: Int): UserEntity? = users[userId]

        override suspend fun insert(user: UserEntity): Long {
            users[user.id] = user
            return user.id.toLong()
        }

        override suspend fun upsert(user: UserEntity) {
            users[user.id] = user
        }

        override suspend fun update(user: UserEntity) {
            users[user.id] = user
        }

        override suspend fun deleteByUsernames(usernames: List<String>): Int {
            val count = users.values.count { it.username in usernames }
            users.entries.removeIf { it.value.username in usernames }
            return count
        }

        override fun getAllUsers(): Flow<List<UserEntity>> = flowOf(users.values.toList())

        override fun getUsersByRole(role: Role): Flow<List<UserEntity>> =
            flowOf(users.values.filter { it.role == role })

        override fun getCountByRole(role: Role): Flow<Int> =
            flowOf(users.values.count { it.role == role })

        override fun getTotalUserCount(): Flow<Int> = flowOf(users.size)
    }
}
