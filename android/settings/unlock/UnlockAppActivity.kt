package com.indoone.settings.unlock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.indoone.settings.applock.AppLockStore
import com.indoone.settings.biometric.BiometricAuthenticator
import com.indoone.settings.biometric.BiometricUnlockStore

class UnlockAppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val store = AppLockStore(applicationContext)
        if (!store.isEnabled()) {
            finish()
            return
        }
        val biometricStore = BiometricUnlockStore(applicationContext)

        setContent {
            MaterialTheme {
                // The same lock screen Auto-Lock shows, so "Lock App" and an automatic
                // lock look and behave identically: biometric first when it is enabled,
                // then the App PIN.
                AutoLockUnlockScreen(
                    biometricEnabled = biometricStore.isEnabled(),
                    onUnlockWithPin = { pin ->
                        if (store.verifyPin(pin)) {
                            finish()
                            true
                        } else {
                            false
                        }
                    },
                    onUnlockWithBiometric = {
                        val authenticator = BiometricAuthenticator(this)
                        if (!authenticator.canAuthenticate()) {
                            false
                        } else {
                            authenticator.authenticateForUnlock(
                                onSuccess = { finish() },
                                onError = { },
                            )
                            true
                        }
                    },
                )
            }
        }
    }
}
