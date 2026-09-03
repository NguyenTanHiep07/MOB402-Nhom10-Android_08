package com.mob10.deliveryapp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mob10.deliveryapp.data.local.DatabaseInitializer
import com.mob10.deliveryapp.data.local.entity.UserEntity
import com.mob10.deliveryapp.data.model.Role
import com.mob10.deliveryapp.data.remote.dto.UserSummaryDto
import com.mob10.deliveryapp.data.repository.AuthRepository
import com.mob10.deliveryapp.data.repository.UserRepository
import com.mob10.deliveryapp.data.util.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isInitializing: Boolean = true,
    val isAuthenticating: Boolean = false,
    val currentUser: UserEntity? = null,
    val errorMessage: String? = null
)

class AuthViewModel(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val initializeDatabase: suspend () -> Unit
) : ViewModel() {
    constructor(
        userRepository: UserRepository,
        authRepository: AuthRepository,
        databaseInitializer: DatabaseInitializer
    ) : this(userRepository, authRepository, databaseInitializer::initialize)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val databaseResult = runCatching { initializeDatabase() }
            if (databaseResult.isFailure) {
                _uiState.value = _uiState.value.copy(
                    isInitializing = false,
                    errorMessage = "Không thể khởi tạo dữ liệu tài khoản."
                )
                return@launch
            }

            runCatching {
                if (!authRepository.isLoggedIn()) {
                    userRepository.logout()
                    null
                } else {
                    userRepository.restoreSession().also { restoredUser ->
                        if (restoredUser == null) authRepository.logout()
                    }
                }
            }
                .onSuccess { restoredUser ->
                    _uiState.value = _uiState.value.copy(
                        isInitializing = false,
                        currentUser = restoredUser,
                        errorMessage = null
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isInitializing = false,
                        currentUser = null,
                        errorMessage = "Không thể khôi phục phiên đăng nhập."
                    )
                }
        }
    }

    fun login(username: String, password: String) {
        if (_uiState.value.isInitializing) {
            _uiState.value = _uiState.value.copy(errorMessage = "Dữ liệu đang được khởi tạo, vui lòng thử lại.")
            return
        }
        if (_uiState.value.isAuthenticating) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isAuthenticating = true,
                errorMessage = null
            )
            when (val result = authRepository.login(username.trim(), password)) {
                is NetworkResult.Success -> {
                    runCatching {
                        result.data.user.toLocalUser().also { userRepository.saveAuthenticatedUser(it) }
                    }.onSuccess { user ->
                        _uiState.value = _uiState.value.copy(
                            isAuthenticating = false,
                            currentUser = user,
                            errorMessage = null
                        )
                    }.onFailure {
                        authRepository.logout()
                        _uiState.value = _uiState.value.copy(
                            isAuthenticating = false,
                            currentUser = null,
                            errorMessage = "Thông tin tài khoản từ máy chủ không hợp lệ."
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isAuthenticating = false,
                        currentUser = null,
                        errorMessage = result.message
                    )
                }
                is NetworkResult.Empty -> {
                    _uiState.value = _uiState.value.copy(
                        isAuthenticating = false,
                        currentUser = null,
                        errorMessage = "Máy chủ không trả về thông tin đăng nhập."
                    )
                }
                is NetworkResult.Loading -> Unit
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
        if (_uiState.value.isInitializing || _uiState.value.isAuthenticating) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isAuthenticating = true,
                errorMessage = null
            )
            runCatching {
                authRepository.logout()
                userRepository.logout()
            }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isAuthenticating = false,
                        currentUser = null,
                        errorMessage = null
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isAuthenticating = false,
                        errorMessage = "Không thể đăng xuất, vui lòng thử lại."
                    )
                }
        }
    }
}

private fun UserSummaryDto.toLocalUser(): UserEntity {
    val localId = id.toInt()
    require(localId > 0 && localId.toLong() == id) { "Mã tài khoản không hợp lệ" }
    return UserEntity(
        id = localId,
        username = username,
        password = "",
        fullName = fullName.orEmpty().ifBlank { username },
        phoneNumber = phoneNumber.orEmpty(),
        role = Role.valueOf(role),
        licensePlate = licensePlate
    )
}

class AuthViewModelFactory(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val databaseInitializer: DatabaseInitializer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(userRepository, authRepository, databaseInitializer) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
