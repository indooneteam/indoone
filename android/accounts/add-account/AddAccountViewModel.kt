package com.indoone.accounts.addaccount

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Holds the Add Account method-selection state.
 */
class AddAccountViewModel : ViewModel() {
    private val _state = MutableStateFlow(AddAccountState())

    val state: StateFlow<AddAccountState> = _state.asStateFlow()

    fun updateOtpUri(value: String) {
        _state.value = _state.value.copy(
            otpUri = value,
            isImportEnabled = value.trim().startsWith("otpauth://"),
        )
    }
}
