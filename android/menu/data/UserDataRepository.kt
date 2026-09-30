package com.indoone.menu.data

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserDataRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(),
) {
    private val uid: String
        get() = auth.currentUser?.uid ?: throw IllegalStateException("Please sign in again.")

    suspend fun readProfile(): Map<String, Any?> = withContext(Dispatchers.IO) {
        val snapshot = Tasks.await(database.reference.child("users").child(uid).child("profile").get())
        snapshot.value as? Map<String, Any?> ?: emptyMap()
    }

    suspend fun updateProfileFields(name: String, nickname: String, profession: String) = withContext(Dispatchers.IO) {
        Tasks.await(database.reference.child("users").child(uid).child("profile").updateChildren(
            mapOf(
                "name" to name.trim(),
                "nickname" to nickname.trim(),
                "profession" to profession.trim(),
                "updatedAt" to System.currentTimeMillis(),
            )
        ))
    }

    suspend fun readMemory(): Map<String, Any?> = withContext(Dispatchers.IO) {
        val snapshot = Tasks.await(database.reference.child("users").child(uid).child("memory").get())
        snapshot.value as? Map<String, Any?> ?: emptyMap()
    }

    suspend fun setMemory(key: String, value: String) = withContext(Dispatchers.IO) {
        require(key.matches(Regex("[A-Za-z0-9_-]+"))) { "Invalid memory key." }
        Tasks.await(database.reference.child("users").child(uid).child("memory").child(key).setValue(
            mapOf("value" to value.trim(), "updatedAt" to System.currentTimeMillis(), "source" to "user")
        ))
    }

    suspend fun deleteMemory(key: String) = withContext(Dispatchers.IO) {
        Tasks.await(database.reference.child("users").child(uid).child("memory").child(key).removeValue())
    }
}
