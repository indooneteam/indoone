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
    private val local = EncryptedAccountRepository(context)
    private val cloud = createCloudRepository()

    override suspend fun save(account: AccountRecord) {
        activeRepository().save(account)
    }

    override suspend fun getAll(): List<AccountRecord> {
        return activeRepository().getAll()
    }

    private fun activeRepository(): AccountRepository {
        val firebaseRepository = cloud

        return if (
            firebaseRepository != null &&
            FirebaseAuth.getInstance().currentUser != null
        ) {
            firebaseRepository
        } else {
            local
        }
    }

    private fun createCloudRepository(): AccountRepository? {
        return runCatching {
            if (FirebaseApp.getApps(localContext).isEmpty()) {
                null
            } else {
                FirebaseAccountRepository()
            }
        }.getOrNull()
    }

    private companion object {
        lateinit var localContext: Context
    }

    init {
        localContext = context.applicationContext
    }
}
