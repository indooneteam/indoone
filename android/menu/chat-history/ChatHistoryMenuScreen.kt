package com.indoone.menu.chathistory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

private data class ConversationItem(val id: String, val count: Int, val closed: Boolean, val updatedLabel: String)

@Composable
fun ChatHistoryMenuScreen(onBack: () -> Unit) {
    val auth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }
    var conversations by remember { mutableStateOf<List<ConversationItem>>(emptyList()) }
    var status by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        val uid = auth.currentUser?.uid
        var registration: ListenerRegistration? = null
        if (uid == null) {
            status = "Please sign in again."
        } else {
            registration = firestore.collection("conversations")
                .whereEqualTo("userId", uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        status = "Chat history could not be loaded right now."
                        return@addSnapshotListener
                    }
                    status = null
                    conversations = snapshot?.documents.orEmpty().map { document ->
                        ConversationItem(
                            id = document.id,
                            count = document.getLong("messageCount")?.toInt() ?: 0,
                            closed = document.getBoolean("closed") ?: false,
                            updatedLabel = document.getTimestamp("updatedAt")?.toDate()?.toString().orEmpty(),
                        )
                    }.sortedByDescending { it.updatedLabel }
                }
        }
        onDispose { registration?.remove() }
    }

    Column(modifier = Modifier.fillMaxSize().padding(22.dp)) {
        Button(onClick = onBack) { Text("Back") }
        Text("Chat History", color = Color(0xFF5E2DD2), fontSize = 24.sp, modifier = Modifier.padding(top = 12.dp))
        status?.let { Text(it, color = Color(0xFFD93025), fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp)) }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 12.dp)) {
            items(conversations, key = { it.id }) { chat ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Chat ${chat.id.take(8)}", color = Color(0xFF27212E), fontSize = 14.sp)
                        Text("${chat.count}/50 messages", color = Color(0xFF6D6576), fontSize = 12.sp, modifier = Modifier.padding(top = 3.dp))
                        if (chat.closed) Text("Closed", color = Color(0xFF6330DB), fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                    }
                }
            }
        }
    }
}
