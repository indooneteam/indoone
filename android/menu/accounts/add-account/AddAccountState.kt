package com.indoone.accounts.addaccount

/**
 * UI state for the Add Account method-selection screen.
 */
data class AddAccountState(
    val otpUri: String = "",
    val isImportEnabled: Boolean = false,
)
