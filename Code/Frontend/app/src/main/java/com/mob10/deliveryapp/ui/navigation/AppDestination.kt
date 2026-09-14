package com.mob10.deliveryapp.ui.navigation

import com.mob10.deliveryapp.data.model.Role

// TH - tuần 3,4: contract gồm login và ba màn home theo role
// TH - demo: file này dễ test; runtime hiện rẽ role trong deliveryapp
enum class AppDestination {
    LOGIN,
    CLIENT_HOME,
    DELIVERY_HOME,
    ADMIN_HOME
}

// TH - tuần 3,4: null về login; mỗi role đi đúng home
fun destinationFor(role: Role?): AppDestination = when (role) {
    Role.CLIENT -> AppDestination.CLIENT_HOME
    Role.DELIVERY -> AppDestination.DELIVERY_HOME
    Role.ADMIN -> AppDestination.ADMIN_HOME
    null -> AppDestination.LOGIN
}
