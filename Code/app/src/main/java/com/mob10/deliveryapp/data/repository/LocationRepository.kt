package com.mob10.deliveryapp.data.repository

import com.mob10.deliveryapp.data.model.AddressSuggestion
import com.mob10.deliveryapp.data.model.RouteEstimate
import com.mob10.deliveryapp.data.remote.RemoteDataSource
import com.mob10.deliveryapp.data.remote.api.LocationApiService
import com.mob10.deliveryapp.data.remote.dto.CoordinateInputDto
import com.mob10.deliveryapp.data.remote.dto.RouteEstimateRequestDto
import com.mob10.deliveryapp.data.remote.mapper.LocationMapper.toDomain
import com.mob10.deliveryapp.data.remote.mapper.LocationMapper.toDomainSuggestionList
import com.mob10.deliveryapp.data.util.NetworkResult
import com.mob10.deliveryapp.data.util.mapData

/**
 * Repository REST cho Location/Route API.
 *
 * Cung cấp interface sạch cho UI — ViewModel gọi repository,
 * không import Retrofit trực tiếp.
 *
 * Lưu ý quan trọng:
 * - Android phải debounce autocomplete 400-500ms.
 * - Route estimate chỉ để preview, server tính lại khi tạo đơn.
 */
class LocationRepository(
    private val locationApi: LocationApiService
) {

    /**
     * Tìm kiếm gợi ý địa chỉ.
     *
     * @param query Chuỗi tìm kiếm (tối thiểu 3 ký tự).
     * @param limit Số kết quả tối đa.
     */
    suspend fun autocomplete(
        query: String,
        limit: Int = 6
    ): NetworkResult<List<AddressSuggestion>> =
        RemoteDataSource.safeApiCall {
            locationApi.autocomplete(query = query, limit = limit)
        }.mapData { it.toDomainSuggestionList() }

    /**
     * Ước lượng tuyến đường và phí tạm tính.
     *
     * @param pickupLat Vĩ độ điểm lấy hàng.
     * @param pickupLng Kinh độ điểm lấy hàng.
     * @param deliveryLat Vĩ độ điểm giao hàng.
     * @param deliveryLng Kinh độ điểm giao hàng.
     * @param totalWeightKg Tổng trọng lượng kiện hàng.
     * @param fragile Có hàng dễ vỡ không.
     * @param express Có giao hỏa tốc không.
     */
    suspend fun estimateRoute(
        pickupLat: Double,
        pickupLng: Double,
        deliveryLat: Double,
        deliveryLng: Double,
        totalWeightKg: Double,
        fragile: Boolean = false,
        express: Boolean = false
    ): NetworkResult<RouteEstimate> =
        RemoteDataSource.safeApiCall {
            locationApi.estimateRoute(
                RouteEstimateRequestDto(
                    pickup = CoordinateInputDto(latitude = pickupLat, longitude = pickupLng),
                    delivery = CoordinateInputDto(latitude = deliveryLat, longitude = deliveryLng),
                    totalWeightKg = totalWeightKg,
                    fragile = fragile,
                    express = express
                )
            )
        }.mapData { it.toDomain() }
}
