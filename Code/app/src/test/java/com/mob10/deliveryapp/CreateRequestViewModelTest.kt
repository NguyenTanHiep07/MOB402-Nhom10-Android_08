package com.mob10.deliveryapp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateRequestViewModelTest {
    @Test
    fun `valid request returns express quote`() {
        val viewModel = CreateRequestViewModel()
        viewModel.onSenderNameChanged("A")
        viewModel.onSenderPhoneChanged("0123456789")
        viewModel.onPickupAddressChanged("Điểm lấy")
        viewModel.onReceiverNameChanged("B")
        viewModel.onReceiverPhoneChanged("0987654321")
        viewModel.onDeliveryAddressChanged("Điểm giao")
        viewModel.onWeightChanged("2")
        viewModel.onDistanceChanged("4")
        viewModel.onServiceSelected(PackageType.EXPRESS.displayName)

        assertTrue(viewModel.validateForm())
        assertEquals(46_000, viewModel.uiState.value.feeQuote.totalFee)
    }
}
