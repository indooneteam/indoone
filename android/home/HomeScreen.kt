package com.indoone.home

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.core.content.FileProvider
import java.io.File
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.indoone.menu.AppBottomNav
import com.indoone.menu.AppTab
import com.indoone.menu.AppTopBar
import com.indoone.home.message.ChatApi
import com.indoone.home.message.CloudChatRepository
import com.indoone.home.plus.PlusAttachmentSheet
import com.indoone.home.vibe.VibeButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID

private const val MAX_UPLOAD_BYTES = 2_000_000
private const val MAX_CHAT_ATTACHMENT_BYTES = 8_000_000

private fun readLimitedBytes(
    context: Context,
    uri: Uri,
    maxBytes: Int = MAX_UPLOAD_BYTES,
): ByteArray {
    val output = ByteArrayOutputStream()
    context.contentResolver.openInputStream(uri)?.use { input ->
        val buffer = ByteArray(16 * 1024)
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            if (output.size() + count > maxBytes) {
                throw IllegalArgumentException(
                    "File is too large. Maximum size is ${maxBytes / 1_000_000} MB."
                )
            }
            output.write(buffer, 0, count)
        }
    } ?: throw IllegalArgumentException("Could not open the selected file.")
    return output.toByteArray()
}

private fun supportedAttachmentMimeType(context: Context, uri: Uri, filename: String): String {
    val detected = context.contentResolver.getType(uri)?.trim()?.lowercase()
    val supported = setOf(
        "image/jpeg",
        "image/png",
        "image/webp",
        "application/pdf",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    )
    if (detected != null && detected in supported) return detected

    return when (filename.substringAfterLast('.', "").lowercase()) {
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "webp" -> "image/webp"
        "pdf" -> "application/pdf"
        "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        else -> throw IllegalArgumentException(
            "This attachment type is not supported yet. Use JPEG, PNG, WebP, PDF, or DOCX."
        )
    }
}

private fun decodeAttachmentThumbnail(context: Context, uri: Uri): Bitmap? {
    return try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sampleSize = 1
        while (bounds.outWidth / sampleSize > 180 || bounds.outHeight / sampleSize > 180) {
            sampleSize *= 2
        }
        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.RGB_565
        }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }
    } catch (_: Exception) {
        null
    }
}

@Composable
private fun PendingAttachmentPreview(
    uri: Uri?,
    fileName: String,
    isPhoto: Boolean,
    onRemove: () -> Unit,
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isPhoto && uri != null) {
            val thumbnail by produceState<Bitmap?>(initialValue = null, key1 = uri) {
                value = withContext(Dispatchers.IO) { decodeAttachmentThumbnail(context, uri) }
            }
            Box(Modifier.size(54.dp)) {
                val bitmap = thumbnail
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Attached photo preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(9.dp)),
                    )
                } else {
                    Box(
                        Modifier.fillMaxSize().clip(RoundedCornerShape(9.dp)).background(Color(0xFFEDE7F8)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("IMG", color = Color(0xFF5E2DD2), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Box(
                    modifier = Modifier.align(Alignment.TopEnd).offset(x = 4.dp, y = (-4).dp)
                        .size(22.dp).clip(CircleShape).background(Color(0xFF292431))
                        .clickable(onClick = onRemove),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("×", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
            Text(
                fileName,
                color = Color(0xFF5E2DD2),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(start = 9.dp),
            )
        } else {
            Row(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF6F3FB))
                    .padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val label = fileName.substringAfterLast('.', "").take(4).uppercase().ifBlank { "FILE" }
                Box(
                    modifier = Modifier.size(34.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFE8DDFB)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(label, color = Color(0xFF5E2DD2), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    fileName,
                    color = Color(0xFF3F354F),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                )
                Box(
                    modifier = Modifier.size(34.dp).clip(CircleShape).clickable(onClick = onRemove),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("×", color = Color(0xFF51495B), fontSize = 20.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

private fun appendVoiceTranscript(existingText: String, spokenText: String): String {
    val base = existingText.trimEnd()
    val spoken = spokenText.trim()
    if (spoken.isEmpty()) return base
    return if (base.isEmpty()) spoken else "$base $spoken"
}

private data class ChatMessage(
    val text: String,
    val fromUser: Boolean,
)

private enum class ChatActivityState {
    IDLE,
    RECEIVED,
    THINKING,
    PREPARING,
}

private fun ChatActivityState.label(): String = when (this) {
    ChatActivityState.IDLE -> ""
    ChatActivityState.RECEIVED -> "Received"
    ChatActivityState.THINKING -> "Thinking…"
    ChatActivityState.PREPARING -> "Preparing your answer…"
}

@Composable
private fun RowScope.QuickPromptChip(
    label: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFD9C9F2)),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                label,
                color = Color(0xFF5E2DD2),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun IndooneHomeLogo(modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val scaleFactor = size.minDimension / 48f
        scale(scaleFactor) {
            rotate(
                45f,
                pivot = androidx.compose.ui.geometry.Offset(24f, 24f),
            ) {
                drawRoundRect(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(
                            androidx.compose.ui.graphics.Color(0xFFC15CFF),
                            androidx.compose.ui.graphics.Color(0xFF7C3AED),
                            androidx.compose.ui.graphics.Color(0xFF22C7FF),
                        ),
                        start = androidx.compose.ui.geometry.Offset(11f, 11f),
                        end = androidx.compose.ui.geometry.Offset(37f, 37f),
                    ),
                    topLeft = androidx.compose.ui.geometry.Offset(11f, 11f),
                    size = androidx.compose.ui.geometry.Size(26f, 26f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
                )
            }
            val outer = androidx.compose.ui.graphics.Path().apply {
                moveTo(24f, 14f); lineTo(27.2f, 20.8f); lineTo(34f, 24f); lineTo(27.2f, 27.2f)
                lineTo(24f, 34f); lineTo(20.8f, 27.2f); lineTo(14f, 24f); lineTo(20.8f, 20.8f); close()
            }
            drawPath(outer, color = androidx.compose.ui.graphics.Color(0xFF0A0A18))
            val inner = androidx.compose.ui.graphics.Path().apply {
                moveTo(24f, 20.8f); lineTo(25.2f, 22.8f); lineTo(27.2f, 24f); lineTo(25.2f, 25.2f)
                lineTo(24f, 27.2f); lineTo(22.8f, 25.2f); lineTo(20.8f, 24f); lineTo(22.8f, 22.8f); close()
            }
            drawPath(inner, color = androidx.compose.ui.graphics.Color(0xFF60A5FA))
        }
    }
}

@Composable
fun HomeScreen(
    onMenuClick: () -> Unit,
    onLobbyClick: () -> Unit,
    onConnectClick: () -> Unit,
    onSettingsClick: () -> Unit,
    startNewChat: Boolean = false,
    openConversationId: String? = null,
    onVibeClick: () -> Unit = {},
    hasUnreadNotifications: Boolean = false,
    onNotificationClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val userKey = remember { FirebaseAuth.getInstance().currentUser?.uid?.takeIf { it.isNotBlank() } ?: "" }
    val cloudChatRepository = remember { CloudChatRepository() }

    var input by remember { mutableStateOf("") }
    var messages by remember(userKey, startNewChat) { mutableStateOf(emptyList<ChatMessage>()) }
    var isSending by remember { mutableStateOf(false) }
    var chatActivityState by remember { mutableStateOf(ChatActivityState.IDLE) }
    var conversationId by rememberSaveable(userKey, startNewChat) { mutableStateOf<String?>(null) }
    var pendingFileId by remember { mutableStateOf<String?>(null) }
    var pendingFileName by remember { mutableStateOf<String?>(null) }
    var pendingAttachmentUri by remember { mutableStateOf<Uri?>(null) }
    var cameraCaptureUri by remember { mutableStateOf<Uri?>(null) }
    var isPlusMenuOpen by remember { mutableStateOf(false) }
    var isListening by remember { mutableStateOf(false) }
    var voiceInputBase by remember { mutableStateOf("") }
    var voiceError by remember { mutableStateOf<String?>(null) }
    var isLoadingHistory by remember { mutableStateOf(!startNewChat) }
    var welcomeMessageIndex by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    val chatListState = rememberLazyListState()

    LaunchedEffect(messages.isEmpty()) {
        if (messages.isEmpty()) {
            welcomeMessageIndex = 0
            while (true) {
                kotlinx.coroutines.delay(2000)
                welcomeMessageIndex = 1 - welcomeMessageIndex
            }
        }
    }

    LaunchedEffect(isLoadingHistory, messages.size, chatActivityState, startNewChat, openConversationId) {
        if (!isLoadingHistory && (messages.isNotEmpty() || chatActivityState != ChatActivityState.IDLE)) {
            // Wait for Compose to measure the newest content. Use a large positive
            // scroll offset on the final item so the viewport is clamped to the
            // actual list bottom, not merely the top of a long final message.
            repeat(3) {
                withFrameNanos { }
                val lastItemIndex = chatListState.layoutInfo.totalItemsCount - 1
                if (lastItemIndex >= 0) {
                    chatListState.scrollToItem(
                        lastItemIndex,
                        scrollOffset = Int.MAX_VALUE / 4,
                    )
                }
            }
        }
    }

    LaunchedEffect(userKey, startNewChat, openConversationId) {
        if (userKey.isBlank() || startNewChat) {
            conversationId = null
            messages = emptyList()
            isLoadingHistory = false
            return@LaunchedEffect
        }
        isLoadingHistory = true
        runCatching {
            cloudChatRepository.cleanupExpiredConversations()
            if (!openConversationId.isNullOrBlank()) {
                cloudChatRepository.loadConversation(openConversationId)
            } else {
                cloudChatRepository.loadLatestChat()
            }
        }.onSuccess { chat ->
            conversationId = chat?.id
            messages = chat?.messages?.map {
                ChatMessage(it.content, it.role == "user")
            }.orEmpty()
        }.onFailure {
            conversationId = null
            messages = emptyList()
        }
        isLoadingHistory = false
    }

    fun sendMessage() {
        val typed = input.trim()
        val attachmentUri = pendingAttachmentUri
        val attachmentName = pendingFileName
        val fileId = pendingFileId
        val hasPendingAttachment = attachmentUri != null || fileId != null
        if (isSending || (typed.isEmpty() && !hasPendingAttachment)) return
        if (typed.isEmpty()) {
            messages = messages + ChatMessage(
                "Type a question about ${attachmentName ?: "this file"} before sending.",
                false,
            )
            return
        }

        val requestConversationId = conversationId ?: UUID.randomUUID().toString()
        messages = messages + ChatMessage(typed, true)
        input = ""
        conversationId = requestConversationId
        isSending = true
        chatActivityState = ChatActivityState.RECEIVED

        scope.launch {
            val receivedStage = async {
                delay(350L)
            }

            val userSaveDeferred = async(Dispatchers.IO) {
                runCatching {
                    cloudChatRepository.appendMessage(
                        conversationId = requestConversationId,
                        role = "user",
                        content = typed,
                    )
                }
            }

            val backendDeferred = async(Dispatchers.IO) {
                runCatching {
                    if (attachmentUri != null) {
                        val filename = attachmentName?.takeIf { it.isNotBlank() } ?: "attachment"
                        val mimeType = supportedAttachmentMimeType(context, attachmentUri, filename)
                        val bytes = readLimitedBytes(context, attachmentUri, MAX_CHAT_ATTACHMENT_BYTES)
                        ChatApi.sendMessage(
                            message = typed,
                            conversationId = requestConversationId,
                            fileId = null,
                            attachmentFilename = filename,
                            attachmentMimeType = mimeType,
                            attachmentBytes = bytes,
                        )
                    } else {
                        ChatApi.sendMessage(typed, requestConversationId, fileId)
                    }
                }
            }

            receivedStage.await()
            chatActivityState = ChatActivityState.THINKING
            val backendResult = backendDeferred.await()

            backendResult.onSuccess { response ->
                conversationId = response.conversationId
                if (pendingAttachmentUri == attachmentUri && pendingFileId == fileId) {
                    pendingAttachmentUri = null
                    pendingFileId = null
                    pendingFileName = null
                }
                chatActivityState = ChatActivityState.PREPARING
                delay(600L)

                messages = messages + ChatMessage(response.reply, false)
                chatActivityState = ChatActivityState.IDLE

                var userSave = userSaveDeferred.await()
                if (userSave.isFailure) {
                    userSave = runCatching {
                        withContext(Dispatchers.IO) {
                            cloudChatRepository.appendMessage(
                                conversationId = requestConversationId,
                                role = "user",
                                content = typed,
                            )
                        }
                    }
                }

                if (userSave.isFailure) {
                    val reason = userSave.exceptionOrNull()?.message
                        ?.takeIf { it.isNotBlank() }
                        ?: "Unknown Firebase error."
                    messages = messages + ChatMessage(
                        "Chat reply received, but Firebase could not save this message: $reason",
                        false,
                    )
                } else {
                    val assistantSave = runCatching {
                        withContext(Dispatchers.IO) {
                            cloudChatRepository.appendMessage(
                                conversationId = response.conversationId,
                                role = "assistant",
                                content = response.reply,
                            )
                        }
                    }

                    if (assistantSave.isFailure) {
                        val reason = assistantSave.exceptionOrNull()?.message
                            ?.takeIf { it.isNotBlank() }
                            ?: "Unknown Firebase error."
                        messages = messages + ChatMessage(
                            "Chat reply received, but Firebase could not save the AI reply: $reason",
                            false,
                        )
                    }
                }
            }.onFailure { error ->
                receivedStage.await()
                chatActivityState = ChatActivityState.IDLE
                val userSave = userSaveDeferred.await()
                if (userSave.isFailure) {
                    val reason = userSave.exceptionOrNull()?.message
                        ?.takeIf { it.isNotBlank() }
                        ?: "Unknown Firebase error."
                    messages = messages + ChatMessage(
                        "Message could not be saved to Firebase: $reason",
                        false,
                    )
                }
                if (input.isBlank()) input = typed
                messages = messages + ChatMessage(
                    error.message ?: "Indoone AI could not complete the request.",
                    false,
                )
            }

            isSending = false
            chatActivityState = ChatActivityState.IDLE
        }
    }

    fun displayNameForUri(uri: Uri, fallback: String): String =
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
        }?.takeIf { it.isNotBlank() } ?: fallback

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val capturedUri = cameraCaptureUri
        cameraCaptureUri = null
        if (success && capturedUri != null) {
            pendingAttachmentUri = capturedUri
            pendingFileId = null
            pendingFileName = "Camera photo"
        } else {
            capturedUri?.let { runCatching { context.contentResolver.delete(it, null, null) } }
        }
    }

    val photosLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            pendingAttachmentUri = uri
            pendingFileId = null
            pendingFileName = displayNameForUri(uri, "Photo")
        }
    }

    val filesLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            pendingAttachmentUri = null
            pendingFileId = null
            pendingFileName = null
            val name = displayNameForUri(uri, "file")
            val extension = name.substringAfterLast('.', "").lowercase()
            if (extension in setOf("txt", "md", "json", "csv", "log")) {
                runCatching {
                    val bytes = withContext(Dispatchers.IO) { readLimitedBytes(context, uri) }
                    val upload = withContext(Dispatchers.IO) { ChatApi.uploadTextFile(name, bytes) }
                    name to upload.fileId
                }.onSuccess { (uploadedName, fileId) ->
                    pendingAttachmentUri = null
                    pendingFileName = uploadedName
                    pendingFileId = fileId
                    messages = messages + ChatMessage("Attached $uploadedName. Ask me about this file.", true)
                }.onFailure { error ->
                    pendingFileName = null
                    pendingFileId = null
                    messages = messages + ChatMessage(error.message ?: "Could not attach that file.", false)
                }
            } else {
                // Photo/PDF/office/video/audio files are selected in the UI now.
                // Their authenticated upload + model processing is added in the backend phase.
                pendingAttachmentUri = uri
                pendingFileName = name
                messages = messages + ChatMessage("Selected $name.", true)
            }
        }
    }

    fun launchCameraCapture() {
        runCatching {
            val cameraDirectory = File(context.cacheDir, "camera").apply { mkdirs() }
            val captureFile = File(cameraDirectory, "indoone-camera-${UUID.randomUUID()}.jpg")
            val captureUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                captureFile,
            )
            cameraCaptureUri = captureUri
            cameraLauncher.launch(captureUri)
        }.onFailure { error ->
            messages = messages + ChatMessage(error.message ?: "Could not open the camera.", false)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            launchCameraCapture()
        } else {
            messages = messages + ChatMessage(
                "Camera permission is required. Allow Camera in Android Settings > Apps > Indoone > Permissions.",
                false,
            )
        }
    }

    val speechRecognizer = remember(context) {
        if (SpeechRecognizer.isRecognitionAvailable(context)) SpeechRecognizer.createSpeechRecognizer(context) else null
    }

    fun stopVoiceInput() {
        speechRecognizer?.stopListening()
    }

    fun startVoiceInput() {
        if (isSending) return
        if (speechRecognizer == null) {
            voiceError = "Speech recognition is not available on this device."
            return
        }

        voiceError = null
        voiceInputBase = input.trimEnd()
        isListening = true
        speechRecognizer.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        })
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) startVoiceInput() else voiceError = "Microphone permission is required."
    }

    DisposableEffect(speechRecognizer) {
        if (speechRecognizer == null) {
            onDispose { }
        } else {
            speechRecognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    isListening = true
                }

                override fun onBeginningOfSpeech() {
                    isListening = true
                }

                override fun onRmsChanged(rmsdB: Float) = Unit

                override fun onBufferReceived(buffer: ByteArray?) = Unit

                override fun onEndOfSpeech() {
                    // Keep the Voice button visible until final recognition results
                    // arrive. This prevents Send from appearing over partial speech.
                }

                override fun onError(error: Int) {
                    isListening = false
                    voiceError = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> "I could not hear that clearly. Try again."
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected. Try again."
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required."
                        else -> "Voice input failed. Please try again."
                    }
                }

                override fun onResults(results: Bundle?) {
                    isListening = false
                    val spoken = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                    if (!spoken.isNullOrBlank()) input = appendVoiceTranscript(voiceInputBase, spoken)
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val spoken = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                    if (!spoken.isNullOrBlank()) input = appendVoiceTranscript(voiceInputBase, spoken)
                }

                override fun onEvent(eventType: Int, params: Bundle?) = Unit
            })
            onDispose {
                speechRecognizer.cancel()
                speechRecognizer.destroy()
            }
        }
    }

    fun onVoiceButtonClick() {
        val activity = context as? Activity ?: return
        if (isListening) {
            stopVoiceInput()
            return
        }
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            startVoiceInput()
        } else if (!activity.isFinishing) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Box(Modifier.fillMaxSize().background(Color.White)) {
        Column(Modifier.fillMaxSize()) {
            AppTopBar(
                onMenuClick = onMenuClick,
                hasUnreadNotifications = hasUnreadNotifications,
                onNotificationsClick = onNotificationClick,
            )
            Box(Modifier.fillMaxWidth().weight(1f)) {
            if (isLoadingHistory) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFF703BE2),
                        strokeWidth = 2.2.dp,
                    )
                }
            } else LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 12.dp),
                verticalArrangement = if (messages.isEmpty()) Arrangement.Center else Arrangement.spacedBy(10.dp),
            ) {
                if (messages.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 26.dp, bottom = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                "Indoone AI",
                                modifier = Modifier.padding(top = 12.dp),
                                color = Color(0xFF703BE2),
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                "Your Personal AI Assistant",
                                modifier = Modifier.padding(top = 4.dp),
                                color = Color(0xFF81758D),
                                fontSize = 12.sp,
                            )
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 18.dp),
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFFFAF8FF),
                                border = BorderStroke(1.dp, Color(0xFFE9E0F7)),
                            ) {
                                Column(Modifier.padding(horizontal = 18.dp, vertical = 15.dp)) {
                                    Text(
                                        "Hi! 👋",
                                        color = Color(0xFF352742),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Text(
                                        if (welcomeMessageIndex == 0) {
                                            "I am Indoone AI. How can I help you today?"
                                        } else {
                                            "Ask me anything."
                                        },
                                        modifier = Modifier.padding(top = 3.dp),
                                        color = Color(0xFF6F6578),
                                        fontSize = 13.sp,
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 14.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                QuickPromptChip("Explain this") { input = "Explain this" }
                                QuickPromptChip("Create image") { input = "Create image" }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                QuickPromptChip("Help me plan") { input = "Help me plan" }
                                QuickPromptChip("More…") { input = "Tell me more" }
                            }
                        }
                    }
                }

                items(messages) { message ->
                    if (message.fromUser) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            Surface(
                                shape = RoundedCornerShape(
                                    topStart = 18.dp,
                                    topEnd = 18.dp,
                                    bottomStart = 18.dp,
                                    bottomEnd = 5.dp,
                                ),
                                color = Color(0xFF703BE2),
                            ) {
                                Text(
                                    message.text,
                                    Modifier.padding(horizontal = 15.dp, vertical = 12.dp),
                                    color = Color.White,
                                    fontSize = 13.sp,
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            IndooneHomeLogo(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(24.dp)
                                    .align(Alignment.Bottom),
                            )

                            Surface(
                                shape = RoundedCornerShape(
                                    topStart = 18.dp,
                                    topEnd = 18.dp,
                                    bottomStart = 5.dp,
                                    bottomEnd = 18.dp,
                                ),
                                color = Color(0xFFF5F2FB),
                            ) {
                                Text(
                                    message.text,
                                    Modifier.padding(horizontal = 15.dp, vertical = 12.dp),
                                    color = Color(0xFF292331),
                                    fontSize = 13.sp,
                                )
                            }
                        }
                    }
                }
                if (chatActivityState != ChatActivityState.IDLE) {
                    item(key = "indoone-chat-status") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            IndooneHomeLogo(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(24.dp)
                                    .align(Alignment.Bottom),
                            )
                            Surface(
                                shape = RoundedCornerShape(
                                    topStart = 18.dp,
                                    topEnd = 18.dp,
                                    bottomStart = 5.dp,
                                    bottomEnd = 18.dp,
                                ),
                                color = Color(0xFFF5F2FB),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 15.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        color = Color(0xFF703BE2),
                                        strokeWidth = 1.8.dp,
                                    )
                                    Text(
                                        chatActivityState.label(),
                                        color = Color(0xFF6F6578),
                                        fontSize = 13.sp,
                                    )
                                }
                            }
                        }
                    }
                }

            }

            if (isPlusMenuOpen) {
                PlusAttachmentSheet(
                    onDismiss = { isPlusMenuOpen = false },
                    onCamera = {
                        isPlusMenuOpen = false
                        val cameraPermissionGranted = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA,
                        ) == PackageManager.PERMISSION_GRANTED
                        if (cameraPermissionGranted) {
                            launchCameraCapture()
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    onPhotos = {
                        isPlusMenuOpen = false
                        photosLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    onFiles = {
                        isPlusMenuOpen = false
                        filesLauncher.launch(arrayOf("*/*"))
                    },
                )
            }
            }

            pendingFileName?.let { fileName ->
                val attachmentUri = pendingAttachmentUri
                val detectedMime = attachmentUri?.let { uri ->
                    runCatching { context.contentResolver.getType(uri) }.getOrNull()
                }
                val extension = fileName.substringAfterLast('.', "").lowercase()
                val isPhoto = attachmentUri != null && (
                    detectedMime?.startsWith("image/") == true ||
                        extension in setOf("jpg", "jpeg", "png", "webp", "gif", "heic", "heif")
                    )
                PendingAttachmentPreview(
                    uri = attachmentUri,
                    fileName = fileName,
                    isPhoto = isPhoto,
                    onRemove = {
                        val cameraFileName = pendingAttachmentUri
                            ?.takeIf { it.authority == "${context.packageName}.fileprovider" }
                            ?.lastPathSegment
                            ?.takeIf { it.startsWith("indoone-camera-") && it.endsWith(".jpg") }
                        cameraFileName?.let { File(File(context.cacheDir, "camera"), it).delete() }
                        pendingAttachmentUri = null
                        pendingFileId = null
                        pendingFileName = null
                    },
                )
            }

            Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.padding(end = 8.dp).size(42.dp).clip(CircleShape).background(Color(0xFFF5F2FB)).clickable(enabled = !isSending) {
                        isPlusMenuOpen = true
                    },
                    contentAlignment = Alignment.Center,
                ) { Text("+", color = Color(0xFF703BE2), fontSize = 25.sp, fontWeight = FontWeight.Medium) }

                Surface(
                    modifier = Modifier.weight(1f).pointerInput(isPlusMenuOpen) {
                        if (isPlusMenuOpen) {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                while (true) {
                                    // Watch the tap without consuming it, so the text field can
                                    // still gain focus and open the keyboard normally.
                                    val event = awaitPointerEvent(PointerEventPass.Initial)
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                    if ((change.position - down.position).getDistance() > viewConfiguration.touchSlop) {
                                        break
                                    }
                                    if (change.previousPressed && !change.pressed) {
                                        isPlusMenuOpen = false
                                        break
                                    }
                                    if (!change.pressed) break
                                }
                            }
                        }
                    },
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFFFAF9FC),
                    border = BorderStroke(1.dp, Color(0xFFE5E0ED)),
                ) {
                    BasicTextField(
                        value = input,
                        onValueChange = { input = it },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 13.dp),
                        // Keep the composer available while an AI reply is in progress.
                        // The send action remains locked until the current reply finishes.
                        enabled = true,
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color(0xFF17151D), fontSize = 14.sp),
                        decorationBox = { innerTextField -> Box { if (input.isEmpty()) Text("Message Indoone AI", color = Color(0xFF8A8492), fontSize = 14.sp); innerTextField() } },
                    )
                }

                val hasPendingAttachment = pendingAttachmentUri != null || pendingFileId != null
                val showSendButton = (input.isNotBlank() || hasPendingAttachment) &&
                    (!isListening || hasPendingAttachment)
                val canSend = showSendButton && !isSending && !isListening && input.isNotBlank()

                if (hasPendingAttachment) {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (isListening) Color(0xFF703BE2) else Color(0xFFF5F2FB))
                            .clickable(enabled = !isSending) {
                                isPlusMenuOpen = false
                                onVoiceButtonClick()
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Mic,
                            contentDescription = if (isListening) "Stop voice input" else "Voice input",
                            tint = if (isListening) Color.White else Color(0xFF4A4650),
                            modifier = Modifier.size(21.dp),
                        )
                    }
                } else if (showSendButton) {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (canSend) Color(0xFF703BE2) else Color(0xFFE7E3EC))
                            .clickable(enabled = canSend, onClick = ::sendMessage),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(
                                Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.2.dp,
                            )
                        } else {
                            Text(
                                "➤",
                                color = if (canSend) Color.White else Color(0xFF9B95A3),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (isListening) Color(0xFF703BE2) else Color(0xFFF5F2FB))
                            .clickable(enabled = !isSending) {
                                isPlusMenuOpen = false
                                onVoiceButtonClick()
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Mic,
                            contentDescription = if (isListening) "Stop voice input" else "Voice input",
                            tint = if (isListening) Color.White else Color(0xFF4A4650),
                            modifier = Modifier.size(21.dp),
                        )
                    }
                }

                if (hasPendingAttachment) {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (canSend) Color(0xFF703BE2) else Color(0xFFE7E3EC))
                            .clickable(enabled = canSend, onClick = ::sendMessage),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(
                                Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.2.dp,
                            )
                        } else {
                            Text(
                                "➤",
                                color = if (canSend) Color.White else Color(0xFF9B95A3),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                } else if (input.isBlank() && !isListening) {
                    VibeButton(
                        onClick = {
                            isPlusMenuOpen = false
                            onVibeClick()
                        },
                    )
                }
            }

            voiceError?.let { error ->
                Text(
                    error,
                    color = Color(0xFFB42318),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 2.dp),
                )
            }

            if (isListening) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF5F2FB),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier.size(34.dp).clip(CircleShape).background(Color(0xFF703BE2)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Mic,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        Text(
                            "Listening… tap mic to stop",
                            modifier = Modifier.padding(start = 10.dp),
                            color = Color(0xFF4A4650),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }

            AppBottomNav(activeTab = AppTab.HOME, onAccountsClick = {}, onLobbyClick = onLobbyClick, onConnectClick = onConnectClick, onSettingsClick = onSettingsClick)
        }
    }
}
