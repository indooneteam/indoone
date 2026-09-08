package com.indoone.accounts.addaccount.scanqr

/**
 * Parsed TOTP data produced after a valid QR code is detected.
 *
 * Keeping this result separate from the scanner UI makes the QR feature
 * independent from the account-detail screen and storage implementation.
 */
data class QrAccountResult(
    val name: String,
    val email: String,
    val secret: String,
    val issuer: String = "",
    val algorithm: String = "SHA1",
    val digits: Int = 6,
    val period: Int = 30,
)
