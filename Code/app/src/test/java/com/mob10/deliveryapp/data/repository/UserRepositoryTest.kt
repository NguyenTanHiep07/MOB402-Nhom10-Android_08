package com.mob10.deliveryapp.data.repository

import com.mob10.deliveryapp.data.local.dao.UserDao
import com.mob10.deliveryapp.data.local.entity.UserEntity
import com.mob10.deliveryapp.data.model.Role
import com.mob10.deliveryapp.data.session.SessionStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UserRepositoryTest {
    private val deliveryUser = user(id = 7, role = Role.DELIVERY)

    @Test
    fun `successful Room login saves session user id`() = runBlocking {
        val storage = FakeSessionStorage()
        val repository = UserRepository(FakeUserDao(listOf(deliveryUser)), storage)

        val result = repository.login(deliveryUser.phoneNumber, deliveryUser.password)

        assertEquals(deliveryUser, result)
        assertEquals(deliveryUser.id, storage.storedUserId)
    }

    @Test
    fun `failed Room login does not create session`() = runBlocking {
        val storage = FakeSessionStorage()
        val repository = UserRepository(FakeUserDao(listOf(deliveryUser)), storage)

        val result = repository.login(deliveryUser.phoneNumber, "wrong-password")

        assertNull(result)
        assertNull(storage.storedUserId)
    }

    @Test
    fun `valid session restores current Room user`() = runBlocking {
        val storage = FakeSessionStorage(initialUserId = deliveryUser.id)
        val repository = UserRepository(FakeUserDao(listOf(deliveryUser)), storage)

        assertEquals(deliveryUser, repository.restoreSession())
        assertEquals(0, storage.clearCount)
    }

    @Test
    fun `session for deleted user is cleared and falls back to null`() = runBlocking {
        val storage = FakeSessionStorage(initialUserId = 999)
        val repository = UserRepository(FakeUserDao(listOf(deliveryUser)), storage)

        assertNull(repository.restoreSession())
        assertNull(storage.storedUserId)
        assertEquals(1, storage.clearCount)
    }

    @Test
    fun `logout clears persisted session`() = runBlocking {
        val storage = FakeSessionStorage(initialUserId = deliveryUser.id)
        val repository = UserRepository(FakeUserDao(listOf(deliveryUser)), storage)

        repository.logout()

        assertNull(storage.storedUserId)
        assertEquals(1, storage.clearCount)
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

private class FakeSessionStorage(initialUserId: Int? = null) : SessionStorage {
    var storedUserId: Int? = initialUserId
        private set
    var clearCount: Int = 0
        private set

    override suspend fun getUserId(): Int? = storedUserId

    override suspend fun saveUserId(userId: Int) {
        storedUserId = userId
    }

    override suspend fun clear() {
        storedUserId = null
        clearCount++
    }
}

private class FakeUserDao(users: List<UserEntity>) : UserDao {
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

    override suspend fun deleteByUsernames(usernames: List<String>): Int {
        val matchingIds = usersById.values
            .filter { it.username in usernames }
            .map { it.id }
        matchingIds.forEach(usersById::remove)
        return matchingIds.size
    }

    override fun getAllUsers(): Flow<List<UserEntity>> = flowOf(usersById.values.toList())

    override fun getUsersByRole(role: Role): Flow<List<UserEntity>> =
        flowOf(usersById.values.filter { it.role == role })

    override fun getCountByRole(role: Role): Flow<Int> =
        flowOf(usersById.values.count { it.role == role })

    override fun getTotalUserCount(): Flow<Int> = flowOf(usersById.size)
}
