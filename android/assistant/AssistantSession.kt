package com.indoone.assistant

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.service.voice.VoiceInteractionSession
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.indoone.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class AssistantSession(context: Context) : VoiceInteractionSession(context) {
    companion object {
        private const val TAG = "IndooneAssistant"
        private const val ORB_WINDOW_DP = 116
        private const val EDGE_MARGIN_DP = 16
        private const val INITIAL_TOP_DP = 92
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val client = AssistantClient(scope)

    private var audioEngine: AssistantAudioEngine? = null
    private var overlay: AssistantOverlayView? = null
    private var active = false
    private var windowX = 0
    private var windowY = 0

    override fun onCreate() {
        super.onCreate()

        getWindow()?.window?.let { window ->
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.setDimAmount(0f)
            window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
            window.setGravity(Gravity.TOP or Gravity.START)

            val widthPx = resources.displayMetrics.widthPixels
            windowX = (widthPx - ORB_WINDOW_DP.dp() - EDGE_MARGIN_DP.dp()).coerceAtLeast(EDGE_MARGIN_DP.dp())
            windowY = INITIAL_TOP_DP.dp()

            window.attributes = window.attributes.apply {
                gravity = Gravity.TOP or Gravity.START
                x = windowX
                y = windowY
            }
            window.setLayout(ORB_WINDOW_DP.dp(), ORB_WINDOW_DP.dp())
        }
    }

    override fun onCreateContentView(): android.view.View {
        return AssistantOverlayView(
            context = context,
            onClose = { hide() },
            onOpenApp = ::openIndooneApp,
            onDrag = ::moveOverlay,
        ).also { overlay = it }
    }

    override fun onShow(args: Bundle?, showFlags: Int) {
        super.onShow(args, showFlags)
        active = true
        overlay?.setStatus("Listening…")
        startSession()
    }

    override fun onHide() {
        stopSession()
        runCatching {
            context.startService(
                Intent(context, AssistantService::class.java).apply {
                    action = AssistantService.ACTION_RESUME_WAKE
                },
            )
        }
        super.onHide()
    }

    override fun onDestroy() {
        stopSession()
        scope.cancel()
        overlay = null
        super.onDestroy()
    }

    private fun moveOverlay(dx: Int, dy: Int) {
        val voiceWindow = getWindow()?.window ?: return
        val display = resources.displayMetrics
        val margin = EDGE_MARGIN_DP.dp()
        val size = ORB_WINDOW_DP.dp()

        windowX = (windowX + dx).coerceIn(
            margin,
            (display.widthPixels - size - margin).coerceAtLeast(margin),
        )
        windowY = (windowY + dy).coerceIn(
            margin,
            (display.heightPixels - size - margin).coerceAtLeast(margin),
        )

        voiceWindow.attributes = voiceWindow.attributes.apply {
            gravity = Gravity.TOP or Gravity.START
            x = windowX
            y = windowY
        }
    }

    private fun openIndooneApp() {
        runCatching {
            context.startActivity(
                Intent(context, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                },
            )
        }.onFailure {
            Log.w(TAG, "Could not open Indoone app", it)
        }
    }

    private fun startSession() {
        val appContext = context.applicationContext

        if (
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.RECORD_AUDIO,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            overlay?.setStatus("Microphone permission required")
            Log.w(TAG, "Microphone permission is not granted")
            return
        }

        audioEngine?.stop()
        audioEngine = AssistantAudioEngine(
            context = appContext,
            onCapturedPcm = client::sendAudio,
        )

        client.connect(
            onEvent = { event ->
                if (!active) return@connect

                when (event.optString("type")) {
                    "connected" -> {
                        overlay?.setStatus("Listening…")
                        audioEngine?.startRecording()
                        Log.i(TAG, "Home assistant audio session connected")
                    }

                    "status" -> {
                        val status = event.optString("interaction_status")
                        overlay?.setStatus(
                            when (status.uppercase()) {
                                "IN_PROGRESS" -> "Thinking…"
                                "IDLE" -> "Listening…"
                                "SPEAKING", "OUTPUT" -> "Speaking…"
                                else -> status.lowercase().replaceFirstChar { it.titlecase() }
                            },
                        )
                    }

                    "transcript" -> {
                        if (event.optString("role") == "assistant") {
                            overlay?.setStatus("Speaking…")
                        }
                    }

                    "audio" -> {
                        overlay?.setStatus("Speaking…")
                        event.optString("audio_base64")
                            .takeIf { it.isNotBlank() }
                            ?.let { audioEngine?.playResponse(it) }
                    }

                    "interrupted" -> {
                        audioEngine?.flushPlayback()
                        overlay?.setStatus("Listening…")
                    }

                    "error" -> {
                        overlay?.setStatus("Listening…")
                        Log.w(
                            TAG,
                            event.optString("detail").ifBlank {
                                "Home assistant session error"
                            },
                        )
                        audioEngine?.stopRecording()
                    }

                    "closed", "stopped" -> {
                        overlay?.setStatus("Disconnected")
                        audioEngine?.stopRecording()
                    }
                }
            },
            onFailure = { message ->
                if (!active) return@connect
                overlay?.setStatus("Connection error")
                Log.w(TAG, "Home assistant connection failed: $message")
                audioEngine?.stopRecording()
            },
        )
    }

    private fun stopSession() {
        if (!active && audioEngine == null) return

        active = false
        audioEngine?.stop()
        audioEngine = null
        client.close()
        overlay = null
        Log.i(TAG, "Home assistant session stopped")
    }

    private fun Int.dp(): Int =
        (this * resources.displayMetrics.density).toInt()
}
