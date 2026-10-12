package com.indoone.menu.chathistory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.ValueEventListener
import com.indoone.home.message.CloudChatConversation
import com.indoone.home.message.CloudChatRepository
import kotlinx.coroutines.launch
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
    val scope = rememberCoroutineScope()

    fun reload() {
        scope.launch {
            runCatching { repository.loadConversations() }
                .onSuccess {
                    conversations = it
                    status = null
                }
                .onFailure {
                    status = it.message ?: "Chat history could not be loaded right now."
                }
        }
    }

    LaunchedEffect(Unit) {
        loading = true
        runCatching {
            repository.cleanupExpiredConversations()
            repository.loadConversations()
        }.onSuccess {
            conversations = it
            status = null
        }.onFailure {
            conversations = emptyList()
            status = it.message ?: "Chat history could not be loaded right now."
        }
        loading = false
    }

    DisposableEffect(repository) {
        val listener = repository.addConversationIdListener(
            onChanged = {
                reload()
            },
            onError = { error -> status = error },
        )
        onDispose { repository.removeConversationIdListener(listener) }
    }

    Surface(Modifier.fillMaxSize(), color = Color.White) {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                }
                Text(
                    "Chat History",
                    color = Color(0xFF211B29),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 7.dp),
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFFF5F9FF),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        modifier = Modifier.size(45.dp),
                        shape = RoundedCornerShape(13.dp),
                        color = Color(0xFFE3EEFF),
                    ) {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Outlined.History,
                                contentDescription = null,
                                tint = Color(0xFF2168D6),
                            )
                        }
                    }
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(
                            "Your conversations",
                            color = Color(0xFF201B28),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "Open or remove saved chats from your account history.",
                            modifier = Modifier.padding(top = 3.dp),
                            color = Color(0xFF6E6777),
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                        )
                    }
                }
            }

            when {
                loading -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(26.dp),
                            color = Color(0xFF2168D6),
                        )
                        Text(
                            "Loading chats…",
                            modifier = Modifier.padding(top = 10.dp),
                            color = Color(0xFF77717F),
                            fontSize = 12.sp,
                        )
                    }
                }

                status != null -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().weight(1f).padding(30.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            Icons.Outlined.ChatBubbleOutline,
                            contentDescription = null,
                            tint = Color(0xFFD93025),
                        )
                        Text(
                            status.orEmpty(),
                            modifier = Modifier.padding(top = 10.dp),
                            color = Color(0xFFD93025),
                            fontSize = 12.sp,
                        )
                    }
                }

                conversations.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().weight(1f).padding(30.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            Icons.Outlined.History,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = Color(0xFF9A91A6),
                        )
                        Text(
                            "No saved chats yet.",
                            modifier = Modifier.padding(top = 10.dp),
                            color = Color(0xFF4F4857),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(9.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 6.dp, bottom = 18.dp),
                    ) {
                        items(conversations, key = { it.id }) { chat ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White,
                                shadowElevation = 1.dp,
                                onClick = { onOpenChat(chat.id) },
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(13.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Surface(
                                        modifier = Modifier.size(40.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (chat.closed) Color(0xFFF3F0F5) else Color(0xFFEAF4FF),
                                    ) {
                                        androidx.compose.foundation.layout.Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                Icons.Outlined.ChatBubbleOutline,
                                                contentDescription = null,
                                                tint = if (chat.closed) Color(0xFF8B8492) else Color(0xFF2168D6),
                                            )
                                        }
                                    }

                                    Column(
                                        modifier = Modifier.weight(1f).padding(start = 11.dp),
                                    ) {
                                        Text(
                                            chat.title,
                                            color = Color(0xFF27212E),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                        )
                                        Text(
                                            DateFormat.getDateTimeInstance(
                                                DateFormat.MEDIUM,
                                                DateFormat.SHORT,
                                            ).format(Date(chat.updatedAtMillis)),
                                            color = Color(0xFF8A8392),
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(top = 2.dp),
                                        )
                                    }

                                    Icon(
                                        Icons.Outlined.ArrowForwardIos,
                                        contentDescription = "Open chat",
                                        modifier = Modifier.size(15.dp),
                                        tint = Color(0xFFAAA3B0),
                                    )

                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                runCatching { repository.deleteConversation(chat.id) }
                                                    .onSuccess {
                                                        conversations = conversations.filterNot { it.id == chat.id }
                                                        status = null
                                                    }
                                                    .onFailure {
                                                        status = it.message ?: "Chat could not be deleted."
                                                    }
                                            }
                                        },
                                    ) {
                                        Icon(
                                            Icons.Outlined.DeleteOutline,
                                            contentDescription = "Delete chat",
                                            tint = Color(0xFFD93025),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
