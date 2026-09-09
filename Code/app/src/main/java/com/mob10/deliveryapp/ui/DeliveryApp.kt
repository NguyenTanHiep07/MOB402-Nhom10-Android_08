package com.mob10.deliveryapp.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mob10.deliveryapp.data.model.Role
import com.mob10.deliveryapp.ui.admin.AdminHomeScreen
import com.mob10.deliveryapp.ui.admin.AdminViewModel
import com.mob10.deliveryapp.ui.admin.AdminViewModelFactory
import com.mob10.deliveryapp.ui.auth.AuthViewModel
import com.mob10.deliveryapp.ui.auth.LocalAccountUpdated
import com.mob10.deliveryapp.ui.auth.LoginScreen
import com.mob10.deliveryapp.ui.auth.RecoveryScreen
import com.mob10.deliveryapp.ui.customer.ClientFeatureFlow
import com.mob10.deliveryapp.ui.driver.DriverHomeScreen
import com.mob10.deliveryapp.ui.theme.Android08Theme

@Composable
fun DeliveryApp(authViewModel: AuthViewModel) {
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val currentUser = authState.currentUser
    val context = LocalContext.current
    var showRecovery by rememberSaveable { mutableStateOf(false) }
    var showRegister by rememberSaveable { mutableStateOf(false) }

    Android08Theme {
        androidx.compose.runtime.CompositionLocalProvider(
            LocalAccountUpdated provides authViewModel::syncProfile
        ) {
            androidx.compose.material3.Surface(
                modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                color = androidx.compose.material3.MaterialTheme.colorScheme.background
            ) {
                if (authState.isNewlyRegistered) {
                    val accountViewModel: com.mob10.deliveryapp.ui.auth.AccountViewModel = viewModel(
                        factory = com.mob10.deliveryapp.ui.auth.AccountViewModelFactory(context.applicationContext, authViewModel::syncProfile)
                    )
                    val accountState by accountViewModel.state.collectAsStateWithLifecycle()
                    com.mob10.deliveryapp.ui.auth.RoleSelectionDialog(
                        onCustomerSelected = { authViewModel.clearNewlyRegistered() },
                        onDriverSelected = { licensePlate ->
                            accountViewModel.submitDriverRequest(licensePlate)
                            authViewModel.clearNewlyRegistered()
                        }
                    )
                    // If there's an error message from accountViewModel, we might want to show it, but for simplicity, we just clear and let the request happen.
                }

                when {
                    currentUser != null -> {
                        when (currentUser.role) {
                            Role.ADMIN -> {
                                val adminViewModel: AdminViewModel = viewModel(
                                    factory = AdminViewModelFactory(context.applicationContext)
                                )
                                AdminHomeScreen(
                                    adminName = currentUser.fullName,
                                    viewModel = adminViewModel,
                                    onLogout = authViewModel::logout
                                )
                            }
                            Role.DELIVERY -> {
                                DriverHomeScreen(
                                    currentUser = currentUser,
                                    onLogout = authViewModel::logout
                                )
                            }
                            Role.CLIENT -> {
                                ClientFeatureFlow(
                                    currentUser = currentUser,
                                    onLogout = authViewModel::logout
                                )
                            }
                        }
                    }
                    showRecovery -> {
                        RecoveryScreen(onBack = { showRecovery = false })
                    }
                    showRegister -> {
                        com.mob10.deliveryapp.ui.auth.RegisterScreen(
                            onRegister = authViewModel::register,
                            onBackToLogin = { showRegister = false },
                            isLoading = authState.isInitializing || authState.isAuthenticating,
                            errorMessage = authState.errorMessage
                        )
                    }
                    else -> {
                        LoginScreen(
                            onLogin = authViewModel::login,
                            onForgotPassword = { showRecovery = true },
                            onNavigateToRegister = { showRegister = true },
                            isLoading = authState.isInitializing || authState.isAuthenticating,
                            errorMessage = authState.errorMessage
                        )
                    }
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
