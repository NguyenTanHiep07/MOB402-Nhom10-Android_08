package com.mob10.deliveryapp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mob10.deliveryapp.data.local.DatabaseInitializer
import com.mob10.deliveryapp.data.local.entity.UserEntity
import com.mob10.deliveryapp.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isInitializing: Boolean = true,
    val currentUser: UserEntity? = null,
    val errorMessage: String? = null
)

class AuthViewModel(
    private val userRepository: UserRepository,
    private val databaseInitializer: DatabaseInitializer
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching { databaseInitializer.initialize() }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isInitializing = false)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isInitializing = false,
                        errorMessage = "Không thể khởi tạo dữ liệu tài khoản."
                    )
                }
        }
    }

    fun login(phoneNumber: String, password: String) {
        if (_uiState.value.isInitializing) {
            _uiState.value = _uiState.value.copy(errorMessage = "Dữ liệu đang được khởi tạo, vui lòng thử lại.")
            return
        }

        viewModelScope.launch {
            val user = userRepository.login(phoneNumber, password)
            _uiState.value = if (user == null) {
                _uiState.value.copy(errorMessage = "Số điện thoại hoặc mật khẩu không đúng.")
            } else {
                _uiState.value.copy(currentUser = user, errorMessage = null)
            }
        }
    }

    fun updateProfile(fullName: String, phoneNumber: String, username: String, licensePlate: String) {
        val currentUser = _uiState.value.currentUser ?: return
        val updatedUser = currentUser.copy(
            fullName = fullName,
            phoneNumber = phoneNumber,
            username = username,
            licensePlate = licensePlate
        )
        viewModelScope.launch {
            try {
                userRepository.updateUser(updatedUser)
                _uiState.value = _uiState.value.copy(currentUser = updatedUser, errorMessage = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Không thể cập nhật hồ sơ. Tên đăng nhập hoặc SĐT có thể đã tồn tại.")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun logout() {
        _uiState.value = _uiState.value.copy(currentUser = null, errorMessage = null)
    }
}

class AuthViewModelFactory(
    private val userRepository: UserRepository,
    private val databaseInitializer: DatabaseInitializer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(userRepository, databaseInitializer) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
