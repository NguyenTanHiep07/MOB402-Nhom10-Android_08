package com.mob10.deliveryapp.data.repository

import com.mob10.deliveryapp.data.remote.RemoteDataSource.safeApiCall
import com.mob10.deliveryapp.data.remote.RetrofitClient
import com.mob10.deliveryapp.data.remote.api.*

class AccountRepository {
    private val api get() = RetrofitClient.recoveryApi
    suspend fun profile() = safeApiCall { api.profile() }
    suspend fun edit(body: AccountEdit) = safeApiCall { api.edit(body) }
    suspend fun link(body: EmailLink) = safeApiCall { api.link(body) }
    suspend fun verify(body: EmailVerify) = safeApiCall { api.verify(body) }
    suspend fun emailStatus() = safeApiCall { api.emailStatus() }
    suspend fun request(phone: String) = safeApiCall { api.request(RecoveryRequest(phone)) }
    suspend fun reset(body: RecoveryReset) = safeApiCall { api.reset(body) }
}
