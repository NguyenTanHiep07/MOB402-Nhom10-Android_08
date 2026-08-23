package com.mob10.deliveryapp.ui.auth

import com.mob10.deliveryapp.data.local.dao.UserDao
import com.mob10.deliveryapp.data.local.entity.UserEntity
import com.mob10.deliveryapp.data.model.Role
import com.mob10.deliveryapp.data.repository.UserRepository
import com.mob10.deliveryapp.data.session.SessionStorage
import com.mob10.deliveryapp.ui.navigation.AppDestination
import com.mob10.deliveryapp.ui.navigation.destinationFor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `restart restores persisted admin and routes to admin home`() = runTest {
        val admin = user(id = 3, role = Role.ADMIN)
        val storage = TestSessionStorage(initialUserId = admin.id)
        val viewModel = AuthViewModel(
            userRepository = UserRepository(TestUserDao(listOf(admin)), storage),
            initializeDatabase = {}
        )

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isInitializing)
        assertEquals(admin, viewModel.uiState.value.currentUser)
        assertEquals(
            AppDestination.ADMIN_HOME,
            destinationFor(viewModel.uiState.value.currentUser?.role)
        )
    }

    @Test
    fun `invalid persisted user is cleared and routes to login`() = runTest {
        val storage = TestSessionStorage(initialUserId = 404)
        val viewModel = AuthViewModel(
            userRepository = UserRepository(TestUserDao(emptyList()), storage),
            initializeDatabase = {}
        )

        advanceUntilIdle()

        assertNull(storage.storedUserId)
        assertNull(viewModel.uiState.value.currentUser)
        assertEquals(AppDestination.LOGIN, destinationFor(viewModel.uiState.value.currentUser?.role))
    }

    @Test
    fun `delivery login uses Room role and logout clears session`() = runTest {
        val delivery = user(id = 2, role = Role.DELIVERY)
        val storage = TestSessionStorage()
        val viewModel = AuthViewModel(
            userRepository = UserRepository(TestUserDao(listOf(delivery)), storage),
            initializeDatabase = {}
        )
        advanceUntilIdle()

        viewModel.login(delivery.phoneNumber, delivery.password)
        advanceUntilIdle()

        assertEquals(delivery.id, storage.storedUserId)
        assertEquals(
            AppDestination.DELIVERY_HOME,
            destinationFor(viewModel.uiState.value.currentUser?.role)
        )

        viewModel.logout()
        advanceUntilIdle()

        assertNull(storage.storedUserId)
        assertNull(viewModel.uiState.value.currentUser)
        assertEquals(AppDestination.LOGIN, destinationFor(viewModel.uiState.value.currentUser?.role))
    }

    @Test
    fun `client login uses Room role and routes to client home`() = runTest {
        val client = user(id = 1, role = Role.CLIENT)
        val storage = TestSessionStorage()
        val viewModel = AuthViewModel(
            userRepository = UserRepository(TestUserDao(listOf(client)), storage),
            initializeDatabase = {}
        )
        advanceUntilIdle()

        viewModel.login(client.phoneNumber, client.password)
        advanceUntilIdle()

        assertEquals(client.id, storage.storedUserId)
        assertEquals(
            AppDestination.CLIENT_HOME,
            destinationFor(viewModel.uiState.value.currentUser?.role)
        )
    }

    private fun user(id: Int, role: Role) = UserEntity(
        id = id,
        username = "user-$id",
        password = "123456",
        fullName = "Test User $id",
        phoneNumber = "09000000$id",
        role = role
    )
}

private class TestSessionStorage(initialUserId: Int? = null) : SessionStorage {
    var storedUserId: Int? = initialUserId
        private set

    override suspend fun getUserId(): Int? = storedUserId

    override suspend fun saveUserId(userId: Int) {
        storedUserId = userId
    }

    override suspend fun clear() {
        storedUserId = null
    }
}

private class TestUserDao(users: List<UserEntity>) : UserDao {
    private val usersById = users.associateBy { it.id }.toMutableMap()

    override suspend fun login(phoneNumber: String, password: String): UserEntity? =
        usersById.values.firstOrNull {
            it.phoneNumber == phoneNumber && it.password == password
        }

    override suspend fun getUserById(userId: Int): UserEntity? = usersById[userId]

    override suspend fun insert(user: UserEntity): Long {
        usersById[user.id] = user
        return user.id.toLong()
    }

    override suspend fun deleteByUsernames(usernames: List<String>): Int = 0

    override fun getAllUsers(): Flow<List<UserEntity>> = flowOf(usersById.values.toList())

    override fun getUsersByRole(role: Role): Flow<List<UserEntity>> =
        flowOf(usersById.values.filter { it.role == role })

    override fun getCountByRole(role: Role): Flow<Int> =
        flowOf(usersById.values.count { it.role == role })

    override fun getTotalUserCount(): Flow<Int> = flowOf(usersById.size)
}
