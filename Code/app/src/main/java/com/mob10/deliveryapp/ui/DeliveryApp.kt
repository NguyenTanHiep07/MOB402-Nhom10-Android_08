package com.mob10.deliveryapp.ui

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mob10.deliveryapp.data.local.AppDatabase
import com.mob10.deliveryapp.data.repository.DeliveryRepository
import com.mob10.deliveryapp.data.repository.UserRepository
import com.mob10.deliveryapp.ui.admin.AdminHomeScreen
import com.mob10.deliveryapp.ui.admin.AdminViewModel
import com.mob10.deliveryapp.ui.auth.AuthViewModel
import com.mob10.deliveryapp.ui.auth.LoginScreen
import com.mob10.deliveryapp.ui.auth.LoginState
import com.mob10.deliveryapp.ui.customer.CustomerHomeScreen
import com.mob10.deliveryapp.ui.customer.CustomerViewModel
import com.mob10.deliveryapp.ui.driver.DriverHomeScreen
import com.mob10.deliveryapp.ui.driver.DriverViewModel
import com.mob10.deliveryapp.ui.theme.Android08Theme

@Composable
fun DeliveryApp() {
    val context = LocalContext.current

    // ── Khởi tạo DB + Repositories ─────────────────────────────────────────
    val db = remember { AppDatabase.getDatabase(context) }
    val userRepository = remember { UserRepository(db.userDao()) }
    val deliveryRepository = remember {
        DeliveryRepository(
            db = db,
            requestDao = db.deliveryRequestDao(),
            packageDao = db.packageDao(),
            historyDao = db.statusHistoryDao()
        )
    }

    // Tự động seed dữ liệu mẫu nếu DB trống
    LaunchedEffect(Unit) {
        com.mob10.deliveryapp.data.local.DatabaseInitializer(db).initialize()
    }


    // ── AuthViewModel ────────────────────────────────────────────────────────
    val authViewModel: AuthViewModel = viewModel(
        factory = viewModelFactory {
            initializer { AuthViewModel(userRepository) }
        }
    )
    val loginState by authViewModel.loginState.collectAsState()

    Android08Theme {
        when (val state = loginState) {
            is LoginState.Success -> {
                val user = state.user
                when (user.role.name) {
                    "ADMIN" -> {
                        val adminViewModel: AdminViewModel = viewModel(
                            key = "admin_${user.id}",
                            factory = viewModelFactory {
                                initializer { AdminViewModel(deliveryRepository, userRepository) }
                            }
                        )
                        AdminHomeScreen(
                            adminName = user.fullName,
                            viewModel = adminViewModel
                        )
                    }
                    "DELIVERY" -> {
                        val driverViewModel: DriverViewModel = viewModel(
                            key = "driver_${user.id}",
                            factory = viewModelFactory {
                                initializer { DriverViewModel(deliveryRepository, user.id) }
                            }
                        )
                        DriverHomeScreen(
                            driverName = user.fullName,
                            viewModel = driverViewModel
                        )
                    }
                    else -> { // CLIENT
                        val customerViewModel: CustomerViewModel = viewModel(
                            key = "customer_${user.id}",
                            factory = viewModelFactory {
                                initializer { CustomerViewModel(deliveryRepository, user.id) }
                            }
                        )
                        CustomerHomeScreen(
                            customerName = user.fullName,
                            viewModel = customerViewModel
                        )
                    }
                }
            }
            is LoginState.Error -> {
                LaunchedEffect(state) {
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                    authViewModel.resetState()
                }
                LoginScreen(onLogin = { username, password ->
                    authViewModel.login(username, password)
                })
            }
            is LoginState.Loading -> {
                LoginScreen(onLogin = { _, _ -> })
            }
            is LoginState.Idle -> {
                LoginScreen(onLogin = { username, password ->
                    authViewModel.login(username, password)
                })
            }
        }
    }
}
