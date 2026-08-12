package com.mob10.deliveryapp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mob10.deliveryapp.data.local.entity.UserEntity
import com.mob10.deliveryapp.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LoginState {
    data object Idle : LoginState()
    data object Loading : LoginState()
    data class Success(val user: UserEntity) : LoginState()
    data class Error(val message: String) : LoginState()
}

class AuthViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    fun login(phoneNumber: String, password: String) {
        if (phoneNumber.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("Vui lòng nhập đầy đủ thông tin")
            return
        }
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val user = userRepository.login(phoneNumber.trim(), password)
            _loginState.value = if (user != null) {
                LoginState.Success(user)
            } else {
                LoginState.Error("Số điện thoại hoặc mật khẩu không đúng")
            }
        }
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}
