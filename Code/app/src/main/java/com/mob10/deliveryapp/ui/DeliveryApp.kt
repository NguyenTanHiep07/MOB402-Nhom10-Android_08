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

    Android08Theme {
        androidx.compose.runtime.CompositionLocalProvider(
            LocalAccountUpdated provides authViewModel::syncProfile
        ) {
            androidx.compose.material3.Surface(
                modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                color = androidx.compose.material3.MaterialTheme.colorScheme.background
            ) {
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
                    else -> {
                        LoginScreen(
                            onLogin = authViewModel::login,
                            onForgotPassword = { showRecovery = true },
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
