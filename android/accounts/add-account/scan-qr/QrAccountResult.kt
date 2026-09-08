package com.indoone.accounts.addaccount.scanqr

/**
 * Parsed TOTP data produced after a valid QR code is detected.
 */
data class QrAccountResult(
    val name: String,
    val email: String,
    val secret: String,
    val issuer: String = "",
    val algorithm: String = "SHA1",
    val digits: Int = 6,
    val period: Int = 30,
    val provider: String = "",
    val service: String = "",
)
