package com.indoone.accounts.accounts.edit

import androidx.lifecycle.ViewModel
import com.indoone.accounts.addaccount.scanqr.accountdetails.AccountDetailsState
import com.indoone.accounts.addaccount.scanqr.accountdetails.AccountDetailsValidator
import com.indoone.accounts.addaccount.scanqr.accountdetails.AccountSaveRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EditAccountViewModel(account: com.indoone.accounts.AccountRecord) : ViewModel() {
    private val _state = MutableStateFlow(
        EditAccountState(
            id = account.id,
            name = account.name,
            email = account.email,
            secret = account.secret,
            digits = account.digits,
            period = account.period,
            algorithm = account.algorithm,
        ),
    )

    val state: StateFlow<EditAccountState> = _state.asStateFlow()

    fun onNameChanged(value: String) = update { copy(name = value, errorMessage = null) }
    fun onEmailChanged(value: String) = update { copy(email = value, errorMessage = null) }
    fun onSecretChanged(value: String) = update { copy(secret = value, errorMessage = null) }
    fun onDigitsChanged(value: Int) = update { copy(digits = value, errorMessage = null) }
    fun onPeriodChanged(value: Int) = update { copy(period = value, errorMessage = null) }
    fun onAlgorithmChanged(value: String) = update { copy(algorithm = value.uppercase(), errorMessage = null) }

    fun prepareSave(): Result<AccountSaveRequest> {
        val result = AccountDetailsValidator.validate(
            AccountDetailsState(
                name = _state.value.name,
                email = _state.value.email,
                secret = _state.value.secret,
                digits = _state.value.digits,
                period = _state.value.period,
                algorithm = _state.value.algorithm,
            ),
        )

        result.exceptionOrNull()?.let { error ->
            update { copy(errorMessage = error.message ?: "Invalid account details.") }
        }
        return result
    }

    fun onSaveStarted() = update { copy(isSaving = true, errorMessage = null) }
    fun onSaveFailed(message: String) = update { copy(isSaving = false, errorMessage = message) }
    fun onSaveCompleted() = update { copy(isSaving = false, errorMessage = null) }

    private inline fun update(transform: EditAccountState.() -> EditAccountState) {
        _state.value = _state.value.transform()
    }
}
