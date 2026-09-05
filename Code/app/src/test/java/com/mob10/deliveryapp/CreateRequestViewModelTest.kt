package com.mob10.deliveryapp

import androidx.lifecycle.SavedStateHandle
import com.mob10.deliveryapp.data.model.AddressSuggestion
import com.mob10.deliveryapp.data.remote.api.LocationApiService
import com.mob10.deliveryapp.data.remote.dto.*
import com.mob10.deliveryapp.data.repository.LocationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class CreateRequestViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun cleanup() { Dispatchers.resetMain() }

    @Test fun `quoted form preserves coordinates across recreation and invalidates quote after address edit`() = runTest(dispatcher) {
        var request: RouteEstimateRequestDto? = null
        val api = object : LocationApiService {
            override suspend fun autocomplete(query: String, limit: Int) = Response.success(emptyList<AddressSuggestionResponseDto>())
            override suspend fun estimateRoute(input: RouteEstimateRequestDto): Response<RouteEstimateResponseDto> {
                request = input
                return Response.success(RouteEstimateResponseDto(5.64, 17, 15000.0, 28200.0, 3000.0, 0.0, 46200.0))
            }
        }
        val saved = SavedStateHandle()
        val vm = CreateRequestViewModel(LocationRepository(api), saved)
        vm.onSenderNameChanged("Người gửi")
        vm.onSenderPhoneChanged("0901234567")
        vm.onReceiverNameChanged("Người nhận")
        vm.onReceiverPhoneChanged("0987654321")
        vm.onPackageNameChanged("Sách")
        vm.onWeightChanged("1")
        vm.selectAddress(true, address(10.7769, 106.7008))
        vm.selectAddress(false, address(10.7827, 106.6957))
        advanceUntilIdle()
        assertTrue(vm.validateForm())
        assertEquals(46200L, vm.uiState.value.feeQuote.totalFee)
        assertEquals(10.7769, request!!.pickup.latitude, 0.00001)
        val restored = CreateRequestViewModel(LocationRepository(api), saved)
        assertTrue(restored.validateForm())
        assertEquals(vm.uiState.value.pickup, restored.uiState.value.pickup)
        restored.onPickupAddressChanged("Địa chỉ khác")
        assertFalse(restored.validateForm())
        assertNull(restored.uiState.value.pickup)
        assertEquals(0L, restored.uiState.value.feeQuote.totalFee)
        advanceUntilIdle()
    }

    @Test fun `typed addresses alone never permit a zero coordinate order`() {
        val vm = CreateRequestViewModel()
        vm.onSenderNameChanged("A")
        vm.onSenderPhoneChanged("0901234567")
        vm.onReceiverNameChanged("B")
        vm.onReceiverPhoneChanged("0987654321")
        vm.onPickupAddressChanged("Hồ Chí Minh")
        vm.onDeliveryAddressChanged("Thủ Đức")
        vm.onPackageNameChanged("Sách")
        vm.onWeightChanged("NaN")
        assertFalse(vm.validateForm())
        assertNotNull(vm.uiState.value.formError)
    }

    private fun address(lat: Double, lng: Double) = AddressSuggestion(null, "Địa chỉ kiểm thử", null, null,
        null, null, "Hồ Chí Minh", "Vietnam", lat, lng)
}
