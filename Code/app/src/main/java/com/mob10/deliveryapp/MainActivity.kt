package com.mob10.deliveryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
        super.onCreate(savedInstanceState)
        val database = AppDatabase.getDatabase(applicationContext)
        val sessionStorage = DataStoreSessionStorage(applicationContext)
        RetrofitClient.init(applicationContext)
        val userRepository = UserRepository(
            userDao = database.userDao(),
            sessionStorage = sessionStorage
        )
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

        setContent { DeliveryApp(authViewModel) }
    }
}
