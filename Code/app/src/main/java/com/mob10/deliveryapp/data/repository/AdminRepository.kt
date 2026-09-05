package com.mob10.deliveryapp.data.repository

import com.mob10.deliveryapp.data.remote.AdminApiService

/**
 * Repository CHỈ gọi API backend cho màn hình Admin.
 * Trả về null khi lỗi (mất mạng, server lỗi...) để ViewModel tự quyết định hiển thị gì.
 */
class AdminRepository(private val api: AdminApiService) {

    suspend fun getTotalUserCount(): Int? = runCatchingOrNull {
        val response = api.getAllUsers()
        if (response.isSuccessful) response.body()?.size else null
    }

    suspend fun getClientCount(): Int? = runCatchingOrNull {
        val response = api.getAllUsers()
        if (response.isSuccessful) {
            response.body()?.count { it.role == "CLIENT" }
        } else null
    }

    suspend fun getDriverCount(): Int? = runCatchingOrNull {
        val response = api.getAllDrivers()
        if (response.isSuccessful) response.body()?.size else null
    }

    suspend fun getTotalRequestCount(): Int? = runCatchingOrNull {
        val response = api.getAllOrders()
        if (response.isSuccessful) response.body()?.size else null
    }

    suspend fun getPendingRequestCount(): Int? = runCatchingOrNull {
        val response = api.getAllOrders()
        if (response.isSuccessful) {
            response.body()?.count { it.status == "CHO_TIEP_NHAN" }
        } else null
    }

    private suspend fun <T> runCatchingOrNull(block: suspend () -> T?): T? {
        return try {
            block()
        } catch (e: Exception) {
            null
        }
    }
}