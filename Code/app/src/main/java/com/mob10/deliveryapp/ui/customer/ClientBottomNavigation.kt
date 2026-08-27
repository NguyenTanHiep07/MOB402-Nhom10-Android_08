package com.mob10.deliveryapp.ui.customer

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ClientBottomNavigation(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val items = listOf(
        "Trang chủ" to Icons.Default.Home,
        "Đơn hàng" to Icons.Default.ListAlt,
        "Theo dõi" to Icons.Default.LocalShipping,
        "Hồ sơ" to Icons.Default.Person
    )
    NavigationBar {
        items.forEachIndexed { index, (label, icon) ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) }
            )
        }
    }
}
