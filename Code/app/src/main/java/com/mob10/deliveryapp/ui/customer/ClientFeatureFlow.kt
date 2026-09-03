package com.mob10.deliveryapp.ui.customer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mob10.deliveryapp.ClientHomeScreen
import com.mob10.deliveryapp.CreateRequestScreen
import com.mob10.deliveryapp.CreateRequestViewModel
import com.mob10.deliveryapp.OrderConfirmationScreen
import com.mob10.deliveryapp.OrderTrackingScreen
import com.mob10.deliveryapp.OrderViewModel
import com.mob10.deliveryapp.OrderViewModelFactory
import com.mob10.deliveryapp.data.local.entity.UserEntity


private enum class ClientScreen { HOME, CREATE_REQUEST, CONFIRMATION, ORDERS, TRACKING, PROFILE, }

@Composable
fun ClientFeatureFlow(currentUser: UserEntity, onLogout: () -> Unit) {
    val context = LocalContext.current
    val orderFactory = remember(currentUser.id) {
        OrderViewModelFactory(context.applicationContext, currentUser.id)
    }
    val orderViewModel: OrderViewModel = viewModel(
        key = "client_orders_${currentUser.id}",
        factory = orderFactory
    )
    val requestViewModel: CreateRequestViewModel = viewModel(
        key = "client_request_${currentUser.id}"
    )
    var destinationName by rememberSaveable(currentUser.id) { mutableStateOf(ClientScreen.HOME.name) }
    val destination = ClientScreen.valueOf(destinationName)

    when (destination) {
        ClientScreen.HOME -> ClientHomeScreen(
            customerName = currentUser.fullName,
            orderViewModel = orderViewModel,
            onCreateRequestClick = { destinationName = ClientScreen.CREATE_REQUEST.name },
            onOrderListClick = { destinationName = ClientScreen.ORDERS.name },
            onTrackingClick = { destinationName = ClientScreen.TRACKING.name },
            onProfileClick = { destinationName = ClientScreen.PROFILE.name },
            onLogout = onLogout
        )
        ClientScreen.CREATE_REQUEST -> CreateRequestScreen(
            viewModel = requestViewModel,
            onBack = { destinationName = ClientScreen.HOME.name },
            onContinueToConfirmation = {
                orderViewModel.saveDraftOrder(requestViewModel.uiState.value)
                destinationName = ClientScreen.CONFIRMATION.name
            }
        )
        ClientScreen.CONFIRMATION -> OrderConfirmationScreen(
            orderViewModel = orderViewModel,
            onBackToEdit = { destinationName = ClientScreen.CREATE_REQUEST.name },
            onConfirmSuccess = {
                requestViewModel.reset()
                destinationName = ClientScreen.ORDERS.name
            }
        )
        ClientScreen.ORDERS -> OrderTrackingScreen(
            orderViewModel = orderViewModel,
            onCreateNewOrder = { destinationName = ClientScreen.CREATE_REQUEST.name },
            onBackToHome = { destinationName = ClientScreen.HOME.name },
            selectedTab = 1,
            onTabSelected = { tab -> destinationName = clientScreenForTab(tab).name }
        )
        ClientScreen.TRACKING -> OrderTrackingScreen(
            orderViewModel = orderViewModel,
            onCreateNewOrder = { destinationName = ClientScreen.CREATE_REQUEST.name },
            onBackToHome = { destinationName = ClientScreen.HOME.name },
            title = "Theo dõi đơn hàng",
            activeOnly = true,
            showCreateButton = false,
            selectedTab = 2,
            onTabSelected = { tab -> destinationName = clientScreenForTab(tab).name }
        )

        ClientScreen.PROFILE -> ClientProfileScreen(
            currentUser = currentUser,
            onBack = { destinationName = ClientScreen.HOME.name },
            onLogout = onLogout,
            onTabSelected = { tab -> destinationName = clientScreenForTab(tab).name }
        )
    }
}

private fun clientScreenForTab(tab: Int): ClientScreen = when (tab) {
    1 -> ClientScreen.ORDERS
    2 -> ClientScreen.TRACKING
    3 -> ClientScreen.PROFILE
    else -> ClientScreen.HOME
}
