package com.mob10.deliveryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.mob10.deliveryapp.data.local.AppDatabase
import com.mob10.deliveryapp.data.local.DatabaseInitializer
import com.mob10.deliveryapp.data.repository.UserRepository
import com.mob10.deliveryapp.ui.DeliveryApp
import com.mob10.deliveryapp.ui.auth.AuthViewModel
import com.mob10.deliveryapp.ui.auth.AuthViewModelFactory

class MainActivity : ComponentActivity() {
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
            DeliveryApp(authViewModel = authViewModel)
        }
    }
}
