package com.indoone.accounts

/**
 * Persistence contract for authenticator accounts.
 *
 * The Android storage implementation can use Firebase or another durable
 * backend without changing the account-add UI flow.
 */
interface AccountRepository {
    suspend fun save(account: AccountRecord)
    suspend fun getAll(): List<AccountRecord>
}
