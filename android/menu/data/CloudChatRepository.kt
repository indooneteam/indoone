package com.indoone.menu.data

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class CloudChatRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    private val uid: String
        get() = auth.currentUser?.uid ?: throw IllegalStateException("Please sign in again.")

    suspend fun appendExchange(
        conversationId: String,
        userMessage: String,
        assistantReply: String,
        messageLimit: Int = 50,
    ) = withContext(Dispatchers.IO) {
        val conversation = firestore.collection("conversations").document(conversationId)
        val snapshot = Tasks.await(conversation.get())
        val existingCount = snapshot.getLong("messageCount")?.toInt() ?: 0
        val now = FieldValue.serverTimestamp()

        if (existingCount >= messageLimit) return@withContext

        if (!snapshot.exists()) {
            conversation.set(
                mapOf(
                    "userId" to uid,
                    "createdAt" to now,
                    "updatedAt" to now,
                    "messageCount" to 0,
                    "closed" to false,
                )
            )
        } else if (snapshot.getString("userId") != uid) {
            throw SecurityException("Conversation does not belong to this account.")
        }

        val remaining = messageLimit - existingCount
        val values = listOf(
            "user" to userMessage,
            "assistant" to assistantReply,
        ).take(remaining)

        val batch = firestore.batch()
        values.forEach { (role, text) ->
            val id = UUID.randomUUID().toString()
            val messageRef = conversation.collection("messages").document(id)
            batch.set(
                messageRef,
                mapOf(
                    "role" to role,
                    "content" to text,
                    "createdAt" to now,
                )
            )
        }

        val newCount = existingCount + values.size
        batch.update(
            conversation,
            mapOf(
                "updatedAt" to now,
                "messageCount" to newCount,
                "closed" to (newCount >= messageLimit),
                "closedAt" to if (newCount >= messageLimit) now else null,
            )
        )
        Tasks.await(batch.commit())
    }
}
