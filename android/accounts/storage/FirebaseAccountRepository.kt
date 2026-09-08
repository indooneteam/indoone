package com.indoone.accounts.storage

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.indoone.accounts.AccountRecord
import com.indoone.accounts.AccountRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Firebase-backed account repository.
 *
 * Accounts are scoped below the currently authenticated user's document so one
 * user's authenticator data cannot be addressed through another user's path.
 */
class FirebaseAccountRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : AccountRepository {
    override suspend fun save(account: AccountRecord) {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("Please login first.")

        val data = hashMapOf(
            "id" to account.id,
            "name" to account.name,
            "email" to account.email,
            "secret" to account.secret,
            "digits" to account.digits,
            "period" to account.period,
            "algorithm" to account.algorithm,
            "provider" to account.provider,
            "service" to account.service,
            "favorite" to account.favorite,
            "createdAt" to account.createdAt,
            "updatedAt" to account.updatedAt,
        )

        awaitTask {
            firestore
                .collection("users")
                .document(uid)
                .collection("accounts")
                .document(account.id)
                .set(data)
        }
    }

    override suspend fun getAll(): List<AccountRecord> {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("Please login first.")

        val snapshot = awaitTask {
            firestore
                .collection("users")
                .document(uid)
                .collection("accounts")
                .get()
        }

        return snapshot.documents.mapNotNull { document ->
            document.toAccountRecord()
        }.sortedBy { it.name.lowercase() }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toAccountRecord(): AccountRecord? {
        val id = getString("id") ?: id
        val name = getString("name") ?: return null
        val secret = getString("secret") ?: return null

        return AccountRecord(
            id = id,
            name = name,
            email = getString("email").orEmpty(),
            secret = secret,
            digits = getLong("digits")?.toInt() ?: 6,
            period = getLong("period")?.toInt() ?: 30,
            algorithm = getString("algorithm") ?: "SHA1",
            provider = getString("provider").orEmpty(),
            service = getString("service").orEmpty(),
            favorite = getBoolean("favorite") ?: false,
            createdAt = getLong("createdAt") ?: 0L,
            updatedAt = getLong("updatedAt") ?: 0L,
        )
    }

    private suspend fun <T> awaitTask(
        request: () -> com.google.android.gms.tasks.Task<T>,
    ): T = suspendCancellableCoroutine { continuation ->
        request()
            .addOnSuccessListener { value ->
                if (continuation.isActive) {
                    continuation.resume(value)
                }
            }
            .addOnFailureListener { error ->
                if (continuation.isActive) {
                    continuation.resumeWithException(error)
                }
            }
    }
}
