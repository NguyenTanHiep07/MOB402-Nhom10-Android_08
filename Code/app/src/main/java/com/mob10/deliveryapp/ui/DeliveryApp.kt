package com.mob10.deliveryapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.mob10.deliveryapp.data.model.Role
import com.mob10.deliveryapp.ui.navigation.AppDestination
import com.mob10.deliveryapp.ui.navigation.destinationFor
import com.mob10.deliveryapp.ui.theme.Android08Theme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun DeliveryApp(authViewModel: AuthViewModel) {
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val currentUser = authState.currentUser
    val context = LocalContext.current
    val navigation = rememberNavController()
    val destination = destinationFor(currentUser?.role).name
    LaunchedEffect(destination, currentUser?.id, authState.isInitializing) {
        if (!authState.isInitializing && navigation.currentDestination?.route != destination) {
            navigation.navigate(destination) {
                popUpTo(navigation.graph.id) { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    Android08Theme {
        androidx.compose.runtime.CompositionLocalProvider(
            com.mob10.deliveryapp.ui.auth.LocalAccountUpdated provides authViewModel::syncProfile
        ) {
        // Each signed-in destination owns its ViewModels. Logout removes its back stack entry,
        // cancelling requests and discarding expired-session flags and cached data.
        NavHost(navController = navigation, startDestination = AppDestination.LOGIN.name) {
            composable(AppDestination.ADMIN_HOME.name) {
                if (currentUser?.role != Role.ADMIN) return@composable
                val adminViewModel: AdminViewModel = viewModel(
                    factory = AdminViewModelFactory(context.applicationContext)
                )
                AdminHomeScreen(
                    adminName = currentUser.fullName,
                    viewModel = adminViewModel,
                    onLogout = authViewModel::logout
                )
            }
            composable(AppDestination.DELIVERY_HOME.name) {
                if (currentUser?.role != Role.DELIVERY) return@composable
                DriverHomeScreen(
                    currentUser = currentUser,
                    onLogout = authViewModel::logout
                )
            }
            composable(AppDestination.CLIENT_HOME.name) {
                if (currentUser?.role != Role.CLIENT) return@composable
                ClientFeatureFlow(
                    currentUser = currentUser,
                    onLogout = authViewModel::logout
                )
            }
            composable("ACCOUNT_RECOVERY") {
                com.mob10.deliveryapp.ui.auth.RecoveryScreen(onBack = { navigation.popBackStack() })
            }
            composable(AppDestination.LOGIN.name) {
                XmlLoginScreen(
                    onLogin = authViewModel::login,
                    onForgotPassword = { navigation.navigate("ACCOUNT_RECOVERY") },
                    isLoading = authState.isInitializing || authState.isAuthenticating,
                    errorMessage = authState.errorMessage
                )
            }
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
