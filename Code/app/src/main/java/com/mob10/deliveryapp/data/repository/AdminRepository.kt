package com.mob10.deliveryapp.data.repository

import com.mob10.deliveryapp.data.model.AdminDriver
import com.mob10.deliveryapp.data.model.AdminUser
import com.mob10.deliveryapp.data.model.Order
import com.mob10.deliveryapp.data.remote.RemoteDataSource
import com.mob10.deliveryapp.data.remote.api.AdminApiService
import com.mob10.deliveryapp.data.remote.mapper.AdminMapper.toDomainDriverList
import com.mob10.deliveryapp.data.remote.mapper.AdminMapper.toDomainUserList
import com.mob10.deliveryapp.data.remote.mapper.OrderMapper.toDomainList
import com.mob10.deliveryapp.data.util.NetworkResult
import com.mob10.deliveryapp.data.util.mapData

/**
 * Repository REST cho Admin — quản lý tài khoản, tài xế và đơn hàng.
 *
 * Tất cả endpoint yêu cầu JWT token với role ADMIN.
 */
class AdminRepository(
    private val adminApi: AdminApiService
) {

    /** Danh sách tất cả tài khoản trong hệ thống. */
    suspend fun getUsers(): NetworkResult<List<AdminUser>> =
        RemoteDataSource.safeApiCall {
            adminApi.getUsers()
        }.mapData { it.toDomainUserList() }

    /** Danh sách tài xế kèm thống kê điểm tin cậy. */
    suspend fun getDrivers(): NetworkResult<List<AdminDriver>> =
        RemoteDataSource.safeApiCall {
            adminApi.getDrivers()
        }.mapData { it.toDomainDriverList() }

    /** Tài xế có điểm tin cậy dưới 70 — cần cảnh báo. */
    suspend fun getDriverAlerts(): NetworkResult<List<AdminDriver>> =
        RemoteDataSource.safeApiCall {
            adminApi.getDriverAlerts()
        }.mapData { it.toDomainDriverList() }

    /** Toàn bộ đơn hàng trong hệ thống (admin xem tất cả). */
    suspend fun getAllOrders(): NetworkResult<List<Order>> =
        RemoteDataSource.safeApiCall {
            adminApi.getAllOrders()
        }.mapData { it.toDomainList() }
}
