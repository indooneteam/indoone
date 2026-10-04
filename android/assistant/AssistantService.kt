package com.indoone.assistant

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.service.voice.VoiceInteractionService
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.indoone.R

class AssistantService : VoiceInteractionService() {
    companion object {
        const val ACTION_RESUME_WAKE = "com.indoone.assistant.RESUME_WAKE"
        const val EXTRA_AUTH_TOKEN = "com.indoone.assistant.AUTH_TOKEN"
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

    override fun onShowSessionFailed(args: android.os.Bundle) {
        sessionShowing = false
        Log.e(TAG, "Assistant session failed to show: $args")
        updateNotification("Assistant session failed")
        startWakeDetector()
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
            updateNotification(
                "Microphone permission required",
                includePermissionAction = true,
            )
            Log.w(TAG, "Microphone permission is not granted")
            return
        }

        runCatching {
            startWakeForeground()
        }.onFailure { error ->
            Log.e(TAG, "Could not start assistant microphone service", error)
            updateNotification(
                "Assistant microphone service failed: ${error.message ?: error::class.java.simpleName}",
            )
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
        Log.i(TAG, "Hey Indoone detected; preparing assistant session")

        Thread {
            runCatching {
                val user = FirebaseAuth.getInstance().currentUser
                    ?: throw IllegalStateException(
                        "Please sign in to use Indoone Assistant.",
                    )
                Tasks.await(user.getIdToken(false)).token
                    ?.takeIf { it.isNotBlank() }
                    ?: throw IllegalStateException(
                        "Could not get the Indoone authentication token.",
                    )
            }.onSuccess { token ->
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    if (!sessionShowing) return@post
                    Log.i(TAG, "Assistant auth token prepared; opening session")
                    showSession(
                        android.os.Bundle().apply {
                            putString("invocation_type", "wake_word")
                            putString("wake_phrase", "Hey Indoone")
                            putString(EXTRA_AUTH_TOKEN, token)
                        },
                        0,
                    )
                }
            }.onFailure { error ->
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    sessionShowing = false
                    Log.e(TAG, "Assistant authentication preparation failed", error)
                    updateNotification(
                        "Assistant authentication failed: ${error.message ?: error::class.java.simpleName}",
                    )
                    startWakeDetector()
                }
            }
        }.apply {
            name = "Indoone-Assistant-Auth"
            isDaemon = true
            start()
        }
    }

    private fun onWakeError(message: String) {
        Log.w(TAG, message)
        updateNotification("Wake listener error: $message")
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

    private fun updateNotification(
        text: String,
        includePermissionAction: Boolean = false,
    ) {
        getSystemService(NotificationManager::class.java)
            .notify(
                NOTIFICATION_ID,
                buildNotification(text, includePermissionAction),
            )
    }

    private fun buildNotification(
        text: String,
        includePermissionAction: Boolean = false,
    ): Notification {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_indoone_notification)
                .setContentTitle("Indoone Assistant")
                .setContentText(text)
                .setStyle(Notification.BigTextStyle().bigText(text))
                .setOngoing(true)
                .setCategory(Notification.CATEGORY_SERVICE)
                .apply {
                    if (includePermissionAction) {
                        val intent = Intent(
                            this@AssistantService,
                            AssistantPermissionActivity::class.java,
                        ).apply {
                            addFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                    Intent.FLAG_ACTIVITY_SINGLE_TOP,
                            )
                        }
                        val pendingIntent = PendingIntent.getActivity(
                            this@AssistantService,
                            NOTIFICATION_ID + 1,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                        )
                        addAction(
                            R.drawable.ic_indoone_notification,
                            "Allow microphone",
                            pendingIntent,
                        )
                    }
                }
                .build()
        } else {
            Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_indoone_notification)
                .setContentTitle("Indoone Assistant")
                .setContentText(text)
                .setStyle(Notification.BigTextStyle().bigText(text))
                .setOngoing(true)
                .setCategory(Notification.CATEGORY_SERVICE)
                .apply {
                    if (includePermissionAction) {
                        val intent = Intent(
                            this@AssistantService,
                            AssistantPermissionActivity::class.java,
                        ).apply {
                            addFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                    Intent.FLAG_ACTIVITY_SINGLE_TOP,
                            )
                        }
                        val pendingIntent = PendingIntent.getActivity(
                            this@AssistantService,
                            NOTIFICATION_ID + 1,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                        )
                        addAction(
                            R.drawable.ic_indoone_notification,
                            "Allow microphone",
                            pendingIntent,
                        )
                    }
                }
                .build()
        }
    }
}
