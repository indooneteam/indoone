package com.indoone.settings.unlock

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.indoone.settings.applock.AppLockStore

class UnlockAppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isSystemDark =
            (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES

        window.statusBarColor = if (isSystemDark) Color.BLACK else Color.WHITE
        window.navigationBarColor = if (isSystemDark) Color.BLACK else Color.WHITE
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = !isSystemDark
            isAppearanceLightNavigationBars = !isSystemDark
        }

        val store = AppLockStore(applicationContext)
        if (!store.isEnabled()) {
            finish()
            return
        }

        setContent {
            MaterialTheme {
                var pin by remember { mutableStateOf("") }
                var error by remember { mutableStateOf("") }

                UnlockAppScreen(
                    pin = pin,
                    error = error,
                    onDigit = { digit ->
                        if (pin.length < 12) {
                            pin += digit
                            error = ""
                        }
                    },
                    onBackspace = {
                        pin = pin.dropLast(1)
                        error = ""
                    },
                    onClear = {
                        pin = ""
                        error = ""
                    },
                    onUnlock = {
                        if (store.verifyPin(pin)) {
                            finish()
                        } else {
                            pin = ""
                            error = "Incorrect App PIN"
                        }
                    },
                )
            }
        }
    }
}
