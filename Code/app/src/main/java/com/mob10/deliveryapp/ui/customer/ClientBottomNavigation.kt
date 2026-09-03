package com.mob10.deliveryapp.ui.customer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.mob10.deliveryapp.ui.theme.UthOnSurfaceVariant
import com.mob10.deliveryapp.ui.theme.UthPrimary
import com.mob10.deliveryapp.ui.theme.UthPrimaryContainer
import com.mob10.deliveryapp.ui.theme.UthSurface

@Composable
fun ClientBottomNavigation(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val items = listOf(
        "Trang chủ" to Icons.Default.Home,
        "Đơn hàng" to Icons.Default.ListAlt,
        "Theo dõi" to Icons.Default.LocalShipping,
        "Hồ sơ" to Icons.Default.Person
    )
    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = UthSurface,
            shadowElevation = 8.dp
        ) {
            NavigationBar(
                containerColor = Color.Transparent,
                tonalElevation = 0.dp
            ) {
                items.forEachIndexed { index, (label, icon) ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { onTabSelected(index) },
                        icon = { Icon(icon, contentDescription = label) },
                        label = {
                            Text(
                                label,
                                fontSize = 11.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = UthPrimary,
                            selectedTextColor = UthPrimary,
                            indicatorColor = UthPrimaryContainer,
                            unselectedIconColor = UthOnSurfaceVariant,
                            unselectedTextColor = UthOnSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}
