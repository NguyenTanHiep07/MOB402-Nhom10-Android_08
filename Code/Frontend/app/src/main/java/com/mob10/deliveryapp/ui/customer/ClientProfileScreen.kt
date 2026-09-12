package com.mob10.deliveryapp.ui.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mob10.deliveryapp.data.local.entity.UserEntity
import com.mob10.deliveryapp.ui.auth.AccountPanel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientProfileScreen(currentUser: UserEntity, onBack: () -> Unit, onLogout: () -> Unit, onTabSelected: (Int) -> Unit) {
    Scaffold(topBar = {
        TopAppBar(title = { Text("Tài khoản") }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Về trang chủ") }
        })
    }, bottomBar = { ClientBottomNavigation(selectedTab = 3, onTabSelected = onTabSelected) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            AccountPanel(currentUser.id)
            OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) { Text("Đăng xuất") }
        }
    }
}
