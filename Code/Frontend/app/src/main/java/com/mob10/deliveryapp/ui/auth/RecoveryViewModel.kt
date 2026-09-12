package com.mob10.deliveryapp.ui.auth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mob10.deliveryapp.data.repository.AccountRepository
import com.mob10.deliveryapp.data.remote.api.RecoveryReset
import com.mob10.deliveryapp.data.util.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RecoveryState(val busy: Boolean = false, val message: String? = null, val error: String? = null, val success: Boolean = false)
class RecoveryViewModel(private val saved: SavedStateHandle) : ViewModel() {
    private val repository = AccountRepository()
    private val mutable = MutableStateFlow(RecoveryState())
    val state = mutable.asStateFlow()
    val phone = saved.getStateFlow("phone", "")
    val codeStep = saved.getStateFlow("codeStep", false)
    val resendAt = saved.getStateFlow("resendAt", 0L)
    fun phone(value: String) { saved["phone"] = value.take(20) }
    fun step(value: Boolean) { if (!mutable.value.busy) { saved["codeStep"] = value; mutable.value = RecoveryState() } }
    fun request() {
        if (mutable.value.busy || System.currentTimeMillis() < resendAt.value) return
        viewModelScope.launch {
            mutable.value = RecoveryState(busy = true)
            when (val result = repository.request(phone.value.trim())) {
                is NetworkResult.Success -> {
                    saved["codeStep"] = true; saved["resendAt"] = System.currentTimeMillis() + 60_000
                    mutable.value = RecoveryState(message = result.data.message)
                }
                is NetworkResult.Error -> mutable.value = RecoveryState(error = result.message)
                else -> mutable.value = RecoveryState(error = "Không nhận được phản hồi. Hãy thử lại.")
            }
        }
    }
    fun reset(code: String, password: String, confirm: String) {
        if (mutable.value.busy) return
        if (!code.matches(Regex("[0-9]{6}")) || password.length !in 12..64 || password.toByteArray().size > 72 || !password.any(Char::isLetter) || !password.any(Char::isDigit) || password != confirm) {
            mutable.value = RecoveryState(error = "Nhập mã 6 số. Mật khẩu cần 12–64 ký tự, có chữ và số; hai lần nhập phải giống nhau."); return
        }
        viewModelScope.launch {
            mutable.value = RecoveryState(busy = true)
            when (val result = repository.reset(RecoveryReset(phone.value.trim(), code, password))) {
                is NetworkResult.Success -> mutable.value = RecoveryState(success = true, message = result.data.message)
                is NetworkResult.Error -> mutable.value = RecoveryState(error = result.message)
                else -> mutable.value = RecoveryState(error = "Không nhận được phản hồi. Hãy thử đăng nhập bằng mật khẩu mới trước khi gửi lại.")
            }
        }
    }
}
