package com.indoone.accounts.addaccount.scanqr

/**
 * Converts a scanner payload into the data required by account-details.
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

        val parsed = QrOtpAuthParser.parse(value)
        val result = parsed.getOrElse { error ->
            onInvalidQr(error.message ?: "Unable to read this QR code.")
            return
        }

        if (result.secret.isBlank()) {
            onInvalidQr("Missing TOTP secret.")
            return
        }

        onAccountDetailsReady(QrManualPrefill.from(result))
    }
}
