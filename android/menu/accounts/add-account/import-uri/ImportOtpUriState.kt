package com.indoone.accounts.addaccount.importuri

/**
 * UI state for importing an OTPAuth URI.
 */
data class ImportOtpUriState(
    val uri: String = "",
    val errorMessage: String? = null,
) {
    val canContinue: Boolean
        get() = uri.trim().startsWith("otpauth://", ignoreCase = true)
}
