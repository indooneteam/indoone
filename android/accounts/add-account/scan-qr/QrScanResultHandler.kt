package com.indoone.accounts.addaccount.scanqr

/**
 * Coordinates the scanner result with the account-details transition.
 *
 * Parsing remains inside the Scan QR feature. The host decides what to do with
 * the parsed result, keeping persistence and navigation outside the scanner.
 */
class QrScanResultHandler(
    private val onAccountDetailsReady: (QrManualPrefill) -> Unit,
    private val onInvalidQr: (String) -> Unit,
) {
    fun handle(rawValue: String) {
        val value = rawValue.trim()

        if (!value.startsWith("otpauth://", ignoreCase = true)) {
            onInvalidQr("QR detected. Please use a TOTP QR code.")
            return
        }

        val result = try {
            QrOtpAuthParser.parse(value)
        } catch (error: IllegalArgumentException) {
            onInvalidQr(error.message ?: "Unable to read this QR code.")
            return
        }

        if (result.secret.isBlank()) {
            onInvalidQr("Missing TOTP secret.")
            return
        }

        onAccountDetailsReady(
            QrManualPrefill.from(result),
        )
    }
}
