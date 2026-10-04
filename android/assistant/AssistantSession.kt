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
    private var active = false

    override fun onShow(args: Bundle?, showFlags: Int) {
        super.onShow(args, showFlags)
        active = true
        startSession()
    }

    override fun onHide() {
        stopSession()
        super.onHide()
    }

    override fun onDestroy() {
        stopSession()
        scope.cancel()
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
                        audioEngine?.startRecording()
                        Log.i(TAG, "Home assistant audio session connected")
                    }

                    "audio" -> {
                        event.optString("audio_base64")
                            .takeIf { it.isNotBlank() }
                            ?.let { audioEngine?.playResponse(it) }
                    }

                    "interrupted" -> audioEngine?.flushPlayback()

                    "error" -> {
                        Log.w(
                            TAG,
                            event.optString("detail").ifBlank {
                                "Home assistant session error"
                            },
                        )
                        audioEngine?.stopRecording()
                    }

                    "closed", "stopped" -> audioEngine?.stopRecording()
                }
            },
            onFailure = { message ->
                if (!active) return@connect
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
        Log.i(TAG, "Home assistant session stopped")
    }
}
