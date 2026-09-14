package com.mob10.deliveryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.mob10.deliveryapp.data.local.AppDatabase
import com.mob10.deliveryapp.data.local.DatabaseInitializer
import com.mob10.deliveryapp.data.repository.UserRepository
import com.mob10.deliveryapp.data.repository.AuthRepository
import com.mob10.deliveryapp.data.remote.RetrofitClient
import com.mob10.deliveryapp.data.session.DataStoreSessionStorage
import com.mob10.deliveryapp.ui.DeliveryApp
import com.mob10.deliveryapp.ui.auth.AuthViewModel
import com.mob10.deliveryapp.ui.auth.AuthViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // TH - tuần 3,4: tạo room và datastore cho auth/session
        val database = AppDatabase.getDatabase(applicationContext)
        val sessionStorage = DataStoreSessionStorage(applicationContext)
        RetrofitClient.init(applicationContext)

        // TH - tuần 3,4: nối userdao và session vào repository dùng chung
        val userRepository = UserRepository(
            userDao = database.userDao(),
            sessionStorage = sessionStorage
        )
        // TH - tuần 2-4: tạo authviewmodel dùng chung cho toàn ứng dụng
        val authViewModel = ViewModelProvider(
            this,
            AuthViewModelFactory(
                userRepository = userRepository,
                authRepository = AuthRepository(
                    authApi = RetrofitClient.authApi,
                    tokenManager = RetrofitClient.getTokenManager()
                ),
                databaseInitializer = DatabaseInitializer(database)
            )
        )[AuthViewModel::class.java]

        // TH - tuần 2: mở giao diện chính và truyền authviewmodel vào
        setContent { DeliveryApp(authViewModel) }
    }
}
