package com.indoone.accounts.accounts

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.indoone.accounts.AccountRecord
import com.indoone.accounts.AccountRepository

/**
 * Persistence operations for account-level actions.
 */
class AccountDetailActions(
    private val repository: AccountRepository,
    private val context: Context,
) {
    suspend fun setFavorite(account: AccountRecord, favorite: Boolean): Result<AccountRecord> =
        runCatching {
            val updated = account.copy(
                favorite = favorite,
                updatedAt = System.currentTimeMillis(),
            )
            repository.save(updated)
            updated
        }

    fun copyCode(code: String): Result<Unit> = runCatching {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(
            ClipData.newPlainText("TOTP code", code.replace(" ", "")),
        )
    }
}
