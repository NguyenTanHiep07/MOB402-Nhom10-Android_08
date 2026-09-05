package com.mob10.deliveryapp.data.remote

import com.google.gson.Gson
import com.mob10.deliveryapp.data.remote.dto.ApiErrorResponse
import com.mob10.deliveryapp.data.util.NetworkResult
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Base layer xử lý tất cả API call — parse response/error → NetworkResult.
 *
 * Mọi Repository đều gọi qua [safeApiCall] để đảm bảo:
 * 1. Exception → NetworkResult.Error (không crash)
 * 2. HTTP error body → parse thành ApiErrorResponse → lấy code + message
 * 3. Success nhưng body null → NetworkResult.Empty
 * 4. Timeout, no network → Error cụ thể
 */
object RemoteDataSource {

    private val gson = Gson()

    /**
     * Bọc một suspend API call trong try-catch, tự động parse response.
     *
     * @param apiCall Lambda gọi Retrofit suspend function
     * @return NetworkResult.Success / Empty / Error
     */
    suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): NetworkResult<T> {
        return try {
            val response = apiCall()
            handleResponse(response)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: SocketTimeoutException) {
            NetworkResult.Error.timeout()
        } catch (e: UnknownHostException) {
            NetworkResult.Error.networkUnavailable()
        } catch (e: java.net.ConnectException) {
            NetworkResult.Error.networkUnavailable()
        } catch (e: java.io.IOException) {
            NetworkResult.Error(
                code = NetworkResult.Error.NETWORK_ERROR,
                message = "Lỗi kết nối: ${e.message}"
            )
        } catch (e: Exception) {
            NetworkResult.Error.unknown(e)
        }
    }

    /**
     * Parse Retrofit Response thành NetworkResult.
     * - 2xx + body != null → Success
     * - 2xx + body == null → Empty
     * - 4xx/5xx → parse error body → Error với httpCode, code, message
     */
    private fun <T> handleResponse(response: Response<T>): NetworkResult<T> {
        if (response.isSuccessful) {
            val body = response.body()
            return if (body != null) {
                // Xử lý trường hợp List rỗng
                if (body is List<*> && body.isEmpty()) {
                    NetworkResult.Empty
                } else {
                    NetworkResult.Success(body)
                }
            } else {
                NetworkResult.Empty
            }
        }

        // Parse error body
        val errorBody = response.errorBody()?.string()
        val apiError = parseErrorBody(errorBody)

        return NetworkResult.Error(
            code = apiError?.code,
            message = apiError?.message ?: getDefaultErrorMessage(response.code()),
            httpCode = response.code()
        )
    }

    /**
     * Parse error body JSON thành ApiErrorResponse.
     * Nếu parse thất bại (body không phải JSON), trả null.
     */
    private fun parseErrorBody(errorBody: String?): ApiErrorResponse? {
        if (errorBody.isNullOrBlank()) return null
        return try {
            gson.fromJson(errorBody, ApiErrorResponse::class.java)
        } catch (_: Exception) {
            null
        }
    }

    /** Thông báo lỗi mặc định theo HTTP status code. */
    private fun getDefaultErrorMessage(httpCode: Int): String = when (httpCode) {
        400 -> "Dữ liệu gửi lên không hợp lệ."
        401 -> "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại."
        403 -> "Bạn không có quyền thực hiện thao tác này."
        404 -> "Không tìm thấy dữ liệu yêu cầu."
        409 -> "Xung đột dữ liệu. Vui lòng thử lại."
        423 -> "Tài khoản bị khóa tạm thời."
        500 -> "Máy chủ gặp lỗi. Vui lòng thử lại sau."
        503 -> "Dịch vụ tạm thời không khả dụng."
        504 -> "Máy chủ phản hồi quá chậm. Vui lòng thử lại."
        else -> "Đã xảy ra lỗi (mã $httpCode)."
    }
}
