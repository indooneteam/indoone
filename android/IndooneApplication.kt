package com.indoone

import android.app.Activity
import android.app.Application
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.indoone.home.message.ChatCleanupWorker
import com.indoone.notifications.IndooneAppState
import com.indoone.notifications.IndooneNotificationChannels
import com.indoone.notifications.NotificationApi
import com.indoone.notifications.NotificationPreferences
import com.indoone.settings.autolock.AutoLockController
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class IndooneApplication : Application() {
    private val controllers = mutableMapOf<Activity, AutoLockController>()
    private val notificationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val autoLockHandler = Handler(Looper.getMainLooper())
    private var resumedActivityCount = 0
    private val lifecyclePreferences by lazy {
        getSharedPreferences(AUTO_LOCK_LIFECYCLE_PREFS, MODE_PRIVATE)
    }
    private val markBackgroundRunnable = Runnable {
        if (resumedActivityCount == 0 &&
            lifecyclePreferences.getLong(KEY_BACKGROUND_STARTED_AT, 0L) == 0L
        ) {
            lifecyclePreferences.edit()
                .putLong(KEY_BACKGROUND_STARTED_AT, System.currentTimeMillis())
                .apply()
        }
    }

    override fun onCreate() {
        super.onCreate()
        IndooneNotificationChannels.create(this)
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            NotificationPreferences(this).saveFcmToken(token)
        }
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "indoone-chat-expiry-cleanup",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<ChatCleanupWorker>(1, TimeUnit.DAYS).build(),
        )
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                configureSystemBars(activity)
                if (activity is ComponentActivity) {
                    controllers[activity] = AutoLockController(activity.applicationContext)
                    installStatusBarInset(activity)
                }
            }

            override fun onActivityResumed(activity: Activity) {
                configureSystemBars(activity)
                syncNotificationToken()
                val componentActivity = activity as? ComponentActivity ?: return

                resumedActivityCount += 1
                autoLockHandler.removeCallbacks(markBackgroundRunnable)

                val backgroundStartedAt = lifecyclePreferences.getLong(KEY_BACKGROUND_STARTED_AT, 0L)
                lifecyclePreferences.edit().remove(KEY_BACKGROUND_STARTED_AT).apply()
                val backgroundDuration = if (backgroundStartedAt > 0L) {
                    (System.currentTimeMillis() - backgroundStartedAt).coerceAtLeast(0L)
                } else {
                    null
                }

                controllers[activity]?.onResumed(componentActivity, backgroundDuration)
                installStatusBarInset(componentActivity)
            }

            override fun onActivityPaused(activity: Activity) {
                controllers[activity]?.onPaused()
                resumedActivityCount = (resumedActivityCount - 1).coerceAtLeast(0)
                if (resumedActivityCount == 0) {
                    autoLockHandler.removeCallbacks(markBackgroundRunnable)
                    autoLockHandler.postDelayed(
                        markBackgroundRunnable,
                        BACKGROUND_DETECTION_DELAY_MS,
                    )
                }
            }

            override fun onActivityDestroyed(activity: Activity) {
                controllers.remove(activity)?.onDestroyed()
            }

            override fun onActivityStarted(activity: Activity) {
                IndooneAppState.onActivityStarted()
            }

            override fun onActivityStopped(activity: Activity) {
                IndooneAppState.onActivityStopped()
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
        })
    }

    private fun syncNotificationToken() {
        FirebaseAuth.getInstance().currentUser ?: return
        val preferences = NotificationPreferences(this)

        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            if (token.isBlank()) return@addOnSuccessListener
            preferences.saveFcmToken(token)
            if (preferences.registeredFcmToken() == token) return@addOnSuccessListener

            notificationScope.launch {
                if (runCatching { NotificationApi.registerToken(token) }.getOrDefault(false)) {
                    preferences.markFcmTokenRegistered(token)
                }
            }
        }
    }

    private fun configureSystemBars(activity: Activity) {
        val window = activity.window
        window.statusBarColor = Color.WHITE
        window.navigationBarColor = Color.WHITE
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
    }

    private fun installStatusBarInset(activity: ComponentActivity) {
        val content = activity.findViewById<View>(android.R.id.content) ?: return
        val initialLeft = content.paddingLeft
        val initialRight = content.paddingRight
        val initialBottom = content.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(content) { view, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(initialLeft, topInset, initialRight, initialBottom)
            insets
        }
        ViewCompat.requestApplyInsets(content)
    }
    companion object {
        private const val AUTO_LOCK_LIFECYCLE_PREFS = "indoone_auto_lock_lifecycle"
        private const val KEY_BACKGROUND_STARTED_AT = "background_started_at"
        private const val BACKGROUND_DETECTION_DELAY_MS = 750L
    }
}
