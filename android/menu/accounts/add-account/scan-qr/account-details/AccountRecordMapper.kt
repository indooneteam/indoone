package com.indoone.accounts.addaccount.scanqr.accountdetails

import com.indoone.accounts.AccountRecord

/**
 * Converts validated account details into the shared Main cloud schema.
 */
object AccountRecordMapper {
    fun from(
        request: AccountSaveRequest,
        now: Long = System.currentTimeMillis(),
    ): AccountRecord {
        val name = request.name.trim()
        val normalizedName = name.lowercase()
        val serviceClass = when {
            "github" in normalizedName -> "github"
            "microsoft" in normalizedName -> "microsoft"
            "binance" in normalizedName -> "binance"
            "dropbox" in normalizedName -> "dropbox"
            "zoho" in normalizedName -> "zoho"
            else -> "google"
        }

        return AccountRecord(
            id = now.toString(),
            name = name,
            email = request.email.trim(),
            secret = request.secret,
            digits = request.digits,
            period = request.period,
            algorithm = request.algorithm,
            provider = when (serviceClass) {
                "github" -> "GitHub"
                "microsoft" -> "Microsoft"
                "binance" -> "Binance"
                "dropbox" -> "Dropbox"
                "zoho" -> "Zoho"
                else -> "Google"
            },
            service = name,
            favorite = false,
            icon = name.firstOrNull()?.uppercase() ?: "?",
            cls = serviceClass,
            createdAt = now,
            updatedAt = now,
        )
    }
}
