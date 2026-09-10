package com.indoone.accounts

/**
 * Persistence contract for authenticator accounts.
 */
interface AccountRepository {
    suspend fun save(account: AccountRecord)
    suspend fun getAll(): List<AccountRecord>
    suspend fun remove(id: String)

    /** Move an active account to Trash for 30 days. */
    suspend fun moveToTrash(id: String) {
        throw UnsupportedOperationException("Trash is unavailable.")
    }

    /** Return currently trashed accounts, purging expired entries first. */
    suspend fun listTrash(): List<TrashRecord> = emptyList()

    /** Restore a trashed account back to the active account collection. */
    suspend fun restoreFromTrash(id: String): AccountRecord {
        throw UnsupportedOperationException("Trash restore is unavailable.")
    }

    /** Permanently delete an account from Trash. */
    suspend fun permanentlyDeleteFromTrash(id: String) {
        throw UnsupportedOperationException("Permanent Trash deletion is unavailable.")
    }
}
