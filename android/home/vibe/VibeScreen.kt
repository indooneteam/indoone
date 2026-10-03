package com.indoone.home.vibe

import android.app.Activity
import android.Manifest
import android.content.Context
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
    val vibeClient = remember { VibeClient(context = context, scope = scope) }
    val audioEngine = remember {
        VibeAudioEngine(
            onCapturedPcm = { pcm -> vibeClient.sendAudio(pcm) },
            onPlaybackEnabled = { isSpeakerOn },
        )
    }

    fun startVibe() {
        scope.launch {
            connectionState = "Connecting"
            runCatching {
                vibeClient.connect(
                    onEvent = { event ->
                        when (event.optString("type")) {
                            "ready" -> {
                                connectionState = "Connected"
                            }
                            "connected" -> {
                                connectionState = "Connected"
                                if (!audioEngine.isRecording()) {
                                    audioEngine.startRecording()
                                    isListening = true
                                }
                            }
                            "transcript" -> {
                                val role = event.optString("role")
                                val text = event.optString("text").trim()
                                if (text.isNotBlank()) {
                                    transcript = when (role) {
                                        "user" -> "You: $text"
                                        "assistant" -> "Indoone: $text"
                                        else -> text
                                    }
                                }
                            }
                            "audio" -> {
                                if (isSpeakerOn) {
                                    event.optString("audio_base64").takeIf { it.isNotBlank() }?.let {
                                        audioEngine.playResponse(it)
                                    }
                                }
                            }
                            "interrupted" -> audioEngine.flushPlayback()
                            "error" -> {
                                connectionState = "Connection error"
                                isListening = false
                                transcript = event.optString("detail").ifBlank {
                                    "Vibe connection failed."
                                }
                            }
                        }
                    },
                    onFailure = { message ->
                        connectionState = "Connection error"
                        isListening = false
                        transcript = message
                    },
                )
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            startVibe()
        } else {
            connectionState = "Microphone permission required"
            transcript = "Microphone permission is required for Vibe."
        }
    }

    BackHandler(onBack = {
        audioEngine.stop()
        vibeClient.close()
        onClose()
    })

    DisposableEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startVibe()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        onDispose {
            audioEngine.stop()
            vibeClient.close()
        }
    }

    DisposableEffect(activity) {
        val window = activity?.window
        if (window == null) {
            onDispose { }
        } else {
            val oldStatusBar = window.statusBarColor
            val oldNavigationBar = window.navigationBarColor
            val controller = WindowInsetsControllerCompat(window, window.decorView)

            window.statusBarColor = AndroidColor.rgb(7, 5, 18)
            window.navigationBarColor = AndroidColor.rgb(7, 5, 18)
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = false

            onDispose {
                window.statusBarColor = oldStatusBar
                window.navigationBarColor = oldNavigationBar
                controller.isAppearanceLightStatusBars = true
                controller.isAppearanceLightNavigationBars = true
            }
        }
    }

    val transition = rememberInfiniteTransition(label = "vibe-screen")
    val pulse by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "vibe-core-pulse",
    )
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "vibe-ring-rotation",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF070512),
                        Color(0xFF0E0920),
                        Color(0xFF080612),
                    ),
                ),
            )
            .padding(
                top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding(),
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "I",
                    color = Color(0xFF9A82FF),
                    fontSize = 25.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                )
                Text(
                    "ndoone",
                    color = Color(0xFF9A82FF),
                    fontSize = 17.sp,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "Vibe",
                    color = Color.White,
                    fontSize = 24.sp,
                )
            }

            Spacer(Modifier.weight(0.45f))

            Box(
                modifier = Modifier.size(270.dp),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val base = size.minDimension * 0.33f

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFE8D9FF).copy(alpha = 0.96f),
                                Color(0xFFA86CFF).copy(alpha = 0.72f),
                                Color(0xFF6C39FF).copy(alpha = 0.18f),
                                Color.Transparent,
                            ),
                            center = center,
                            radius = size.minDimension * 0.48f,
                        ),
                        radius = size.minDimension * 0.48f,
                        center = center,
                    )

                    drawCircle(
                        color = Color(0xFFB77CFF).copy(alpha = 0.18f),
                        radius = base * 1.52f * pulse,
                        center = center,
                        style = Stroke(width = 3.dp.toPx()),
                    )

                    drawCircle(
                        color = Color(0xFF7F5BFF).copy(alpha = 0.26f),
                        radius = base * 1.20f,
                        center = center,
                        style = Stroke(width = 1.5.dp.toPx()),
                    )

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.98f),
                                Color(0xFFE1C9FF).copy(alpha = 0.96f),
                                Color(0xFF8C4DFF).copy(alpha = 0.62f),
                                Color.Transparent,
                            ),
                            center = center,
                            radius = base,
                        ),
                        radius = base * pulse,
                        center = center,
                    )

                    rotate(rotation, pivot = center) {
                        for (index in 0 until 8) {
                            val angle = Math.toRadians(index * 45.0)
                            val ringRadius = base * 1.42f
                            val point = Offset(
                                center.x + (ringRadius * cos(angle)).toFloat(),
                                center.y + (ringRadius * sin(angle)).toFloat(),
                            )
                            drawCircle(
                                color = if (index % 2 == 0) {
                                    Color(0xFFD8C4FF).copy(alpha = 0.80f)
                                } else {
                                    Color(0xFF6BC9FF).copy(alpha = 0.60f)
                                },
                                radius = if (index % 2 == 0) 4.dp.toPx() else 2.5.dp.toPx(),
                                center = point,
                            )
                        }
                    }

                    if (isListening) {
                        for (index in 0 until 5) {
                            val amplitude = (0.55f + index * 0.12f) * pulse
                            drawArc(
                                color = Color(0xFFB996FF).copy(alpha = 0.34f - index * 0.045f),
                                startAngle = 210f + index * 9f,
                                sweepAngle = 120f,
                                useCenter = false,
                                topLeft = Offset(
                                    center.x - base * (1.65f + index * 0.09f),
                                    center.y - base * (1.65f + index * 0.09f),
                                ),
                                size = androidx.compose.ui.geometry.Size(
                                    base * (3.3f + index * 0.18f) * amplitude,
                                    base * (3.3f + index * 0.18f) * amplitude,
                                ),
                                style = Stroke(
                                    width = (2.2f - index * 0.2f).dp.toPx(),
                                    cap = StrokeCap.Round,
                                ),
                            )
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        "VIBE",
                        color = Color(0xFF7C5CFF),
                        fontSize = 12.sp,
                        letterSpacing = 3.sp,
                    )
                    Text(
                        when {
                            connectionState != "Connected" -> connectionState
                            isListening -> "Listening"
                            else -> "Paused"
                        },
                        color = Color.White,
                        fontSize = 22.sp,
                    )
                    Text(
                        "Speak naturally",
                        color = Color.White.copy(alpha = 0.56f),
                        fontSize = 12.sp,
                    )
                }
            }

            Spacer(Modifier.weight(0.3f))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = Color.White.copy(alpha = 0.055f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Color.White.copy(alpha = 0.10f),
                ),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    Text(
                        "LIVE TRANSCRIPT",
                        color = Color(0xFFA78BFF),
                        fontSize = 10.sp,
                        letterSpacing = 1.6.sp,
                    )
                    Text(
                        transcript,
                        modifier = Modifier.padding(top = 6.dp),
                        color = Color.White.copy(alpha = 0.62f),
                        fontSize = 13.sp,
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                VibeControlButton(
                    icon = if (isListening) Icons.Outlined.Mic else Icons.Outlined.MicOff,
                    label = if (isListening) "Mute" else "Unmute",
                    active = isListening,
                    onClick = {
                        if (isListening) {
                            audioEngine.stopRecording()
                            isListening = false
                        } else {
                            if (connectionState == "Connected") {
                                audioEngine.startRecording()
                                isListening = true
                            }
                        }
                    },
                )

                Spacer(Modifier.width(14.dp))

                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color(0xFF9B5CFF),
                                    Color(0xFF6D3BDB),
                                    Color(0xFF3C216E),
                                ),
                            ),
                        )
                        .clickable(onClick = onClose),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "End Vibe",
                        tint = Color.White,
                        modifier = Modifier.size(27.dp),
                    )
                }

                Spacer(Modifier.width(14.dp))

                VibeControlButton(
                    icon = if (isSpeakerOn) Icons.Outlined.VolumeUp else Icons.Outlined.VolumeOff,
                    label = if (isSpeakerOn) "Speaker" else "Muted",
                    active = isSpeakerOn,
                    onClick = {
                        isSpeakerOn = !isSpeakerOn
                        if (!isSpeakerOn) {
                            audioEngine.flushPlayback()
                        }
                    },
                )
            }

            Spacer(
                modifier = Modifier
                    .height(16.dp)
                    .padding(bottom = 2.dp),
            )
        }
    }
}

@Composable
private fun VibeControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(78.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (active) Color.White.copy(alpha = 0.12f)
                    else Color.White.copy(alpha = 0.055f),
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (active) Color.White else Color.White.copy(alpha = 0.52f),
                modifier = Modifier.size(21.dp),
            )
        }
        Text(
            label,
            modifier = Modifier.padding(top = 6.dp),
            color = Color.White.copy(alpha = 0.52f),
            fontSize = 10.sp,
        )
    }
}
