package com.indoone.accounts.accounts

import com.indoone.accounts.AccountRepository

/**
 * Moves an authenticator account from the active collection to Trash.
 */
class AccountRemovalService(
    private val repository: AccountRepository,
) {
    suspend fun remove(accountId: String): Result<Unit> = runCatching {
        require(accountId.isNotBlank()) { "Account id is required." }
        repository.moveToTrash(accountId)
    }
}
