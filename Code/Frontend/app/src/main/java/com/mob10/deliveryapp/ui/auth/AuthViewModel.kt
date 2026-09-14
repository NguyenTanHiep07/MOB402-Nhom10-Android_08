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
    // TH - tuần 3,4: true khi app đang khởi tạo và restore session
    val isInitializing: Boolean = true,
    // TH - tuần 3,4: true khi đang đăng nhập hoặc đăng xuất
    val isAuthenticating: Boolean = false,
    // TH - tuần 2-4: null về login; có user thì mở home theo role
    val currentUser: UserEntity? = null,
    val errorMessage: String? = null,
    val registrationSuccess: Boolean = false
)

// TH - tuần 2-4: giữ và xử lý trạng thái đăng nhập cho giao diện
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
            authRepository.sessionExpired.collect { expired ->
                if (expired) {
                    userRepository.logout()
                    _uiState.value = AuthUiState(isInitializing = false,
                        errorMessage = "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.")
                }
            }
        }
        // TH - tuần 3,4: khởi tạo dữ liệu rồi restore session
        viewModelScope.launch {
            // TH - tuần 3,4: room phải sẵn sàng trước khi đọc user cũ
            val databaseResult = runCatching { initializeDatabase() }
            if (databaseResult.isFailure) {
                _uiState.value = _uiState.value.copy(
                    isInitializing = false,
                    errorMessage = "Không thể khởi tạo dữ liệu tài khoản."
                )
                return@launch
            }

            runCatching {
                // TH - phần auth phát triển sau: token hỏng thì về login
                if (!authRepository.isLoggedIn()) {
                    userRepository.logout()
                    null
                } else {
                    // TH - phần auth phát triển sau: token đúng thì restore user
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

    // TH - phần auth phát triển sau: đăng nhập server và cập nhật user
    fun login(phoneNumber: String, password: String) {
        if (_uiState.value.isInitializing) {
            _uiState.value = _uiState.value.copy(errorMessage = "Dữ liệu đang được khởi tạo, vui lòng thử lại.")
            return
        }
        // TH - tuần 3,4: chặn bấm đăng nhập nhiều lần
        if (_uiState.value.isAuthenticating) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isAuthenticating = true,
                errorMessage = null
            )
            when (val result = authRepository.login(phoneNumber.trim(), password)) {
                is NetworkResult.Success -> {
                    // TH - phần auth phát triển sau: đổi user server và lưu session
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
                    // TH - phần auth phát triển sau: hiện lỗi mạng hoặc server
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

    fun register(phoneNumber: String, password: String, fullName: String) {
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
            when (val result = authRepository.register(phoneNumber.trim(), password, fullName.trim())) {
                is NetworkResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isAuthenticating = false,
                        errorMessage = null,
                        registrationSuccess = true
                    )
                }
                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isAuthenticating = false,
                        errorMessage = result.message
                    )
                }
                is NetworkResult.Empty -> {
                    _uiState.value = _uiState.value.copy(
                        isAuthenticating = false,
                        errorMessage = "Máy chủ không trả về thông tin đăng ký."
                    )
                }
                is NetworkResult.Loading -> Unit
            }
        }
    }

    fun clearRegistrationSuccess() {
        _uiState.value = _uiState.value.copy(registrationSuccess = false)
    }

    fun syncProfile(profile: com.mob10.deliveryapp.data.remote.api.AccountProfile) {
        val user = _uiState.value.currentUser ?: return
        if (user.id.toLong() != profile.id) return
        val updated = user.copy(username = profile.username, fullName = profile.fullName,
            phoneNumber = profile.phoneNumber, licensePlate = profile.licensePlate)
        if (updated == user) return
        _uiState.value = _uiState.value.copy(currentUser = updated)
        viewModelScope.launch {
            try { userRepository.updateUser(updated) }
            catch (e: kotlinx.coroutines.CancellationException) { throw e }
            catch (_: Exception) { /* Server remains authoritative; the next login refreshes the cache. */ }
        }
    }

    fun clearError() {
        // TH - tuần 2-4: xoá lỗi cũ sau khi giao diện đã hiển thị
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    // TH - tuần 3,4: xoá phiên và quay về màn đăng nhập
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

// TH - phần auth phát triển sau: đổi user server sang user room
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

// TH - tuần 2-4: factory tạo authviewmodel cùng dependency
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
