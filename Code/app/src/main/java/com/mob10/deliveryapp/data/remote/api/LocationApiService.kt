package com.mob10.deliveryapp.data.remote.api

import com.mob10.deliveryapp.data.remote.dto.AddressSuggestionResponseDto
import com.mob10.deliveryapp.data.remote.dto.RouteEstimateRequestDto
import com.mob10.deliveryapp.data.remote.dto.RouteEstimateResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Location & Route API Service.
 *
 * Android KHÔNG gọi trực tiếp Photon/OSRM — chỉ gọi qua backend.
 *
 * - Autocomplete: dành cho CLIENT, debounce 400-500ms phía Android.
 * - Route estimate: dùng preview, server tính lại khi tạo đơn.
 */
interface LocationApiService {

    /**
     * Tìm kiếm địa chỉ gợi ý (autocomplete).
     *
     * @param query Chuỗi tìm kiếm, tối thiểu 3 ký tự.
     * @param limit Số kết quả tối đa (mặc định 6, server giới hạn 8).
     */
    @GET("locations/autocomplete")
    suspend fun autocomplete(
        @Query("query") query: String,
        @Query("limit") limit: Int = 6
    ): Response<List<AddressSuggestionResponseDto>>

    /**
     * Ước lượng tuyến đường và phí tạm tính.
     *
     * Kết quả chỉ để preview — server tính lại khi tạo đơn thật.
     */
    @POST("routes/estimate")
    suspend fun estimateRoute(
        @Body request: RouteEstimateRequestDto
    ): Response<RouteEstimateResponseDto>
}
