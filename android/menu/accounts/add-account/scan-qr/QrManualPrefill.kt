package com.indoone.accounts.addaccount.scanqr

/**
 * Maps a scanned TOTP payload into the fields expected by account setup.
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
                    .ifBlank { result.name }
                    .ifBlank { "Account" },
                email = result.email,
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
