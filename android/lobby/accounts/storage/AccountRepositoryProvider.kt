package com.indoone.lobby.accounts.storage

import android.content.Context
import android.util.Log

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.indoone.lobby.accounts.AccountRecord
import com.indoone.lobby.accounts.AccountRepository

/**
 * Selects the cloud repository when Firebase is configured and the user is
 * authenticated; otherwise keeps the app functional with encrypted local storage.
 *
 * Reads are served from a short-lived cache. The cloud read is a network round
 * trip, and every screen that shows accounts used to pay for it on open — the
 * Lobby, Favorites and Trash all started empty and filled in a moment later. The
 * cache makes those screens paint immediately. It is dropped whenever an account
 * is written, so a change is never hidden behind it.
 */
class AccountRepositoryProvider(
    context: Context,
) : AccountRepository {
    private val appContext = context.applicationContext
    private val local = EncryptedAccountRepository(appContext)
    private val cloud = createCloudRepository()

    override suspend fun save(account: AccountRecord) {
        activeRepository().save(account)
        invalidateAccountsCache()
    }

    override suspend fun getAll(): List<AccountRecord> {
        val userId = currentUserId()
        cachedAccounts(userId)?.let { return it }

        val records = readAll()
        cacheAccounts(userId, records)
        return records
    }

    override suspend fun remove(id: String) {
        activeRepository().remove(id)
        invalidateAccountsCache()
    }

    override suspend fun moveToTrash(id: String) {
        activeRepository().moveToTrash(id)
        invalidateAccountsCache()
    }

    override suspend fun listTrash() = activeRepository().listTrash()

    override suspend fun restoreFromTrash(id: String): AccountRecord {
        val restored = activeRepository().restoreFromTrash(id)
        invalidateAccountsCache()
        return restored
    }

    override suspend fun permanentlyDeleteFromTrash(id: String) {
        activeRepository().permanentlyDeleteFromTrash(id)
        invalidateAccountsCache()
    }

    private suspend fun readAll(): List<AccountRecord> {
        val activeCloud = cloud?.takeIf { FirebaseAuth.getInstance().currentUser != null }
        if (activeCloud != null) {
            return runCatching { activeCloud.getAll() }.getOrElse { error ->
                // Keep app startup usable when Firebase Realtime Database rules or
                // connectivity temporarily block the cloud read. The encrypted
                // local repository remains the safe offline source of truth.
                Log.w(
                    "AccountRepositoryProvider",
                    "Cloud account read unavailable; using encrypted local accounts.",
                    error,
                )
                local.getAll()
            }
        }
        return local.getAll()
    }

    private fun activeRepository(): AccountRepository {
        return if (
            cloud != null &&
            FirebaseAuth.getInstance().currentUser != null
        ) {
            cloud
        } else {
            local
        }
    }

    private fun createCloudRepository(): AccountRepository? {
        return runCatching {
            if (FirebaseApp.getApps(appContext).isEmpty()) {
                null
            } else {
                FirebaseAccountRepository()
            }
        }.getOrNull()
    }

    private fun currentUserId(): String? =
        runCatching { FirebaseAuth.getInstance().currentUser?.uid }.getOrNull()

    private fun cachedAccounts(userId: String?): List<AccountRecord>? {
        val cached = accountsCache ?: return null
        if (cached.userId != userId) return null
        if (System.currentTimeMillis() - cached.readAtMillis > CACHE_TTL_MILLIS) return null
        return cached.records
    }

    private fun cacheAccounts(userId: String?, records: List<AccountRecord>) {
        accountsCache = AccountsCache(
            userId = userId,
            records = records,
            readAtMillis = System.currentTimeMillis(),
        )
    }

    private fun invalidateAccountsCache() {
        accountsCache = null
    }

    private data class AccountsCache(
        val userId: String?,
        val records: List<AccountRecord>,
        val readAtMillis: Long,
    )

    private companion object {
        private const val CACHE_TTL_MILLIS = 30_000L

        // Shared by every instance, so the drawer's repository and the activity's
        // repository see the same accounts.
        @Volatile
        private var accountsCache: AccountsCache? = null
    }
}
