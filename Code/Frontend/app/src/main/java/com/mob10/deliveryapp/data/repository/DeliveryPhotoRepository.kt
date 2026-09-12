package com.mob10.deliveryapp.data.repository

import com.mob10.deliveryapp.data.remote.RetrofitClient
import com.mob10.deliveryapp.data.remote.RemoteDataSource
import com.mob10.deliveryapp.data.remote.api.DeliveryPhoto

class DeliveryPhotoRepository {
    suspend fun avatar(id: Long) = RemoteDataSource.safeApiCall { RetrofitClient.deliveryPhotoApi.driverAvatar(id) }
    suspend fun load(id: Long) = RemoteDataSource.safeApiCall { RetrofitClient.deliveryPhotoApi.deliveryPhoto(id) }
    suspend fun complete(id: Long, image: String) = RemoteDataSource.safeApiCall { RetrofitClient.deliveryPhotoApi.completeWithPhoto(id, DeliveryPhoto(image)) }
}
