package com.indoone.accounts.storage

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.indoone.accounts.AccountRecord
import com.indoone.accounts.AccountRepository

/**
 * Selects the cloud repository when Firebase is configured and the user is
 * authenticated; otherwise keeps the app functional with encrypted local storage.
 */
class AccountRepositoryProvider(
    context: Context,
) : AccountRepository {
    private val appContext = context.applicationContext
    private val local = EncryptedAccountRepository(appContext)
    private val cloud = createCloudRepository()

    override suspend fun save(account: AccountRecord) {
        activeRepository().save(account)
    }

    override suspend fun getAll(): List<AccountRecord> {
        return activeRepository().getAll()
    }

    override suspend fun remove(id: String) {
        activeRepository().remove(id)
    }

    override suspend fun moveToTrash(id: String) {
        activeRepository().moveToTrash(id)
    }

    override suspend fun listTrash() = activeRepository().listTrash()

    override suspend fun restoreFromTrash(id: String): AccountRecord {
        return activeRepository().restoreFromTrash(id)
    }

    override suspend fun permanentlyDeleteFromTrash(id: String) {
        activeRepository().permanentlyDeleteFromTrash(id)
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
}
