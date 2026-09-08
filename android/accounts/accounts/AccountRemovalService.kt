package com.indoone.accounts.accounts

import com.indoone.accounts.AccountRepository

/**
 * Removes an authenticator account from the active account collection.
 */
class AccountRemovalService(
    private val repository: AccountRepository,
) {
    suspend fun remove(accountId: String): Result<Unit> = runCatching {
        require(accountId.isNotBlank()) { "Account id is required." }
        repository.remove(accountId)
    }
}
