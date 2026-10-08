package com.indoone.settings.autolock

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.indoone.settings.applock.AppLockStore
import com.indoone.settings.biometric.BiometricAuthenticator
import com.indoone.settings.biometric.BiometricUnlockStore
import com.indoone.settings.unlock.AutoLockUnlockScreen

class AutoLockController(private val context: Context) {
    private val handler = Handler(Looper.getMainLooper())
    private val autoLockStore = AutoLockStore(context)
    private val appLockStore = AppLockStore(context)
    private val biometricStore = BiometricUnlockStore(context)
    private var activity: ComponentActivity? = null
    private var dialog: Dialog? = null
    private var pendingBackgroundDurationMs: Long? = null

    fun onResumed(activity: ComponentActivity) {
        this.activity = activity
        configureActivitySystemBars(activity)

        val backgroundDuration = pendingBackgroundDurationMs
        pendingBackgroundDurationMs = null
        if (
            backgroundDuration != null &&
            shouldLock() &&
            backgroundDuration >= autoLockStore.minutes() * 60_000L
        ) {
            handler.post {
                if (this.activity === activity) {
                    showLock()
                }
            }
        }
    }

    fun onPaused() {
        // Auto-Lock is based on the whole app leaving the foreground,
        // not on a single Activity being paused during normal navigation.
    }

    fun onAppBackgrounded() {
        pendingBackgroundDurationMs = 0L
    }

    fun onAppForegrounded(backgroundDurationMs: Long) {
        pendingBackgroundDurationMs = backgroundDurationMs.coerceAtLeast(0L)
        val currentActivity = activity ?: return
        handler.post {
            if (this.activity === currentActivity) {
                val duration = pendingBackgroundDurationMs
                if (
                    duration != null &&
                    shouldLock() &&
                    duration >= autoLockStore.minutes() * 60_000L
                ) {
                    pendingBackgroundDurationMs = null
                    showLock()
                }
            }
        }
    }

    fun onDestroyed() {
        handler.removeCallbacksAndMessages(null)
        dialog?.dismiss()
        dialog = null
        activity = null
        pendingBackgroundDurationMs = null
    }

    private fun shouldLock(): Boolean = appLockStore.isEnabled() || biometricStore.isEnabled()

    private fun configureActivitySystemBars(activity: ComponentActivity) {
        val window = activity.window
        window.statusBarColor = Color.WHITE
        window.navigationBarColor = Color.WHITE
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
    }

    private fun showLock() {
        val currentActivity = activity ?: return
        if (dialog != null || !shouldLock()) return

        val lockDialog = Dialog(currentActivity)
        lockDialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        lockDialog.setCancelable(false)
        lockDialog.setCanceledOnTouchOutside(false)
        lockDialog.window?.let { dialogWindow ->
            dialogWindow.statusBarColor = Color.WHITE
            dialogWindow.navigationBarColor = Color.WHITE
        }

        val root = android.widget.FrameLayout(currentActivity)
        val compose = ComposeView(currentActivity).apply {
            setContent {
                MaterialTheme {
                    AutoLockUnlockScreen(
                        biometricEnabled = biometricStore.isEnabled(),
                        onUnlockWithPin = { pin ->
                            if (appLockStore.verifyPin(pin)) {
                                dismissLock()
                                true
                            } else false
                        },
                        onUnlockWithBiometric = {
                            val authenticator = BiometricAuthenticator(currentActivity)
                            if (!authenticator.canAuthenticate()) {
                                false
                            } else {
                                authenticator.authenticateForUnlock(
                                    onSuccess = { dismissLock() },
                                    onError = { },
                                )
                                true
                            }
                        },
                    )
                }
            }
        }

        root.addView(compose, android.widget.FrameLayout.LayoutParams(-1, -1))
        lockDialog.setContentView(root)
        lockDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        lockDialog.window?.setLayout(-1, -1)
        lockDialog.setOnDismissListener { dialog = null }
        dialog = lockDialog
        lockDialog.show()
        lockDialog.window?.let { dialogWindow ->
            dialogWindow.statusBarColor = Color.WHITE
            dialogWindow.navigationBarColor = Color.WHITE
            WindowCompat.setDecorFitsSystemWindows(dialogWindow, true)
            WindowInsetsControllerCompat(dialogWindow, dialogWindow.decorView).apply {
                isAppearanceLightStatusBars = true
                isAppearanceLightNavigationBars = true
            }
            dialogWindow.setLayout(-1, -1)
        }
    }

    private fun dismissLock() {
        dialog?.dismiss()
        dialog = null
        pendingBackgroundDurationMs = null
    }
}
