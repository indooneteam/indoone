package com.indoone.authentication.login

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginViewModel : ViewModel() {
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun updateIdentifier(value: String) {
        _state.value = _state.value.copy(identifier = value, errorMessage = null)
    }

    fun updatePassword(value: String) {
        _state.value = _state.value.copy(password = value, errorMessage = null)
    }

    fun updateOtp(value: String) {
        _state.value = _state.value.copy(
            otp = value.filter(Char::isDigit).take(6),
            errorMessage = null,
        )
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}
