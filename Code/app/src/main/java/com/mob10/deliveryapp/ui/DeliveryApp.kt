package com.mob10.deliveryapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.mob10.deliveryapp.ui.auth.LoginScreen
import com.mob10.deliveryapp.ui.theme.Android08Theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.mob10.deliveryapp.ui.admin.AdminHomeScreen
import com.mob10.deliveryapp.ui.driver.DriverHomeScreen
import com.mob10.deliveryapp.ui.customer.CustomerHomeScreen

@Composable
fun DeliveryApp() {
    var userRole by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    Android08Theme {
        when (userRole) {
            "admin" -> AdminHomeScreen(adminName = "Sếp")
            "driver" -> DriverHomeScreen(driverName = "Nguyễn Văn A")
            "customer" -> CustomerHomeScreen(customerName = "Khách hàng VIP")
            else -> {
                LoginScreen(
                    onLogin = { phone, password ->
                        if (password == "12345678") {
                            when (phone) {
                                "0987654321" -> userRole = "admin"
                                "0912345678" -> userRole = "driver"
                                "0900000000" -> userRole = "customer"
                                else -> Toast.makeText(context, "Tài khoản không tồn tại!", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Sai mật khẩu!", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DeliveryAppPreview() {
    DeliveryApp()
}
