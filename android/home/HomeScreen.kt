package com.indoone.home

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.indoone.menu.AppBottomNav
import com.indoone.menu.AppTab
import com.indoone.menu.AppTopBar
import com.indoone.home.message.ChatApi
import com.indoone.home.message.ChatApiException
import com.indoone.home.message.ChatMediaAttachment
import com.indoone.home.message.CloudChatRepository
import com.indoone.home.plus.PlusAttachmentSheet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID

private const val MAX_UPLOAD_BYTES = 2_000_000
private const val MAX_CHAT_IMAGE_BYTES = 5_000_000
private const val MAX_CHAT_DOCUMENT_BYTES = 8_000_000
private const val MAX_CHAT_TOTAL_ATTACHMENT_BYTES = 20_000_000
private const val MAX_CHAT_ATTACHMENTS = 10

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

private fun decodeAttachmentThumbnail(
    context: Context,
    uri: Uri,
    maxDimension: Int = 180,
): Bitmap? {
    return try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sampleSize = 1
        while (bounds.outWidth / sampleSize > maxDimension || bounds.outHeight / sampleSize > maxDimension) {
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

private fun isPhotoAttachment(context: Context, attachment: ChatAttachment): Boolean {
    val uri = attachment.uri ?: return false
    val mime = runCatching { context.contentResolver.getType(uri)?.lowercase() }.getOrNull()
    val extension = attachment.displayName.substringAfterLast('.', "").lowercase()
    return mime?.startsWith("image/") == true ||
        extension in setOf("jpg", "jpeg", "png", "webp", "gif", "heic", "heif")
}

@Composable
private fun AttachmentImage(
    uri: Uri,
    description: String,
    modifier: Modifier = Modifier,
    thumbnailMaxDimension: Int = 180,
) {
    val context = LocalContext.current
    val thumbnail by produceState<Bitmap?>(
        initialValue = null,
        key1 = uri,
        key2 = thumbnailMaxDimension,
    ) {
        value = withContext(Dispatchers.IO) {
            decodeAttachmentThumbnail(context, uri, thumbnailMaxDimension)
        }
    }
    val bitmap = thumbnail
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = description,
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(RoundedCornerShape(9.dp)),
        )
    } else {
        Box(
            modifier = modifier.clip(RoundedCornerShape(9.dp)).background(Color(0xFFEDE7F8)),
            contentAlignment = Alignment.Center,
        ) {
            Text("IMG", color = Color(0xFF5E2DD2), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PendingAttachmentPreview(
    attachment: ChatAttachment,
    enabled: Boolean,
    onRemove: () -> Unit,
) {
    val context = LocalContext.current
    val isPhoto = isPhotoAttachment(context, attachment)
    Column(
        modifier = Modifier.width(82.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Box(Modifier.size(64.dp)) {
            if (isPhoto && attachment.uri != null) {
                AttachmentImage(attachment.uri, "Attached photo preview", Modifier.fillMaxSize())
            } else {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(9.dp),
                    color = Color(0xFFF0E9FB),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            attachment.displayName.substringAfterLast('.', "").take(4)
                                .uppercase().ifBlank { "FILE" },
                            color = Color(0xFF5E2DD2),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            Box(
                modifier = Modifier.align(Alignment.TopEnd).offset(x = 4.dp, y = (-4).dp)
                    .size(22.dp).clip(CircleShape).background(Color(0xFF292431))
                    .clickable(enabled = enabled, onClick = onRemove),
                contentAlignment = Alignment.Center,
            ) {
                Text("×", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
        Text(
            attachment.displayName,
            color = Color(0xFF5E2DD2),
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SentPhotoStack(photos: List<ChatAttachment>) {
    if (photos.isEmpty()) return

    // Keep the sent media compact and right-aligned. The image cards underneath
    // peek out below the front card instead of stretching a purple bubble across chat.
    val cardWidth = 220.dp
    val cardHeight = 156.dp
    // A half-card peek makes the stacked-photo treatment obvious without making
    // long messages huge: render at most three cards; the badge and gallery cover the rest.
    val cardPeek = 72.dp
    val visiblePhotos = photos.take(3)
    val stackHeight = cardHeight + cardPeek * (visiblePhotos.size - 1)

    var showPhotoGallery by remember(photos.map { it.id }) { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .width(cardWidth)
            .height(stackHeight)
            .clickable { showPhotoGallery = true },
    ) {
        // Draw back cards first so the first selected photo remains in front.
        visiblePhotos.indices.reversed().forEach { index ->
            val photo = visiblePhotos[index]
            val uri = photo.uri ?: return@forEach
            AttachmentImage(
                uri = uri,
                description = if (photos.size == 1) "Sent photo" else "Photo ${index + 1} of ${photos.size}",
                modifier = Modifier
                    .offset(y = cardPeek * index)
                    .width(cardWidth)
                    .height(cardHeight),
            )
        }
        if (photos.size > 1) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xB8000000),
            ) {
                Text(
                    "${photos.size} photos",
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }

    if (showPhotoGallery) {
        Dialog(
            onDismissRequest = { showPhotoGallery = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
                    .padding(16.dp),
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF17151D),
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 10.dp, top = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            "${photos.size} photos",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable { showPhotoGallery = false },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("×", color = Color.White, fontSize = 26.sp)
                        }
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentPadding = PaddingValues(10.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(photos, key = { it.id }) { photo ->
                            photo.uri?.let { uri ->
                                AttachmentImage(
                                    uri = uri,
                                    description = "Photo: ${photo.displayName}",
                                    modifier = Modifier.fillMaxWidth().height(280.dp),
                                    thumbnailMaxDimension = 1000,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SentAttachmentContent(attachments: List<ChatAttachment>) {
    val context = LocalContext.current
    val photos = attachments.filter { it.uri != null && isPhotoAttachment(context, it) }
    val otherFiles = attachments.filterNot { it.uri != null && isPhotoAttachment(context, it) }

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (photos.isNotEmpty()) {
            SentPhotoStack(photos)
        }
        otherFiles.forEach { attachment ->
            Surface(
                modifier = Modifier.widthIn(max = 220.dp),
                shape = RoundedCornerShape(9.dp),
                color = Color(0xFF8055DC),
                border = BorderStroke(1.dp, Color(0xFFB69AEF)),
            ) {
                Text(
                    attachment.displayName,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                    color = Color.White,
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
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

private data class ChatAttachment(
    val displayName: String,
    val uri: Uri? = null,
    val fileId: String? = null,
    val id: String = UUID.randomUUID().toString(),
)

private data class ChatMessage(
    val text: String,
    val fromUser: Boolean,
    val attachments: List<ChatAttachment> = emptyList(),
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
    var pendingAttachments by remember { mutableStateOf<List<ChatAttachment>>(emptyList()) }
    var cameraCaptureUri by remember { mutableStateOf<Uri?>(null) }
    var isPlusMenuOpen by remember { mutableStateOf(false) }
    var isAddingAttachments by remember { mutableStateOf(false) }
    var isListening by remember { mutableStateOf(false) }
    var voiceInputBase by remember { mutableStateOf("") }
    var voicePreview by remember { mutableStateOf("") }
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
            // A large positive scroll offset on the final item clamps the viewport to the
            // real list bottom, rather than to the top of a long final message.
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
        val attachmentsToSend = pendingAttachments.toList()
        val hasPendingAttachment = attachmentsToSend.isNotEmpty()
        if (isSending || isAddingAttachments || (typed.isEmpty() && !hasPendingAttachment)) return

        val messageToBackend = typed.ifBlank {
            "Please inspect the attached file(s) and describe their contents or visible details."
        }
        val requestConversationId = conversationId ?: UUID.randomUUID().toString()
        messages = messages + ChatMessage(typed, true, attachmentsToSend)
        input = ""
        conversationId = requestConversationId
        isSending = true
        chatActivityState = ChatActivityState.RECEIVED

        val attachmentSummary = attachmentsToSend
            .takeIf { it.isNotEmpty() }
            ?.joinToString(", ") { it.displayName }
            ?.let { "[Attachments: $it]" }
        val persistedUserContent = listOf(typed, attachmentSummary.orEmpty())
            .filter { it.isNotBlank() }
            .joinToString("\n")

        scope.launch {
            val receivedStage = async { delay(350L) }
            val userSaveDeferred = async(Dispatchers.IO) {
                runCatching {
                    cloudChatRepository.appendMessage(
                        conversationId = requestConversationId,
                        role = "user",
                        content = persistedUserContent,
                    )
                }
            }
            val backendDeferred = async(Dispatchers.IO) {
                runCatching {
                    val fileIds = attachmentsToSend.mapNotNull { it.fileId }
                    val mediaPayloads = mutableListOf<ChatMediaAttachment>()
                    var totalBytes = 0
                    for (attachment in attachmentsToSend) {
                        val uri = attachment.uri ?: continue
                        val mimeType = supportedAttachmentMimeType(context, uri, attachment.displayName)
                        val maxAttachmentBytes = if (mimeType.startsWith("image/")) {
                            MAX_CHAT_IMAGE_BYTES
                        } else {
                            MAX_CHAT_DOCUMENT_BYTES
                        }
                        val bytes = readLimitedBytes(context, uri, maxAttachmentBytes)
                        totalBytes += bytes.size
                        if (totalBytes > MAX_CHAT_TOTAL_ATTACHMENT_BYTES) {
                            throw IllegalArgumentException(
                                "Attachments together exceed the 20 MB upload limit. Remove or reduce some files and try again."
                            )
                        }
                        mediaPayloads += ChatMediaAttachment(
                            filename = attachment.displayName,
                            mimeType = mimeType,
                            bytes = bytes,
                        )
                    }
                    ChatApi.sendMessage(
                        message = messageToBackend,
                        conversationId = requestConversationId,
                        fileIds = fileIds,
                        mediaAttachments = mediaPayloads,
                    )
                }
            }

            receivedStage.await()
            chatActivityState = ChatActivityState.THINKING
            val backendResult = backendDeferred.await()

            backendResult.onSuccess { response ->
                conversationId = response.conversationId
                if (pendingAttachments.map { it.id } == attachmentsToSend.map { it.id }) {
                    pendingAttachments = emptyList()
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
                                content = persistedUserContent,
                            )
                        }
                    }
                }
                if (userSave.isFailure) {
                    val reason = userSave.exceptionOrNull()?.message
                        ?.takeIf { it.isNotBlank() }
                        ?: "Unknown cloud error."
                    messages = messages + ChatMessage(
                        "Chat reply received, but this message could not be saved to the cloud: $reason",
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
                            ?: "Unknown cloud error."
                        messages = messages + ChatMessage(
                            "Chat reply received, but the reply could not be saved to the cloud: $reason",
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
                        ?: "Unknown cloud error."
                    messages = messages + ChatMessage(
                        "Message could not be saved to the cloud: $reason",
                        false,
                    )
                }
                if (input.isBlank()) input = typed
                val failureReply = error.message ?: "Indoone AI could not complete the request."
                messages = messages + ChatMessage(failureReply, false)
                // A reply the backend actually sent is part of the conversation, so
                // save it: refreshing the chat then shows what was said at the time.
                if ((error as? ChatApiException)?.statusCode != null) {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            cloudChatRepository.appendMessage(
                                conversationId = requestConversationId,
                                role = "assistant",
                                content = failureReply,
                            )
                        }
                    }
                }
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
            if (pendingAttachments.size >= MAX_CHAT_ATTACHMENTS) {
                val fileName = capturedUri.lastPathSegment
                    ?.takeIf { capturedUri.authority == "${context.packageName}.fileprovider" }
                fileName?.let { File(File(context.cacheDir, "camera"), it).delete() }
                messages = messages + ChatMessage("You can attach up to 10 files per message.", false)
            } else {
                pendingAttachments = pendingAttachments + ChatAttachment(
                    displayName = "Camera photo",
                    uri = capturedUri,
                )
            }
        } else {
            val fileName = capturedUri?.lastPathSegment
                ?.takeIf { capturedUri.authority == "${context.packageName}.fileprovider" }
            fileName?.let { File(File(context.cacheDir, "camera"), it).delete() }
        }
    }

    val photosLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(MAX_CHAT_ATTACHMENTS),
    ) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        val remaining = MAX_CHAT_ATTACHMENTS - pendingAttachments.size
        if (remaining <= 0) {
            messages = messages + ChatMessage("You can attach up to 10 files per message.", false)
            return@rememberLauncherForActivityResult
        }
        val selected = uris.take(remaining)
        if (selected.size < uris.size) {
            messages = messages + ChatMessage(
                "You can attach up to 10 files per message. Only the remaining $remaining item(s) were added.",
                false,
            )
        }
        pendingAttachments = pendingAttachments + selected.map { uri ->
            ChatAttachment(displayName = displayNameForUri(uri, "Photo"), uri = uri)
        }
    }

    val filesLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        val remaining = MAX_CHAT_ATTACHMENTS - pendingAttachments.size
        if (remaining <= 0) {
            messages = messages + ChatMessage("You can attach up to 10 files per message.", false)
            return@rememberLauncherForActivityResult
        }
        val selected = uris.take(remaining)
        if (selected.size < uris.size) {
            messages = messages + ChatMessage(
                "You can attach up to 10 files per message. Only the remaining $remaining item(s) were added.",
                false,
            )
        }
        scope.launch {
            isAddingAttachments = true
            try {
                for (uri in selected) {
                    val name = displayNameForUri(uri, "file")
                    val extension = name.substringAfterLast('.', "").lowercase()
                    if (extension in setOf("txt", "md", "json", "csv", "log")) {
                        runCatching {
                            val bytes = withContext(Dispatchers.IO) { readLimitedBytes(context, uri) }
                            val upload = withContext(Dispatchers.IO) { ChatApi.uploadTextFile(name, bytes) }
                            ChatAttachment(
                                displayName = upload.filename.ifBlank { name },
                                fileId = upload.fileId,
                            )
                        }.onSuccess { attachment ->
                            pendingAttachments = (pendingAttachments + attachment).take(MAX_CHAT_ATTACHMENTS)
                        }.onFailure { error ->
                            messages = messages + ChatMessage(
                                error.message ?: "Could not attach $name.",
                                false,
                            )
                        }
                    } else {
                        val supported = runCatching { supportedAttachmentMimeType(context, uri, name) }
                        if (supported.isSuccess) {
                            pendingAttachments = (
                                pendingAttachments + ChatAttachment(displayName = name, uri = uri)
                            ).take(MAX_CHAT_ATTACHMENTS)
                        } else {
                            messages = messages + ChatMessage(
                                supported.exceptionOrNull()?.message
                                    ?: "This attachment type is not supported: $name",
                                false,
                            )
                        }
                    }
                }
            } finally {
                isAddingAttachments = false
            }
        }
    }

    fun launchCameraCapture() {
        if (pendingAttachments.size >= MAX_CHAT_ATTACHMENTS) {
            messages = messages + ChatMessage("You can attach up to 10 files per message.", false)
            return
        }
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
        voicePreview = ""
        isListening = true
        speechRecognizer.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)

            // Prefer well-formatted final text without forcing English as the language.
            if (Build.VERSION.SDK_INT >= 33) {
                putExtra(
                    RecognizerIntent.EXTRA_ENABLE_FORMATTING,
                    RecognizerIntent.FORMATTING_OPTIMIZE_QUALITY,
                )
            }

            // Android 14+ supports automatic spoken-language detection and switching.
            // Keep English, Kannada, and Telugu available as recognition choices.
            if (Build.VERSION.SDK_INT >= 34) {
                val supportedVoiceLanguages = arrayListOf("en-IN", "en-US", "kn-IN", "te-IN")
                putExtra(RecognizerIntent.EXTRA_ENABLE_LANGUAGE_DETECTION, true)
                putStringArrayListExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_DETECTION_ALLOWED_LANGUAGES,
                    ArrayList(supportedVoiceLanguages),
                )
                putExtra(
                    RecognizerIntent.EXTRA_ENABLE_LANGUAGE_SWITCH,
                    RecognizerIntent.LANGUAGE_SWITCH_BALANCED,
                )
                putStringArrayListExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_SWITCH_ALLOWED_LANGUAGES,
                    ArrayList(supportedVoiceLanguages),
                )
            }
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
                    voicePreview = ""
                    voiceError = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> "I could not hear that clearly. Try again."
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected. Try again."
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required."
                        else -> "Voice input failed. Please try again."
                    }
                }

                override fun onResults(results: Bundle?) {
                    // The one and only time the message box is written: the final text,
                    // once, after you have finished speaking.
                    isListening = false
                    voicePreview = ""
                    val spoken = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                    if (!spoken.isNullOrBlank()) input = appendVoiceTranscript(voiceInputBase, spoken)
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    // Partial speech goes to the preview chip only. The message box is
                    // left exactly as it was, so nothing is typed or rewritten while
                    // you are still talking.
                    val spoken = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                    voicePreview = spoken.orEmpty()
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
                // The scroll effect above drives this state. Without it the list ran on its
                // own internal state, so every conversation opened at the first message
                // and the scroll to the bottom went nowhere.
                state = chatListState,
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
                            Column(
                                modifier = Modifier.widthIn(max = 260.dp),
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                if (message.attachments.isNotEmpty()) {
                                    SentAttachmentContent(message.attachments)
                                }
                                if (message.text.isNotBlank()) {
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
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                            color = Color.White,
                                            fontSize = 15.sp,
                                        )
                                    }
                                }
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
                                Column(Modifier.padding(horizontal = 15.dp, vertical = 12.dp)) {
                                    // A reply that carries links reads better as its prose and
                                    // then one row per link, each with a copy button, than as
                                    // one run of text with the URLs buried inside it.
                                    val parts = remember(message.text) { splitReplyIntoParts(message.text) }
                                    parts.forEachIndexed { index, part ->
                                        val gap = if (index == 0) 0.dp else 8.dp
                                        when (part) {
                                            is ReplyPart.Prose -> Text(
                                                part.text,
                                                Modifier.padding(top = gap),
                                                color = Color(0xFF292331),
                                                fontSize = 15.sp,
                                            )
                                            is ReplyPart.Link -> ReplyLinkRow(
                                                url = part.url,
                                                modifier = Modifier.padding(top = gap),
                                            )
                                        }
                                    }
                                }
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
                        if (pendingAttachments.size >= MAX_CHAT_ATTACHMENTS) {
                            messages = messages + ChatMessage("You can attach up to 10 files per message.", false)
                        } else {
                            photosLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    },
                    onFiles = {
                        isPlusMenuOpen = false
                        if (pendingAttachments.size >= MAX_CHAT_ATTACHMENTS) {
                            messages = messages + ChatMessage("You can attach up to 10 files per message.", false)
                        } else {
                            filesLauncher.launch(arrayOf("*/*"))
                        }
                    },
                )
            }
            }

            if (pendingAttachments.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    pendingAttachments.forEach { attachment ->
                        PendingAttachmentPreview(
                            attachment = attachment,
                            enabled = !isSending,
                            onRemove = {
                                if (!isSending) {
                                    val cameraFileName = attachment.uri
                                        ?.takeIf { it.authority == "${context.packageName}.fileprovider" }
                                        ?.lastPathSegment
                                        ?.takeIf { it.startsWith("indoone-camera-") && it.endsWith(".jpg") }
                                    cameraFileName?.let { File(File(context.cacheDir, "camera"), it).delete() }
                                    pendingAttachments = pendingAttachments.filterNot { it.id == attachment.id }
                                }
                            },
                        )
                    }
                }
            }


            Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.padding(end = 8.dp).size(42.dp).clip(CircleShape).background(Color(0xFFF5F2FB)).clickable(enabled = !isSending && !isAddingAttachments) {
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
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color(0xFF17151D), fontSize = 15.sp),
                        decorationBox = { innerTextField -> Box { if (input.isEmpty()) Text("Message Indoone AI", color = Color(0xFF8A8492), fontSize = 15.sp); innerTextField() } },
                    )
                }

                val hasPendingAttachment = pendingAttachments.isNotEmpty()
                val showSendButton = input.isNotBlank() || hasPendingAttachment
                val canSend = showSendButton && !isSending && !isAddingAttachments && !isListening

                // Keep the microphone available even after text is typed. Voice input
                // can append to the draft, while Send appears alongside the microphone.
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

                if (showSendButton) {
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
                        Column(Modifier.padding(start = 10.dp)) {
                            Text(
                                "Listening… tap mic to stop",
                                color = Color(0xFF4A4650),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                            )
                            if (voicePreview.isNotBlank()) {
                                Text(
                                    voicePreview,
                                    modifier = Modifier.padding(top = 2.dp),
                                    color = Color(0xFF7A7482),
                                    fontSize = 12.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }

            AppBottomNav(activeTab = AppTab.HOME, onAccountsClick = {}, onLobbyClick = onLobbyClick, onVibeClick = onVibeClick, onSettingsClick = onSettingsClick)
        }
    }
}

private sealed interface ReplyPart {
    data class Prose(val text: String) : ReplyPart
    data class Link(val url: String) : ReplyPart
}

private val REPLY_LINK_PATTERN = Regex("""https?://\S+""")

/**
 * Splits a reply into prose and links, so each link can be given a row of its own.
 *
 * Blank prose is dropped: a reply that is nothing but a link should not leave an empty
 * line above it, and prose that was only the whitespace around a link should not become
 * a line of its own.
 */
private fun splitReplyIntoParts(text: String): List<ReplyPart> {
    val parts = mutableListOf<ReplyPart>()
    var cursor = 0
    REPLY_LINK_PATTERN.findAll(text).forEach { match ->
        val prose = text.substring(cursor, match.range.first).trim()
        if (prose.isNotEmpty()) parts += ReplyPart.Prose(prose)
        parts += ReplyPart.Link(match.value)
        cursor = match.range.last + 1
    }
    val tail = text.substring(cursor).trim()
    if (tail.isNotEmpty()) parts += ReplyPart.Prose(tail)
    return parts
}

/**
 * One link on its own row, with a copy button at the end of that row. The button puts
 * the whole URL on the clipboard and says so, because a tap with no visible result
 * reads as though it did nothing.
 */
@Composable
private fun ReplyLinkRow(url: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Surface(
        modifier = modifier.widthIn(max = 250.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE6DEF4)),
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 5.dp, top = 5.dp, bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                url,
                modifier = Modifier.weight(1f, fill = false),
                color = Color(0xFF6330DB),
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(32.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(Color(0xFFF3EEFC))
                    .clickable {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Indoone link", url))
                        Toast.makeText(context, "Link copied", Toast.LENGTH_SHORT).show()
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.ContentCopy,
                    contentDescription = "Copy link",
                    tint = Color(0xFF6330DB),
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
