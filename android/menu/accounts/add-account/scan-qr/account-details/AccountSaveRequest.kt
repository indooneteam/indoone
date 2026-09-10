package com.indoone.accounts.addaccount.scanqr.accountdetails

/**
 * Validated account data ready for the accounts storage layer.
 */
data class AccountSaveRequest(
    val name: String,
    val email: String,
    val secret: String,
    val digits: Int,
    val period: Int,
    val algorithm: String,
)
