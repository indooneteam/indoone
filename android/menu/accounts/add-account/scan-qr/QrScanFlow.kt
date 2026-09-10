package com.indoone.accounts.addaccount.scanqr

/**
 * Coordinates the QR result after scanning without coupling the scanner to UI.
 *
 * The caller can use the parsed result to open the account-details screen.
 */
object QrScanFlow {
    fun parseToAccount(rawValue: String): Result<QrAccountResult> {
        return QrOtpAuthParser.parse(rawValue)
    }
}
