package com.mob10.deliveryapp.data.repository

import com.mob10.deliveryapp.data.remote.api.AuthApiService
import com.mob10.deliveryapp.data.remote.dto.LoginRequest
import com.mob10.deliveryapp.data.remote.dto.LoginResponse
import com.mob10.deliveryapp.data.remote.dto.UserSummaryDto
import com.mob10.deliveryapp.data.session.SessionStorage
import com.mob10.deliveryapp.data.session.TokenManager
import com.mob10.deliveryapp.data.util.NetworkResult
import io.mockk.*
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class AuthRepositoryTest {

    private val authApi: AuthApiService = mockk()
    private val tokenManager: TokenManager = mockk(relaxed = true)
    private val sessionStorage: SessionStorage = mockk(relaxed = true)
    private lateinit var repository: AuthRepository

    @Before
    fun setUp() {
        repository = AuthRepository(authApi, tokenManager, sessionStorage)
    }

    @Test
    fun `login success saves token and userId`() = runBlocking {
        val userSummary = UserSummaryDto(
            id = 5L,
            username = "shipper1",
            fullName = "Tài Xế A",
            phoneNumber = "0901234567",
            role = "DELIVERY",
            licensePlate = "59-X1 99999"
        )
        val loginResponse = LoginResponse(
            accessToken = "mock_jwt_token_12345",
            tokenType = "Bearer",
            expiresInMs = 86400000L,
            user = userSummary
        )

        coEvery { authApi.login(LoginRequest("shipper1", "123456")) } returns Response.success(loginResponse)

        val result = repository.login("shipper1", "123456")

        assertTrue(result is NetworkResult.Success)
        val data = (result as NetworkResult.Success).data
        assertEquals("mock_jwt_token_12345", data.accessToken)
        assertEquals(5L, data.user.id)

        // Verify token & session were saved
        verify { tokenManager.saveToken("mock_jwt_token_12345", "Bearer", 86400000L) }
        coVerify { sessionStorage.saveUserId(5) }
    }

    @Test
    fun `login failure returns Error and does not save token`() = runBlocking {
        val errorJson = """{"status":401,"code":"INVALID_CREDENTIALS","message":"Sai tài khoản hoặc mật khẩu"}"""
        val errorBody = errorJson.toResponseBody("application/json".toMediaType())

        coEvery { authApi.login(any()) } returns Response.error(401, errorBody)

        val result = repository.login("shipper1", "wrong_pass")

        assertTrue(result is NetworkResult.Error)
        val error = result as NetworkResult.Error
        assertEquals(401, error.httpCode)
        assertEquals("INVALID_CREDENTIALS", error.code)
        assertEquals("Sai tài khoản hoặc mật khẩu", error.message)

        verify(exactly = 0) { tokenManager.saveToken(any(), any(), any()) }
        coVerify(exactly = 0) { sessionStorage.saveUserId(any()) }
    }

    @Test
    fun `logout clears token and session`() = runBlocking {
        repository.logout()

        verify { tokenManager.clearToken() }
        coVerify { sessionStorage.clear() }
    }
}
