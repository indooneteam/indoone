package com.indoone.settings.applock.lockapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.indoone.settings.applock.AppLockStore

class LockAppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val store = AppLockStore(applicationContext)
        if (!store.isEnabled()) {
            finish()
            return
        }

        setContent {
            MaterialTheme {
                var pin by remember { mutableStateOf("") }
                var error by remember { mutableStateOf("") }

                LockAppScreen(
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
