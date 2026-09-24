package com.indoone.menu.chathistory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indoone.menu.data.CloudChatConversation
import com.indoone.menu.data.CloudChatRepository
import java.text.DateFormat
import java.util.Date

@Composable
fun ChatHistoryMenuScreen(
    onBack: () -> Unit,
    onOpenChat: (String) -> Unit,
) {
    val repository = remember { CloudChatRepository() }
    var conversations by remember { mutableStateOf<List<CloudChatConversation>>(emptyList()) }
    var status by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        loading = true
        runCatching { repository.loadConversations() }
            .onSuccess {
                conversations = it
                status = null
            }
            .onFailure {
                conversations = emptyList()
                status = it.message ?: "Chat history could not be loaded right now."
            }
        loading = false
    }

    Column(modifier = Modifier.fillMaxSize().padding(22.dp)) {
        Button(onClick = onBack) { Text("Back") }
        Text(
            "Chat History",
            color = Color(0xFF5E2DD2),
            fontSize = 24.sp,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            "Your chats are stored in your Firestore account history.",
            color = Color(0xFF6D6576),
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 5.dp),
        )

        when {
            loading -> {
                Text(
                    "Loading chats…",
                    color = Color(0xFF6D6576),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
            status != null -> {
                Text(
                    status.orEmpty(),
                    color = Color(0xFFD93025),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
            conversations.isEmpty() -> {
                Text(
                    "No saved chats yet.",
                    color = Color(0xFF6D6576),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(top = 16.dp),
                ) {
                    items(conversations, key = { it.id }) { chat ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenChat(chat.id) }
                                .padding(vertical = 6.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        chat.title,
                                        color = Color(0xFF27212E),
                                        fontSize = 14.sp,
                                    )
                                    Text(
                                        "${chat.messageCount}/50 messages${if (chat.closed) " · Closed" else ""}",
                                        color = Color(0xFF6D6576),
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 3.dp),
                                    )
                                    Text(
                                        DateFormat.getDateTimeInstance(
                                            DateFormat.MEDIUM,
                                            DateFormat.SHORT,
                                        ).format(Date(chat.updatedAtMillis)),
                                        color = Color(0xFF8A8392),
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(top = 2.dp),
                                    )
                                }
                                Text(
                                    "Open",
                                    color = Color(0xFF5E2DD2),
                                    fontSize = 12.sp,
                                )
                            }
                        }
                        HorizontalDivider(color = Color(0xFFEEE8F4))
                    }
                }
            }
        }
    }
}
