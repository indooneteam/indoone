package com.indoone.home

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.ContextCompat
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
    val userKey = remember { FirebaseAuth.getInstance().currentUser?.uid?.takeIf { it.isNotBlank() } ?: "local" }
    val loadedHistory = remember(userKey) { ChatHistoryStore.load(context, userKey) }
    val latestSavedChat = loadedHistory.firstOrNull()
    val cloudChatRepository = remember { CloudChatRepository() }

    var input by remember { mutableStateOf("") }
    var messages by remember(userKey) { mutableStateOf(latestSavedChat?.messages?.map { ChatMessage(it.text, it.fromUser) } ?: emptyList()) }
    var isSending by remember { mutableStateOf(false) }
    var conversationId by rememberSaveable(userKey) { mutableStateOf(latestSavedChat?.id) }
    val scope = rememberCoroutineScope()

    fun saveCurrentChat(id: String, snapshot: List<ChatMessage> = messages) {
        ChatHistoryStore.save(context, userKey, id, snapshot.map { ChatHistoryStore.StoredMessage(it.text, it.fromUser) })
    }

    fun sendMessage() {
        val text = input.trim()
        if (text.isEmpty() || isSending) return
        messages = messages + ChatMessage(text, true)
        input = ""
        isSending = true
        scope.launch {
            runCatching { withContext(Dispatchers.IO) { ChatApi.sendMessage(text, conversationId) } }
                .onSuccess { response ->
                    conversationId = response.conversationId
                    val updated = messages + ChatMessage(response.reply, false)
                    messages = updated
                    saveCurrentChat(response.conversationId, updated)
                    runCatching {
                        cloudChatRepository.appendExchange(response.conversationId, text, response.reply)
                    }
                    if (updated.size >= 50) {
                        isSending = false
                        conversationId = null
                        messages = emptyList()
                    }
                }
                .onFailure { error ->
                    messages = messages + ChatMessage(error.message ?: "Indoone AI could not complete the request.", false)
                }
            isSending = false
        }
    }

    val voiceLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        if (!spoken.isNullOrBlank()) input = spoken
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            voiceLauncher.launch(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to Indoone")
            })
        }
    }

    fun startVoiceInput() {
        val activity = context as? Activity ?: return
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            voiceLauncher.launch(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to Indoone")
            })
        } else if (!activity.isFinishing) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Box(Modifier.fillMaxSize().background(Color.White)) {
        Column(Modifier.fillMaxSize()) {
            AppTopBar(onMenuClick = onMenuClick)
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(messages) { message ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (message.fromUser) Arrangement.End else Arrangement.Start) {
                        Surface(shape = RoundedCornerShape(18.dp), color = if (message.fromUser) Color(0xFF703BE2) else Color(0xFFF5F2FB)) {
                            Text(message.text, Modifier.padding(horizontal = 15.dp, vertical = 12.dp), color = if (message.fromUser) Color.White else Color(0xFF292331), fontSize = 13.sp)
                        }
                    }
                }
            }

            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.padding(end = 10.dp).size(42.dp).clip(CircleShape).background(Color(0xFFF5F2FB)).clickable {
                        // Attachment actions use ChatApi.uploadTextFile in the next UI pass.
                    },
                    contentAlignment = Alignment.Center,
                ) { Text("+", color = Color(0xFF703BE2), fontSize = 25.sp, fontWeight = FontWeight.Medium) }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFFFAF9FC),
                    border = BorderStroke(1.dp, Color(0xFFE5E0ED)),
                ) {
                    BasicTextField(
                        value = input,
                        onValueChange = { input = it },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 13.dp),
                        enabled = !isSending,
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color(0xFF17151D), fontSize = 14.sp),
                        decorationBox = { innerTextField -> Box { if (input.isEmpty()) Text("Message Indoone AI", color = Color(0xFF8A8492), fontSize = 14.sp); innerTextField() } },
                    )
                }

                Box(
                    modifier = Modifier.padding(start = 8.dp).size(42.dp).clip(CircleShape).background(Color(0xFFF5F2FB)).clickable(enabled = !isSending, onClick = ::startVoiceInput),
                    contentAlignment = Alignment.Center,
                ) { Text("🎙", fontSize = 18.sp) }

                val canSend = !isSending && input.isNotBlank()
                Box(
                    modifier = Modifier.padding(start = 8.dp).size(48.dp).clip(CircleShape).background(if (canSend) Color(0xFF703BE2) else Color(0xFFE9E5F0)).clickable(enabled = canSend, onClick = ::sendMessage),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isSending) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.2.dp)
                    else Text("➤", color = if (canSend) Color.White else Color(0xFF9992A3), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }

            AppBottomNav(activeTab = AppTab.HOME, onAccountsClick = {}, onLobbyClick = onLobbyClick, onConnectClick = onConnectClick, onSettingsClick = onSettingsClick)
        }
    }
}
