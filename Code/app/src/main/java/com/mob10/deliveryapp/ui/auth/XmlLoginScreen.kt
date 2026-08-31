package com.mob10.deliveryapp.ui.auth

import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.mob10.deliveryapp.R

/**
 * Login runtime sử dụng layout XML theo yêu cầu bàn giao của feature/auth-navigation.
 * AndroidView chỉ làm cầu nối vào cây Compose hiện tại; xác thực vẫn đi qua AuthViewModel.
 */
@Composable
fun XmlLoginScreen(
    onLogin: (username: String, password: String) -> Unit,
    onForgotPassword: () -> Unit = {},
    isLoading: Boolean = false
) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            FrameLayout(context).apply {
                LayoutInflater.from(context).inflate(R.layout.screen_login, this, true)
            }
        },
        update = { root ->
            val usernameInput = root.findViewById<TextInputEditText>(R.id.usernameInput)
            val passwordInput = root.findViewById<TextInputEditText>(R.id.passwordInput)
            val validationError = root.findViewById<TextView>(R.id.loginValidationError)
            val loginButton = root.findViewById<MaterialButton>(R.id.loginButton)
            val forgotPassword = root.findViewById<TextView>(R.id.forgotPasswordAction)

            loginButton.isEnabled = !isLoading
            loginButton.setText(
                if (isLoading) R.string.login_initializing else R.string.login_button
            )
            loginButton.setOnClickListener {
                val username = usernameInput.text?.toString()?.trim().orEmpty()
                val password = passwordInput.text?.toString().orEmpty()
                if (username.isBlank() || password.isBlank()) {
                    validationError.visibility = View.VISIBLE
                } else {
                    validationError.visibility = View.GONE
                    onLogin(username, password)
                }
            }
            forgotPassword.setOnClickListener { onForgotPassword() }
        }
    )
}
