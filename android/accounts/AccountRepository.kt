package com.indoone.accounts

/**
 * Persistence contract for authenticator accounts.
 */
interface AccountRepository {
    suspend fun save(account: AccountRecord)
    suspend fun getAll(): List<AccountRecord>
    suspend fun remove(id: String)
}
