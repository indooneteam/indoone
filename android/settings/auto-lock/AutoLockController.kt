package com.indoone.settings.autolock

import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.MotionEvent
import android.view.Window
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import com.indoone.configureIndooneSystemBars
import com.indoone.settings.applock.AppLockStore
import com.indoone.settings.biometric.BiometricAuthenticator
import com.indoone.settings.biometric.BiometricUnlockStore
import com.indoone.settings.unlock.AutoLockUnlockScreen

class AutoLockController(private val context: Context) {
    private val handler = Handler(Looper.getMainLooper())
    private val autoLockStore = AutoLockStore(context)
    private val appLockStore = AppLockStore(context)
    private val biometricStore = BiometricUnlockStore(context)
    private var lastActivity = SystemClock.elapsedRealtime()
    private var activity: ComponentActivity? = null
    private var dialog: Dialog? = null
    private var checking = false

    private val checker = object : Runnable {
        override fun run() {
            if (checking && shouldLock() && dialog == null) {
                val idleMs = SystemClock.elapsedRealtime() - lastActivity
                if (idleMs >= autoLockStore.minutes() * 60_000L) {
                    showLock()
                }
            }
            handler.postDelayed(this, 1_000L)
        }
    }

    fun onResumed(activity: ComponentActivity) {
        this.activity = activity
        activity.configureIndooneSystemBars()
        lastActivity = SystemClock.elapsedRealtime().coerceAtMost(lastActivity)
        if (!checking) {
            checking = true
            handler.removeCallbacks(checker)
            handler.post(checker)
        }
        installActivityTouchWatcher(activity)
    }

    fun onPaused() {
        checking = false
        handler.removeCallbacks(checker)
    }

    fun onDestroyed() {
        checking = false
        handler.removeCallbacks(checker)
        dialog?.dismiss()
        dialog = null
        activity = null
    }

    fun touch() {
        if (dialog == null) lastActivity = SystemClock.elapsedRealtime()
    }

    private fun shouldLock(): Boolean = appLockStore.isEnabled() || biometricStore.isEnabled()

    private fun installActivityTouchWatcher(activity: ComponentActivity) {
        val decor = activity.window.decorView
        if (decor.getTag(TAG_TOUCH_WATCHER) == true) return
        decor.setTag(TAG_TOUCH_WATCHER, true)
        decor.setOnTouchListener { _, event ->
            if (event.actionMasked == MotionEvent.ACTION_DOWN) touch()
            false
        }
    }

    private fun showLock() {
        val currentActivity = activity ?: return
        if (dialog != null || !shouldLock()) return
        lastActivity = SystemClock.elapsedRealtime()

        val lockDialog = Dialog(currentActivity)
        lockDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        lockDialog.setCancelable(false)
        lockDialog.setCanceledOnTouchOutside(false)
        lockDialog.window?.let { dialogWindow ->
            dialogWindow.statusBarColor = android.graphics.Color.WHITE
            dialogWindow.navigationBarColor = android.graphics.Color.WHITE
        }

        val root = FrameLayout(currentActivity)
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
        root.addView(compose, FrameLayout.LayoutParams(-1, -1))
        lockDialog.setContentView(root)
        lockDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        lockDialog.window?.setLayout(-1, -1)
        lockDialog.setOnDismissListener { dialog = null }
        dialog = lockDialog
        lockDialog.show()
        lockDialog.window?.let { dialogWindow ->
            dialogWindow.statusBarColor = android.graphics.Color.WHITE
            dialogWindow.navigationBarColor = android.graphics.Color.WHITE
            androidx.core.view.WindowInsetsControllerCompat(dialogWindow, dialogWindow.decorView).apply {
                isAppearanceLightStatusBars = true
                isAppearanceLightNavigationBars = true
            }
            dialogWindow.setLayout(-1, -1)
        }
    }

    private fun dismissLock() {
        dialog?.dismiss()
        dialog = null
        lastActivity = SystemClock.elapsedRealtime()
    }

    private companion object {
        const val TAG_TOUCH_WATCHER = 0x1D001001
    }
}
