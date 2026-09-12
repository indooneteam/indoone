package com.indoone.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.indoone.menu.AppBottomNav
import com.indoone.menu.AppTab
import com.indoone.menu.AppTopBar
import com.indoone.menu.data.CloudChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private data class ChatMessage(
    val text: String,
    val fromUser: Boolean,
)

@Composable
fun HomeScreen(
    onMenuClick: () -> Unit,
    onLobbyClick: () -> Unit,
    onConnectClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    val context = LocalContext.current
    val userKey = remember {
        FirebaseAuth.getInstance().currentUser?.uid?.takeIf { it.isNotBlank() } ?: "local"
    }
    val loadedHistory = remember(userKey) {
        ChatHistoryStore.load(context, userKey)
    }
    val latestSavedChat = loadedHistory.firstOrNull()
    val cloudChatRepository = remember { CloudChatRepository() }

    var input by remember { mutableStateOf("") }
    var messages by remember(userKey) {
        mutableStateOf(
            latestSavedChat?.messages?.map {
                ChatMessage(it.text, it.fromUser)
            } ?: emptyList()
        )
    }
    var isSending by remember { mutableStateOf(false) }
    var conversationId by rememberSaveable(userKey) {
        mutableStateOf(latestSavedChat?.id)
    }
    val scope = rememberCoroutineScope()

    fun saveCurrentChat(id: String, snapshot: List<ChatMessage> = messages) {
        val storedMessages = snapshot.map {
            ChatHistoryStore.StoredMessage(
                text = it.text,
                fromUser = it.fromUser,
            )
        }
        ChatHistoryStore.save(context, userKey, id, storedMessages)
    }

    fun sendMessage() {
        val text = input.trim()
        if (text.isEmpty() || isSending) return

        messages = messages + ChatMessage(text, fromUser = true)
        input = ""
        isSending = true

        scope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    ChatApi.sendMessage(text, conversationId)
                }
            }
            result.onSuccess { response ->
                conversationId = response.conversationId
                val updatedMessages = messages + ChatMessage(response.reply, fromUser = false)
                messages = updatedMessages
                saveCurrentChat(response.conversationId, updatedMessages)
                runCatching {
                    cloudChatRepository.appendExchange(
                        conversationId = response.conversationId,
                        userMessage = text,
                        assistantReply = response.reply,
                    )
                }

                if (updatedMessages.size >= 50) {
                    input = ""
                    isSending = false
                    conversationId = null
                    messages = emptyList()
                }
            }.onFailure { error ->
                messages = messages + ChatMessage(
                    error.message ?: "Indoone AI could not complete the request. Please try again.",
                    fromUser = false,
                )
            }
            isSending = false
        }
    }

    fun onPlusClick() {
        // Attachment/actions will be added here during the next Home AI development pass.
    }

    Box(Modifier.fillMaxSize().background(Color.White)) {
        Column(Modifier.fillMaxSize()) {
            AppTopBar(
                onMenuClick = onMenuClick,
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(messages) { message ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (message.fromUser) Arrangement.End else Arrangement.Start,
                    ) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = if (message.fromUser) Color(0xFF703BE2) else Color(0xFFF5F2FB),
                        ) {
                            Text(
                                message.text,
                                modifier = Modifier.padding(horizontal = 15.dp, vertical = 12.dp),
                                color = if (message.fromUser) Color.White else Color(0xFF292331),
                                fontSize = 13.sp,
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .padding(end = 10.dp)
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF5F2FB))
                        .clickable(onClick = ::onPlusClick),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "+",
                        color = Color(0xFF703BE2),
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFFFAF9FC),
                    border = BorderStroke(1.dp, Color(0xFFE5E0ED)),
                ) {
                    BasicTextField(
                        value = input,
                        onValueChange = { input = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 15.dp, vertical = 13.dp),
                        enabled = !isSending,
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = Color(0xFF17151D),
                            fontSize = 14.sp,
                        ),
                        decorationBox = { innerTextField ->
                            Box {
                                if (input.isEmpty()) {
                                    Text("Message Indoone AI", color = Color(0xFF8A8492), fontSize = 14.sp)
                                }
                                innerTextField()
                            }
                        },
                    )
                }

                val canSend = !isSending && input.isNotBlank()
                Box(
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (canSend) Color(0xFF703BE2) else Color(0xFFE9E5F0))
                        .clickable(enabled = canSend, onClick = ::sendMessage),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isSending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.2.dp,
                        )
                    } else {
                        Text(
                            "➤",
                            color = if (canSend) Color.White else Color(0xFF9992A3),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            AppBottomNav(
                activeTab = AppTab.HOME,
                onAccountsClick = {},
                onLobbyClick = onLobbyClick,
                onConnectClick = onConnectClick,
                onSettingsClick = onSettingsClick,
            )
        }
    }
}
