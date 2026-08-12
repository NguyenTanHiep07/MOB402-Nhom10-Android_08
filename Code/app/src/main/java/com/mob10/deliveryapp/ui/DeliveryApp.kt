package com.mob10.deliveryapp.ui


import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.mob10.deliveryapp.data.model.Role
import com.mob10.deliveryapp.ui.admin.AdminHomeScreen
import com.mob10.deliveryapp.ui.customer.CustomerHomeScreen
import com.mob10.deliveryapp.ui.driver.DriverHomeScreen
import com.mob10.deliveryapp.ui.auth.AuthViewModel
import com.mob10.deliveryapp.ui.auth.LoginScreen
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
        when (currentUser?.role) {
            Role.ADMIN -> AdminHomeScreen(adminName = currentUser?.fullName.orEmpty())
            Role.DELIVERY -> DriverHomeScreen(driverName = currentUser?.fullName.orEmpty())
            Role.CLIENT -> CustomerHomeScreen(customerName = currentUser?.fullName.orEmpty())
            else -> {
                LoginScreen(
                    onLogin = authViewModel::login
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
