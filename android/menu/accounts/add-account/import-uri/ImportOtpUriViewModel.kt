package com.indoone.accounts.addaccount.importuri

import androidx.lifecycle.ViewModel
import com.indoone.accounts.addaccount.scanqr.QrAccountResult
import com.indoone.accounts.addaccount.scanqr.QrOtpAuthParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Owns OTPAuth URI input and converts a valid URI into account details.
 */
class ImportOtpUriViewModel : ViewModel() {
    private val _state = MutableStateFlow(ImportOtpUriState())

    val state: StateFlow<ImportOtpUriState> = _state.asStateFlow()

    fun onUriChanged(value: String) {
        _state.value = ImportOtpUriState(
            uri = value,
            errorMessage = null,
        )
    }

    fun parse(): Result<QrAccountResult> {
        val value = _state.value.uri.trim()

        if (value.isBlank()) {
            val error = IllegalArgumentException("Paste an OTPAUTH URI first.")
            _state.value = _state.value.copy(errorMessage = error.message)
            return Result.failure(error)
        }

        val result = QrOtpAuthParser.parse(value)

        result.exceptionOrNull()?.let { error ->
            _state.value = _state.value.copy(
                errorMessage = "Invalid TOTP OTPAUTH data. Check the URI and try again.",
            )
        } ?: run {
            _state.value = _state.value.copy(errorMessage = null)
        }

        return result
    }
}
