package com.indoone.assistant

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.ServiceInfo
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.speech.RecognizerIntent
import android.service.voice.VoiceInteractionService
import androidx.core.content.ContextCompat
import com.indoone.R
import java.util.Locale

class AssistantService : VoiceInteractionService() {
    companion object {
        const val ACTION_RESUME_WAKE = "com.indoone.assistant.RESUME_WAKE"
        private const val CHANNEL_ID = "indoone_assistant_wake"
        private const val NOTIFICATION_ID = 4101
        private const val WAKE_PHRASE = "hey indoone"
        private const val LANGUAGE_TAG = "en-IN"
    }

    private val handler = Handler(Looper.getMainLooper())
    private var recognizer: SpeechRecognizer? = null
    private var wakeEnabled = false
    private var sessionShowing = false

    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) = Unit
        override fun onBeginningOfSpeech() = Unit
        override fun onRmsChanged(rmsdB: Float) = Unit
        override fun onBufferReceived(buffer: ByteArray?) = Unit
        override fun onEndOfSpeech() = scheduleWakeListen(350)
        override fun onPartialResults(results: Bundle?) {
            inspectResults(results)
        }
        override fun onResults(results: Bundle?) {
            if (inspectResults(results)) return
            scheduleWakeListen(350)
        }
        override fun onError(error: Int) {
            scheduleWakeListen(500)
        }
        override fun onEvent(eventType: Int, params: Bundle?) = Unit
    }

    override fun onReady() {
        super.onReady()
        ensureWakeNotificationChannel()
        startWakeForeground()
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startWakeListening()
        }
        if (Build.VERSION.SDK_INT >= 36) {
            runCatching { setInvocationEffectEnabled(true) }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_RESUME_WAKE) {
            sessionShowing = false
            startWakeListening()
        }
        return START_NOT_STICKY
    }

    override fun onShowSessionFailed(args: Bundle?) {
        super.onShowSessionFailed(args)
        sessionShowing = false
        startWakeListening()
    }

    override fun onShutdown() {
        stopWakeListening()
        stopWakeForeground()
        super.onShutdown()
    }

    override fun onDestroy() {
        stopWakeListening()
        stopWakeForeground()
        super.onDestroy()
    }

    private fun startWakeListening() {
        if (sessionShowing || wakeEnabled) return

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            !SpeechRecognizer.isOnDeviceRecognitionAvailable(this)
        ) {
            updateNotification("On-device wake recognition unavailable")
            return
        }

        wakeEnabled = true
        updateNotification("Listening for “Hey Indoone”")
        scheduleWakeListen(150)
    }

    private fun scheduleWakeListen(delayMs: Long) {
        handler.postDelayed({
            if (!wakeEnabled || sessionShowing) return@postDelayed
            startOneRecognition()
        }, delayMs)
    }

    private fun startOneRecognition() {
        recognizer?.cancel()
        recognizer?.destroy()

        recognizer = SpeechRecognizer.createOnDeviceSpeechRecognizer(this).apply {
            setRecognitionListener(recognitionListener)

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, LANGUAGE_TAG)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
            }

            runCatching { startListening(intent) }
                .onFailure { scheduleWakeListen(750) }
        }
    }

    private fun inspectResults(results: Bundle?): Boolean {
        if (results == null) return false

        val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?: return false

        val detected = matches.any { normalize(it).contains(WAKE_PHRASE) }
        if (!detected) return false

        triggerWake()
        return true
    }

    private fun triggerWake() {
        if (sessionShowing) return

        sessionShowing = true
        wakeEnabled = false
        stopWakeListening()
        updateNotification("Assistant active")

        showSession(
            Bundle().apply {
                putString("invocation_type", "wake_word")
                putString("wake_phrase", "Hey Indoone")
            },
            0,
        )
    }

    private fun stopWakeListening() {
        wakeEnabled = false
        handler.removeCallbacksAndMessages(null)
        recognizer?.let {
            runCatching { it.stopListening() }
            runCatching { it.cancel() }
            runCatching { it.destroy() }
        }
        recognizer = null
    }

    private fun ensureWakeNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Indoone Assistant",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Background wake-word listener for Hey Indoone"
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }

    private fun startWakeForeground() {
        val notification = buildNotification("Starting Hey Indoone listener")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE,
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun stopWakeForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            stopForeground(true)
        }
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification(text))
    }

    private fun buildNotification(text: String): Notification {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_indoone_notification)
                .setContentTitle("Indoone Assistant")
                .setContentText(text)
                .setOngoing(true)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build()
        } else {
            Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_indoone_notification)
                .setContentTitle("Indoone Assistant")
                .setContentText(text)
                .setOngoing(true)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build()
        }
    }

    private fun normalize(value: String): String =
        value.lowercase(Locale.US)
            .replace(Regex("[^a-z0-9 ]"), " ")
            .replace(Regex("\s+"), " ")
            .trim()
}
