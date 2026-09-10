package com.indoone.accounts.addaccount.scanqr.accountdetails

/**
 * Validates and normalizes account details before persistence.
 */
object AccountDetailsValidator {
    fun validate(state: AccountDetailsState): Result<AccountSaveRequest> {
        val name = state.name.trim()
        val email = state.email.trim()
        val secret = state.secret
            .replace(" ", "")
            .replace("-", "")
            .uppercase()
        val algorithm = state.algorithm.trim().uppercase()

        if (name.isBlank()) {
            return Result.failure(
                IllegalArgumentException("Enter account name."),
            )
        }

        if (secret.isBlank()) {
            return Result.failure(
                IllegalArgumentException("Enter the TOTP secret key."),
            )
        }

        if (state.digits !in setOf(6, 8)) {
            return Result.failure(
                IllegalArgumentException("TOTP digits must be 6 or 8."),
            )
        }

        if (state.period <= 0) {
            return Result.failure(
                IllegalArgumentException("TOTP period must be greater than zero."),
            )
        }

        if (algorithm !in setOf("SHA1", "SHA256", "SHA512")) {
            return Result.failure(
                IllegalArgumentException("Unsupported TOTP algorithm."),
            )
        }

        return Result.success(
            AccountSaveRequest(
                name = name,
                email = email,
                secret = secret,
                digits = state.digits,
                period = state.period,
                algorithm = algorithm,
            ),
        )
    }
}
