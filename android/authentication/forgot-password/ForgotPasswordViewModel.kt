package com.indoone.authentication.forgotpassword

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ForgotPasswordViewModel : ViewModel() {
    private val _state = MutableStateFlow(ForgotPasswordState())
    val state: StateFlow<ForgotPasswordState> = _state.asStateFlow()

    fun updateIdentifier(value: String) {
        _state.value = _state.value.copy(identifier = value, errorMessage = null)
    }

    fun updateOtp(value: String) {
        _state.value = _state.value.copy(otp = value.filter(Char::isDigit).take(6), errorMessage = null)
    }

    fun requestOtp() {
        _state.value = _state.value.copy(otpSent = true, errorMessage = null)
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}
