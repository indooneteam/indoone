package com.indoone.accounts.addaccount.entersetupkey

/**
 * UI state for manual TOTP account setup.
 */
data class EnterSetupKeyState(
    val name: String = "",
    val email: String = "",
    val secret: String = "",
    val digits: Int = 6,
    val period: Int = 30,
    val algorithm: String = "SHA1",
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
) {
    val canSave: Boolean
        get() = name.isNotBlank() && secret.isNotBlank() && !isSaving
}
