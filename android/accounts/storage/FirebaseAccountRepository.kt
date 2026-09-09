package com.indoone.accounts.storage

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.Query
import com.indoone.accounts.AccountRecord
import com.indoone.accounts.AccountRepository
import com.indoone.accounts.TrashRecord
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Firebase Realtime Database repository matching Main's users/{uid}/accounts schema.
 */
class FirebaseAccountRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(),
) : AccountRepository {

    override suspend fun save(account: AccountRecord) {
        val cleaned = account.copy(
            id = account.id.toLongOrNull()?.toString() ?: account.id,
            name = account.name.trim(),
            email = account.email.trim(),
            secret = account.secret.replace("\\s".toRegex(), "").uppercase(),
            icon = account.icon.ifBlank { account.name.firstOrNull()?.uppercase() ?: "?" },
        )
        awaitTask {
            accountsPath().child(cleaned.id).setValue(cleaned.toMap())
        }
    }

    override suspend fun getAll(): List<AccountRecord> {
        val uid = requireUid()
        val byId = LinkedHashMap<String, AccountRecord>()

        val own = awaitTask { accounts(uid).get() }
        mergeAccounts(byId, own)

        val email = auth.currentUser?.email?.trim()?.lowercase().orEmpty()
        if (email.isNotBlank()) {
            runCatching {
                val linkedUsers = awaitTask {
                    database.reference
                        .child("users")
                        .orderByChild("profile/email")
                        .equalTo(email)
                        .get()
                }
                linkedUsers.children.forEach { userNode ->
                    mergeAccounts(byId, userNode.child("accounts"))
                }
            }
        }

        return byId.values.sortedByDescending { it.id.toLongOrNull() ?: 0L }
    }

    override suspend fun remove(id: String) {
        awaitTask { accountsPath().child(id).removeValue() }
    }

    override suspend fun moveToTrash(id: String) {
        val account = awaitTask { accountsPath().child(id).get() }.toAccountRecord()
            ?: throw IllegalStateException("Account not found.")
        val now = System.currentTimeMillis()
        val updates = mapOf(
            "trash/$id" to account.toMap().toMutableMap().apply {
                put("deletedAt", now)
                put("purgeAt", now + TRASH_DURATION_MS)
            },
            "accounts/$id" to null,
        )
        awaitTask { userPath().updateChildren(updates) }
    }

    override suspend fun listTrash(): List<TrashRecord> {
        val snapshot = awaitTask { trashPath().get() }
        val now = System.currentTimeMillis()
        val result = mutableListOf<TrashRecord>()
        val expired = mutableListOf<DatabaseReference>()

        snapshot.children.forEach { item ->
            val account = item.toAccountRecord() ?: return@forEach
            val deletedAt = item.child("deletedAt").longValue()
            val purgeAt = item.child("purgeAt").longValue().takeIf { it > 0L }
                ?: (deletedAt + TRASH_DURATION_MS)
            if (purgeAt > now) {
                result += TrashRecord(account, deletedAt, purgeAt)
            } else {
                expired += item.ref
            }
        }

        expired.forEach { it.removeValue() }
        return result.sortedByDescending { it.deletedAt }
    }

    override suspend fun restoreFromTrash(id: String): AccountRecord {
        val ref = trashPath().child(id)
        val snapshot = awaitTask { ref.get() }
        val account = snapshot.toAccountRecord()
            ?: throw IllegalStateException("Trash account not found.")
        val purgeAt = snapshot.child("purgeAt").longValue()
        if (purgeAt <= System.currentTimeMillis()) {
            awaitTask { ref.removeValue() }
            throw IllegalStateException("Trash item has expired.")
        }

        awaitTask {
            userPath().updateChildren(
                mapOf(
                    "accounts/$id" to account.toMap(),
                    "trash/$id" to null,
                ),
            )
        }
        return account
    }

    override suspend fun permanentlyDeleteFromTrash(id: String) {
        awaitTask { trashPath().child(id).removeValue() }
    }

    private fun requireUid(): String = auth.currentUser?.uid
        ?: throw IllegalStateException("Please login first.")

    private fun accounts(uid: String): DatabaseReference =
        database.reference.child("users").child(uid).child("accounts")

    private fun trash(uid: String): DatabaseReference =
        database.reference.child("users").child(uid).child("trash")

    private fun accountsPath(): DatabaseReference = accounts(requireUid())

    private fun trashPath(): DatabaseReference = trash(requireUid())

    private fun userPath(): DatabaseReference =
        database.reference.child("users").child(requireUid())

    private fun mergeAccounts(target: MutableMap<String, AccountRecord>, snapshot: DataSnapshot) {
        snapshot.children.forEach { child ->
            child.toAccountRecord()?.let { account ->
                val current = target[account.id]
                if (current == null || account.updatedAt >= current.updatedAt) {
                    target[account.id] = account
                }
            }
        }
    }

    private fun AccountRecord.toMap(): Map<String, Any> = mapOf(
        "id" to (id.toLongOrNull() ?: id),
        "name" to name,
        "email" to email,
        "secret" to secret,
        "digits" to digits,
        "period" to period,
        "algorithm" to algorithm,
        "provider" to provider,
        "service" to service,
        "favorite" to favorite,
        "icon" to icon,
        "cls" to cls,
        "updatedAt" to updatedAt,
        "createdAt" to createdAt,
    )

    private fun DataSnapshot.toAccountRecord(): AccountRecord? {
        if (!exists()) return null
        val name = child("name").stringValue() ?: return null
        val secret = child("secret").stringValue() ?: return null
        val recordId = child("id").value?.toString() ?: key ?: return null
        val normalizedId = recordId.toLongOrNull()?.toString() ?: recordId
        val email = child("email").stringValue().orEmpty()
        val provider = child("provider").stringValue().orEmpty()
        val service = child("service").stringValue().orEmpty()
        val icon = child("icon").stringValue().ifBlank { name.firstOrNull()?.uppercase() ?: "?" }
        val cls = child("cls").stringValue().ifBlank { classify(name, provider, service) }

        return AccountRecord(
            id = normalizedId,
            name = name,
            email = email,
            secret = secret,
            digits = child("digits").intValue(6),
            period = child("period").intValue(30),
            algorithm = child("algorithm").stringValue()?.uppercase() ?: "SHA1",
            provider = provider,
            service = service,
            favorite = child("favorite").booleanValue(),
            icon = icon,
            cls = cls,
            createdAt = child("createdAt").longValue(),
            updatedAt = child("updatedAt").longValue(),
        )
    }

    private fun classify(name: String, provider: String, service: String): String {
        val value = "$name $provider $service".lowercase()
        return when {
            "github" in value -> "github"
            "microsoft" in value -> "microsoft"
            "binance" in value -> "binance"
            "dropbox" in value -> "dropbox"
            "zoho" in value -> "zoho"
            else -> "google"
        }
    }

    private fun DataSnapshot.stringValue(): String? = value?.toString()

    private fun DataSnapshot.longValue(): Long = when (val raw = value) {
        is Number -> raw.toLong()
        is String -> raw.toLongOrNull() ?: 0L
        else -> 0L
    }

    private fun DataSnapshot.intValue(default: Int): Int = when (val raw = value) {
        is Number -> raw.toInt()
        is String -> raw.toIntOrNull() ?: default
        else -> default
    }

    private fun DataSnapshot.booleanValue(): Boolean = when (val raw = value) {
        is Boolean -> raw
        is String -> raw.toBoolean()
        is Number -> raw.toInt() != 0
        else -> false
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
