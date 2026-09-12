package com.mob10.deliveryapp.data.repository

import com.mob10.deliveryapp.data.remote.RemoteDataSource.safeApiCall
import com.mob10.deliveryapp.data.remote.RetrofitClient
import com.mob10.deliveryapp.data.remote.api.*

open class AccountRepository(private val apiProvider: () -> RecoveryApiService = { RetrofitClient.recoveryApi }) {
    private val api get() = apiProvider()
    open suspend fun profile() = safeApiCall { api.profile() }
    open suspend fun edit(body: AccountEdit) = safeApiCall { api.edit(body) }
    open suspend fun link(body: EmailLink) = safeApiCall { api.link(body) }
    open suspend fun verify(body: EmailVerify) = safeApiCall { api.verify(body) }
    open suspend fun emailStatus() = safeApiCall { api.emailStatus() }
    open suspend fun request(phone: String) = safeApiCall { api.request(RecoveryRequest(phone)) }
    open suspend fun reset(body: RecoveryReset) = safeApiCall { api.reset(body) }
}
