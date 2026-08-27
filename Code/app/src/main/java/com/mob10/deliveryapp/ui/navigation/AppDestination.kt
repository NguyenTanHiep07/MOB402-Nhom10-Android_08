package com.mob10.deliveryapp.ui.navigation

import com.mob10.deliveryapp.data.model.Role

/**
 * Các đích điều hướng cấp ứng dụng sau bước xác thực.
 *
 * Việc ánh xạ Role thành đích riêng giúp luồng đăng nhập không phụ thuộc trực tiếp
 * vào từng Composable và có thể kiểm thử mà không cần khởi chạy Android UI.
 */
enum class AppDestination {
    LOGIN,
    CLIENT_HOME,
    DELIVERY_HOME,
    ADMIN_HOME
}

fun destinationFor(role: Role?): AppDestination = when (role) {
    Role.CLIENT -> AppDestination.CLIENT_HOME
    Role.DELIVERY -> AppDestination.DELIVERY_HOME
    Role.ADMIN -> AppDestination.ADMIN_HOME
    null -> AppDestination.LOGIN
}
