package com.indoone.authentication.signup

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SignUpViewModel : ViewModel() {
    private val _state = MutableStateFlow(SignUpState())
    val state: StateFlow<SignUpState> = _state.asStateFlow()

    fun updateName(value: String) {
        _state.value = _state.value.copy(name = value, errorMessage = null)
    }

    fun updateIdentifier(value: String) {
        _state.value = _state.value.copy(identifier = value, errorMessage = null)
    }

    fun updatePassword(value: String) {
        _state.value = _state.value.copy(password = value, errorMessage = null)
    }

    fun updateConfirmPassword(value: String) {
        _state.value = _state.value.copy(confirmPassword = value, errorMessage = null)
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}
