package com.mob10.deliveryapp.data.remote.api

import retrofit2.Response
import retrofit2.http.*

data class AccountProfile(val id: Long, val username: String, val fullName: String, val phoneNumber: String,
    val role: String, val licensePlate: String?, val email: String?, val emailVerified: Boolean, val avatarBase64: String?)
data class AccountEdit(val username: String, val fullName: String, val phoneNumber: String, val currentPassword: String, val avatarBase64: String?)
data class EmailLink(val email: String, val currentPassword: String)
data class EmailVerify(val code: String, val currentPassword: String)
data class RecoveryRequest(val phoneNumber: String)
data class RecoveryReset(val phoneNumber: String, val code: String, val newPassword: String)
data class AccountMessage(val message: String)

interface RecoveryApiService {
    @GET("account") suspend fun profile(): Response<AccountProfile>
    @PUT("account") suspend fun edit(@Body body: AccountEdit): Response<AccountProfile>
    @POST("account/email/request") suspend fun link(@Body body: EmailLink): Response<AccountMessage>
    @POST("account/email/verify") suspend fun verify(@Body body: EmailVerify): Response<AccountProfile>
    @GET("account/email/status") suspend fun emailStatus(): Response<AccountMessage>
    @POST("auth/recovery/request") suspend fun request(@Body body: RecoveryRequest): Response<AccountMessage>
    @POST("auth/recovery/complete") suspend fun reset(@Body body: RecoveryReset): Response<AccountMessage>
}
