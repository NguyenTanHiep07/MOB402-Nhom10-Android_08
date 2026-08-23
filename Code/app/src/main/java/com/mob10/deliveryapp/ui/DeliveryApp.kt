package com.mob10.deliveryapp.ui

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mob10.deliveryapp.ui.admin.AdminHomeScreen
import com.mob10.deliveryapp.ui.admin.AdminViewModel
import com.mob10.deliveryapp.ui.admin.AdminViewModelFactory
import com.mob10.deliveryapp.ui.customer.ClientFeatureFlow
import com.mob10.deliveryapp.ui.driver.DriverHomeScreen
import com.mob10.deliveryapp.ui.auth.AuthViewModel
import com.mob10.deliveryapp.ui.auth.LoginScreen
import com.mob10.deliveryapp.ui.auth.XmlLoginScreen
import com.mob10.deliveryapp.ui.navigation.AppDestination
import com.mob10.deliveryapp.ui.navigation.destinationFor
import com.mob10.deliveryapp.ui.theme.Android08Theme

@Composable
fun DeliveryApp(authViewModel: AuthViewModel) {
    val authState by authViewModel.uiState.collectAsState()
    val currentUser = authState.currentUser
    val context = LocalContext.current

    authState.errorMessage?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            authViewModel.clearError()
        }
    }

    Android08Theme {
        when (destinationFor(currentUser?.role)) {
            AppDestination.ADMIN_HOME -> {
                val adminViewModel: AdminViewModel = viewModel(
                    factory = AdminViewModelFactory(context.applicationContext)
                )
                AdminHomeScreen(
                    adminName = currentUser?.fullName.orEmpty(),
                    viewModel = adminViewModel,
                    onLogout = authViewModel::logout
                )
            }
            AppDestination.DELIVERY_HOME -> DriverHomeScreen(
                currentUser = currentUser,
                onLogout = authViewModel::logout,
                onUpdateProfile = authViewModel::updateProfile
            )
            AppDestination.CLIENT_HOME -> currentUser?.let { user ->
                ClientFeatureFlow(
                    currentUser = user,
                    onLogout = authViewModel::logout
                )
            }
            AppDestination.LOGIN -> {
                XmlLoginScreen(
                    onLogin = authViewModel::login,
                    isLoading = authState.isInitializing
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DeliveryAppPreview() {
    Android08Theme {
        LoginScreen()
    }
}
