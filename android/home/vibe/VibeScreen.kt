package com.indoone.home.vibe

import android.app.Activity
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.graphics.Color as AndroidColor
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.MicOff
import androidx.compose.material.icons.outlined.VolumeOff
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun VibeScreen(
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var isListening by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(true) }
    var connectionState by remember { mutableStateOf("Connecting") }
    var transcript by remember { mutableStateOf("Your live conversation will appear here.") }

    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val vibeClient = remember { VibeClient(scope = scope) }
    val audioEngine = remember {
        VibeAudioEngine(
            context = context,
            onCapturedPcm = { pcm -> vibeClient.sendAudio(pcm) },
            onPlaybackEnabled = { isSpeakerOn },
        )
    }

    fun startVibe() {
        scope.launch {
            connectionState = "Connecting"
            vibeClient.connect(
                onEvent = { event ->
                    when (event.optString("type")) {
                        "ready", "connected" -> {
                            connectionState = "Connected"
                            if (!audioEngine.isRecording()) {
                                audioEngine.startRecording()
                                isListening = audioEngine.isRecording()
                            }
                        }
                        "transcript" -> {
                            val text = event.optString("text").trim()
                            if (text.isNotBlank()) {
                                transcript = when (event.optString("role")) {
                                    "user" -> "You: $text"
                                    "assistant" -> "Indoone: $text"
                                    else -> text
                                }
                            }
                        }
                        "audio" -> {
                            if (isSpeakerOn) {
                                event.optString("audio_base64")
                                    .takeIf { it.isNotBlank() }
                                    ?.let(audioEngine::playResponse)
                            }
                        }