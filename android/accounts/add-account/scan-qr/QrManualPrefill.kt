package com.indoone.accounts.addaccount.scanqr

/**
 * Maps a scanned TOTP payload into the fields expected by account setup.
 *
 * The object deliberately contains data only; navigation and persistence stay
 * outside the scanner feature so the feature remains isolated and testable.
 */
data class QrManualPrefill(
    val name: String,
    val email: String,
    val secret: String,
    val algorithm: String,
    val digits: Int,
    val period: Int,
    val provider: String = "",
    val service: String = "",
) {
    companion object {
        fun from(result: QrAccountResult): QrManualPrefill {
            return QrManualPrefill(
                name = result.issuer
                    .ifBlank { result.label }
                    .ifBlank { "Account" },
                email = result.label,
                secret = result.secret,
                algorithm = result.algorithm,
                digits = result.digits,
                period = result.period,
                provider = result.provider,
                service = result.service,
            )
        }
    }
}
