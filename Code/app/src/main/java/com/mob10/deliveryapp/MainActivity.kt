package com.mob10.deliveryapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.ViewModelProvider
import com.mob10.deliveryapp.data.local.AppDatabase
import com.mob10.deliveryapp.data.local.DatabaseInitializer
import com.mob10.deliveryapp.data.repository.UserRepository
import com.mob10.deliveryapp.ui.DeliveryApp
import com.mob10.deliveryapp.ui.auth.AuthViewModel
import com.mob10.deliveryapp.ui.auth.AuthViewModelFactory

class MainActivity : ComponentActivity() {
    private val orderViewModel: OrderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = AppDatabase.getDatabase(applicationContext)
        val authViewModel = ViewModelProvider(
            this,
            AuthViewModelFactory(
                userRepository = UserRepository(database.userDao()),
                databaseInitializer = DatabaseInitializer(database)
            )
        )[AuthViewModel::class.java]

        setContent {
            MaterialTheme {
                Surface {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "client_home"
                    ) {
                        // 1️⃣ TRANG CHỦ KHÁCH HÀNG
                        // 1️⃣ TRANG CHỦ KHÁCH HÀNG
                        composable("client_home") {
                            ClientHomeScreen(
                                orderViewModel = orderViewModel, // 👈 Thêm dòng này vào là hết lỗi đỏ!
                                onCreateRequestClick = { navController.navigate("create_request") },
                                onOrderListClick = { navController.navigate("order_tracking") }
                            )
                        }

                        // 2️⃣ TẠO YÊU CẦU GIAO HÀNG
                        composable("create_request") {
                            CreateRequestScreen(
                                onNextToConfirm = { sName, sPhone, sAddr, rName, rPhone, rAddr, weight, pType ->
                                    orderViewModel.saveDraftOrder(sName, sPhone, sAddr, rName, rPhone, rAddr, weight, pType)
                                    navController.navigate("confirm_order")
                                }
                            )
                        }

                        // 3️⃣ XÁC NHẬN ĐƠN HÀNG
                        composable("confirm_order") {
                            OrderConfirmationScreen(
                                orderViewModel = orderViewModel,
                                onBackToEdit = { navController.popBackStack() },
                                onConfirmSuccess = {
                                    Toast.makeText(this@MainActivity, "Xác nhận đặt đơn thành công!", Toast.LENGTH_SHORT).show()
                                    navController.navigate("order_tracking") {
                                        popUpTo("client_home") { inclusive = false }
                                    }
                                }
                            )
                        }

                        // 4️⃣ LỊCH SỬ & THEO DÕI ĐƠN HÀNG
                        composable("order_tracking") {
                            OrderTrackingScreen(
                                orderViewModel = orderViewModel,
                                onCreateNewOrder = { navController.navigate("create_request") },
                                onBackToHome = {
                                    navController.popBackStack("client_home", inclusive = false)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}