package com.indoone.accounts.storage

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.indoone.accounts.AccountRecord
import com.indoone.accounts.AccountRepository
import com.indoone.accounts.TrashRecord
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Firebase-backed account repository scoped to the authenticated user.
 */
class FirebaseAccountRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : AccountRepository {
    override suspend fun save(account: AccountRecord) {
        val uid = requireUid()
        awaitTask {
            accounts(uid).document(account.id).set(account.toMap())
        }
    }

    override suspend fun getAll(): List<AccountRecord> {
        val snapshot = awaitTask { accounts(requireUid()).get() }
        return snapshot.documents.mapNotNull { it.toAccountRecord() }.sortedBy { it.name.lowercase() }
    }

    override suspend fun remove(id: String) {
        awaitTask { accounts(requireUid()).document(id).delete() }
    }

    override suspend fun moveToTrash(id: String) {
        val uid = requireUid()
        val reference = accounts(uid).document(id)
        val snapshot = awaitTask { reference.get() }
        val account = snapshot.toAccountRecord() ?: throw IllegalStateException("Account not found.")
        val now = System.currentTimeMillis()
        val trashData = account.toMap() + mapOf(
            "deletedAt" to now,
            "purgeAt" to now + TRASH_DURATION_MS,
        )
        awaitTask { trash(uid).document(id).set(trashData) }
        awaitTask { reference.delete() }
    }

    override suspend fun listTrash(): List<TrashRecord> {
        val uid = requireUid()
        val snapshot = awaitTask { trash(uid).get() }
        val now = System.currentTimeMillis()
        val valid = mutableListOf<TrashRecord>()
        snapshot.documents.forEach { document ->
            val account = document.toAccountRecord() ?: return@forEach
            val deletedAt = document.getLong("deletedAt") ?: 0L
            val purgeAt = document.getLong("purgeAt") ?: (deletedAt + TRASH_DURATION_MS)
            if (purgeAt > now) {
                valid += TrashRecord(account, deletedAt, purgeAt)
            } else {
                document.reference.delete()
            }
        }
        return valid.sortedByDescending { it.deletedAt }
    }

    override suspend fun restoreFromTrash(id: String): AccountRecord {
        val uid = requireUid()
        val reference = trash(uid).document(id)
        val snapshot = awaitTask { reference.get() }
        val account = snapshot.toAccountRecord() ?: throw IllegalStateException("Trash account not found.")
        val purgeAt = snapshot.getLong("purgeAt") ?: 0L
        if (purgeAt <= System.currentTimeMillis()) {
            awaitTask { reference.delete() }
            throw IllegalStateException("Trash item has expired.")
        }
        awaitTask { accounts(uid).document(id).set(account.toMap()) }
        awaitTask { reference.delete() }
        return account
    }

    override suspend fun permanentlyDeleteFromTrash(id: String) {
        awaitTask { trash(requireUid()).document(id).delete() }
    }

    private fun requireUid(): String = auth.currentUser?.uid
        ?: throw IllegalStateException("Please login first.")

    private fun accounts(uid: String) = firestore.collection("users").document(uid).collection("accounts")
    private fun trash(uid: String) = firestore.collection("users").document(uid).collection("trash")

    private fun AccountRecord.toMap(): Map<String, Any> = hashMapOf(
        "id" to id,
        "name" to name,
        "email" to email,
        "secret" to secret,
        "digits" to digits,
        "period" to period,
        "algorithm" to algorithm,
        "provider" to provider,
        "service" to service,
        "favorite" to favorite,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt,
    )

    private fun com.google.firebase.firestore.DocumentSnapshot.toAccountRecord(): AccountRecord? {
        val recordId = getString("id") ?: id
        val name = getString("name") ?: return null
        val secret = getString("secret") ?: return null
        return AccountRecord(
            id = recordId,
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
                if (continuation.isActive) continuation.resume(value)
            }
            .addOnFailureListener { error ->
                if (continuation.isActive) continuation.resumeWithException(error)
            }
    }

    private companion object {
        const val TRASH_DURATION_MS = 30L * 24L * 60L * 60L * 1000L
    }
}
