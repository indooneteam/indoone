package com.indoone.assistant

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.service.voice.VoiceInteractionService
import android.util.Log
import androidx.core.content.ContextCompat
import com.indoone.R

class AssistantService : VoiceInteractionService() {
    companion object {
        const val ACTION_RESUME_WAKE = "com.indoone.assistant.RESUME_WAKE"
        private const val CHANNEL_ID = "indoone_assistant_wake"
        private const val NOTIFICATION_ID = 4101
        private const val TAG = "IndooneAssistant"
    }

    private var sessionShowing = false

    private val wakeDetector by lazy {
        AssistantWakeDetector(
            context = applicationContext,
            onDetected = ::onWakeDetected,
            onError = ::onWakeError,
        )
    }

    override fun onReady() {
        super.onReady()
        Log.i(TAG, "VoiceInteractionService ready")

        ensureWakeNotificationChannel()
        startWakeForeground()

        if (Build.VERSION.SDK_INT >= 36) {
            runCatching { setInvocationEffectEnabled(false) }
        }

        startWakeDetector()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_RESUME_WAKE) {
            Log.i(TAG, "Resuming silent Hey Indoone detector")
            sessionShowing = false
            startWakeDetector()
        }
        return START_STICKY
    }

    override fun onShutdown() {
        wakeDetector.release()
        stopWakeForeground()
        super.onShutdown()
    }

    override fun onDestroy() {
        wakeDetector.release()
        stopWakeForeground()
        super.onDestroy()
    }

    private fun startWakeDetector() {
        if (sessionShowing) return

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            updateNotification("Microphone permission required")
            Log.w(TAG, "Microphone permission is not granted")
            return
        }

        updateNotification("Waiting for “Hey Indoone”")
        wakeDetector.initialize {
            if (!sessionShowing) {
                wakeDetector.start()
                Log.i(TAG, "Silent offline wake detector started")
            }
        }
    }

    private fun onWakeDetected() {
        if (sessionShowing) return

        sessionShowing = true
        wakeDetector.stop()
        updateNotification("Assistant active")
        Log.i(TAG, "Hey Indoone detected; opening assistant session")

        showSession(
            android.os.Bundle().apply {
                putString("invocation_type", "wake_word")
                putString("wake_phrase", "Hey Indoone")
            },
            0,
        )
    }

    private fun onWakeError(message: String) {
        Log.w(TAG, message)
        updateNotification("Wake listener unavailable")
    }

    private fun ensureWakeNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Indoone Assistant",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Silent background wake-word listener for Hey Indoone"
            setSound(null, null)
            enableVibration(false)
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
        getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, buildNotification(text))
    }

    private fun buildNotification(text: String): Notification {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_indoone_notification)
                .setContentTitle("Indoone Assistant")
                .setContentText(text)
                .setOngoing(true)
                .setSilent(true)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build()
        } else {
            Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_indoone_notification)
                .setContentTitle("Indoone Assistant")
                .setContentText(text)
                .setOngoing(true)
                .setSilent(true)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build()
        }
    }
}
