package com.indoone.menu.data

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.indoone.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

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

    /**
     * Tell the backend the user's name, so a reply can address them by name.
     *
     * Only the name is sent. The backend stores it in its memory store and adds one
     * short line to the prompt, which costs a few tokens and nothing when unknown.
     * A failure here never fails the profile save: the name simply stays unknown.
     */
    suspend fun publishNameToBackend(name: String, nickname: String) = withContext(Dispatchers.IO) {
        val value = name.trim().ifBlank { nickname.trim() }
        if (value.isBlank()) return@withContext
        val base = BuildConfig.INDOONE_BACKEND_URL.trim().trimEnd('/')
        if (base.isBlank()) return@withContext
        val token = Tasks.await(auth.currentUser?.getIdToken(false))?.token
            ?.takeIf { it.isNotBlank() }
            ?: return@withContext

        val connection = (URL("$base/api/memory").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 10_000
            readTimeout = 15_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer $token")
        }
        try {
            val payload = JSONObject()
                .put("user_id", uid)
                .put("key", "name")
                .put("value", value)
                .put("confidence", 1.0)
                .put("source", "profile")
                .toString()
            connection.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
            connection.responseCode
        } finally {
            connection.disconnect()
        }
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
