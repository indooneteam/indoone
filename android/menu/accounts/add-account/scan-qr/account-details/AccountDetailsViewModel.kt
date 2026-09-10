package com.indoone.accounts.addaccount.scanqr.accountdetails

import androidx.lifecycle.ViewModel
import com.indoone.accounts.addaccount.scanqr.QrAccountResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Owns editable account details after QR decoding.
 */
class AccountDetailsViewModel(
    initialResult: QrAccountResult? = null,
) : ViewModel() {
    private val _state = MutableStateFlow(
        AccountDetailsState(
            name = initialResult?.name.orEmpty(),
            email = initialResult?.email.orEmpty(),
            secret = initialResult?.secret.orEmpty(),
            digits = initialResult?.digits ?: 6,
            period = initialResult?.period ?: 30,
            algorithm = initialResult?.algorithm ?: "SHA1",
        ),
    )

    val state: StateFlow<AccountDetailsState> = _state.asStateFlow()

    fun onNameChanged(value: String) {
        update { copy(name = value, errorMessage = null) }
    }

    fun onEmailChanged(value: String) {
        update { copy(email = value, errorMessage = null) }
    }

    fun onSecretChanged(value: String) {
        update { copy(secret = value, errorMessage = null) }
    }

    fun onDigitsChanged(value: Int) {
        update { copy(digits = value.coerceIn(6, 8), errorMessage = null) }
    }

    fun onPeriodChanged(value: Int) {
        update { copy(period = value.coerceAtLeast(1), errorMessage = null) }
    }

    fun onAlgorithmChanged(value: String) {
        update { copy(algorithm = value.uppercase(), errorMessage = null) }
    }

    fun prepareSave(): Result<AccountSaveRequest> {
        val result = AccountDetailsValidator.validate(_state.value)

        result.exceptionOrNull()?.let { error ->
            onSaveFailed(error.message ?: "Invalid account details.")
        }

        return result
    }

    fun onSaveStarted() {
        update { copy(isSaving = true, errorMessage = null) }
    }

    fun onSaveFailed(message: String) {
        update {
            copy(
                isSaving = false,
                errorMessage = message,
            )
        }
    }

    fun onSaveCompleted() {
        update { copy(isSaving = false, errorMessage = null) }
    }

    private inline fun update(
        transform: AccountDetailsState.() -> AccountDetailsState,
    ) {
        _state.value = _state.value.transform()
    }
}
