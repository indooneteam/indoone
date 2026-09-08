package com.indoone.accounts

/**
 * Stored TOTP account independent from the UI list model.
 */
data class AccountRecord(
    val id: String,
    val name: String,
    val email: String,
    val secret: String,
    val digits: Int,
    val period: Int,
    val algorithm: String,
    val provider: String = "",
    val service: String = "",
    val favorite: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
)
