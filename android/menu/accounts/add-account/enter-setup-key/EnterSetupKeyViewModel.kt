package com.indoone.accounts.addaccount.entersetupkey

import androidx.lifecycle.ViewModel
import com.indoone.accounts.addaccount.scanqr.accountdetails.AccountDetailsState
import com.indoone.accounts.addaccount.scanqr.accountdetails.AccountDetailsValidator
import com.indoone.accounts.addaccount.scanqr.accountdetails.AccountRecordMapper
import com.indoone.accounts.addaccount.scanqr.accountdetails.AccountSaveRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Owns manual setup-key form state and validates it before persistence.
 */
class EnterSetupKeyViewModel : ViewModel() {
    private val _state = MutableStateFlow(EnterSetupKeyState())

    val state: StateFlow<EnterSetupKeyState> = _state.asStateFlow()

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
        update { copy(digits = value, errorMessage = null) }
    }

    fun onPeriodChanged(value: Int) {
        update { copy(period = value, errorMessage = null) }
    }

    fun onAlgorithmChanged(value: String) {
        update { copy(algorithm = value, errorMessage = null) }
    }

    fun prepareSave(): Result<AccountSaveRequest> {
        val detailsState = AccountDetailsState(
            name = _state.value.name,
            email = _state.value.email,
            secret = _state.value.secret,
            digits = _state.value.digits,
            period = _state.value.period,
            algorithm = _state.value.algorithm,
        )

        val result = AccountDetailsValidator.validate(detailsState)

        result.exceptionOrNull()?.let { error ->
            update {
                copy(
                    errorMessage = error.message ?: "Invalid account details.",
                    isSaving = false,
                )
            }
        }

        return result
    }

    fun onSaveStarted() {
        update { copy(isSaving = true, errorMessage = null) }
    }

    fun onSaveFailed(message: String) {
        update { copy(isSaving = false, errorMessage = message) }
    }

    fun onSaveCompleted() {
        update { copy(isSaving = false, errorMessage = null) }
    }

    private inline fun update(
        transform: EnterSetupKeyState.() -> EnterSetupKeyState,
    ) {
        _state.value = _state.value.transform()
    }
}
