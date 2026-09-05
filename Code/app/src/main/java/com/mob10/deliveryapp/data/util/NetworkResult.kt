package com.mob10.deliveryapp.data.util

/**
 * Kết quả thống nhất cho mọi API call trong ứng dụng.
 *
 * UI layer chỉ cần observe một sealed class duy nhất để xử lý
 * Loading → Success/Empty/Error mà không cần biết chi tiết network.
 */
sealed class NetworkResult<out T> {

    /** Đang gọi API, UI hiển thị loading indicator. */
    data object Loading : NetworkResult<Nothing>()

    /** API trả về dữ liệu thành công. */
    data class Success<T>(val data: T) : NetworkResult<T>()

    /** API thành công nhưng không có dữ liệu (ví dụ: danh sách rỗng). */
    data object Empty : NetworkResult<Nothing>()

    /**
     * Lỗi xảy ra khi gọi API.
     * @param code Mã lỗi backend (ví dụ: ORDER_ALREADY_TAKEN, UNAUTHORIZED)
     * @param message Thông báo lỗi hiển thị cho người dùng
     * @param httpCode HTTP status code (401, 403, 409, 500...)
     */
    data class Error(
        val code: String? = null,
        val message: String,
        val httpCode: Int? = null
    ) : NetworkResult<Nothing>() {

        val isUnauthorized: Boolean get() = httpCode == 401
        val isForbidden: Boolean get() = httpCode == 403
        val isConflict: Boolean get() = httpCode == 409
        val isLocked: Boolean get() = httpCode == 423
        val isServerError: Boolean get() = httpCode != null && httpCode >= 500
        val isNetworkError: Boolean get() = httpCode == null && code == NETWORK_ERROR
        val isTimeout: Boolean get() = code == TIMEOUT_ERROR

        companion object {
            const val NETWORK_ERROR = "NETWORK_ERROR"
            const val TIMEOUT_ERROR = "TIMEOUT_ERROR"
            const val UNKNOWN_ERROR = "UNKNOWN_ERROR"

            fun networkUnavailable() = Error(
                code = NETWORK_ERROR,
                message = "Không có kết nối mạng. Vui lòng kiểm tra lại."
            )

            fun timeout() = Error(
                code = TIMEOUT_ERROR,
                message = "Yêu cầu quá thời gian. Vui lòng thử lại."
            )

            fun unknown(throwable: Throwable? = null) = Error(
                code = UNKNOWN_ERROR,
                message = throwable?.message ?: "Đã xảy ra lỗi không xác định."
            )
        }
    }
}

/**
 * Chuyển đổi dữ liệu bên trong NetworkResult.Success mà không mất thông tin lỗi.
 *
 * Dùng trong Repository để map DTO → Domain Model:
 * ```
 * RemoteDataSource.safeApiCall { api.getOrders() }
 *     .mapData { it.toDomainList() }
 * ```
 *
 * Nếu [transform] ném RuntimeException (ví dụ IllegalArgumentException khi parse enum),
 * kết quả sẽ là NetworkResult.Error thay vì crash.
 */
fun <T, R> NetworkResult<T>.mapData(transform: (T) -> R): NetworkResult<R> = when (this) {
    is NetworkResult.Success -> try {
        NetworkResult.Success(transform(data))
    } catch (cancelled: kotlinx.coroutines.CancellationException) {
        throw cancelled
    } catch (_: RuntimeException) {
        NetworkResult.Error(
            code = "INVALID_SERVER_RESPONSE",
            message = "Dữ liệu máy chủ trả về không đúng định dạng."
        )
    }
    is NetworkResult.Empty -> NetworkResult.Empty
    is NetworkResult.Error -> this
    is NetworkResult.Loading -> NetworkResult.Loading
}
