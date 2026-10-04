package com.indoone.assistant

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.service.voice.VoiceInteractionSession
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class AssistantSession(context: Context) : VoiceInteractionSession(context) {
    companion object {
        private const val TAG = "IndooneAssistant"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val client = AssistantClient(scope)

    private var audioEngine: AssistantAudioEngine? = null
    private var overlay: AssistantOverlayView? = null
    private var active = false

    override fun onCreate() {
        super.onCreate()

        getWindow()?.window?.let { window ->
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.setDimAmount(0f)
            window.setGravity(android.view.Gravity.BOTTOM)
            window.setLayout(
                android.view.WindowManager.LayoutParams.MATCH_PARENT,
                android.view.WindowManager.LayoutParams.WRAP_CONTENT,
            )
        }
    }

    override fun onCreateContentView(): android.view.View {
        return AssistantOverlayView(context) {
            hide()
        }.also { overlay = it }
    }

    override fun onShow(args: Bundle?, showFlags: Int) {
        super.onShow(args, showFlags)
        active = true
        overlay?.setStatus("Connecting…")
        startSession()
    }

    override fun onHide() {
        stopSession()
        runCatching {
            context.startService(
                android.content.Intent(context, AssistantService::class.java).apply {
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
                                else -> status.lowercase().replaceFirstChar { it.titlecase() }
                            },
                        )
                    }

                    "transcript" -> {
                        if (event.optString("role") == "assistant") {
                            overlay?.setTranscript(event.optString("text"))
                        }
                    }

                    "audio" -> {
                        event.optString("audio_base64")
                            .takeIf { it.isNotBlank() }
                            ?.let { audioEngine?.playResponse(it) }
                    }

                    "interrupted" -> audioEngine?.flushPlayback()

                    "error" -> {
                        overlay?.setStatus("Connection error")
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
}
