package com.indoone.settings.biometric

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.CancellationSignal
import java.util.concurrent.Executor

class BiometricAuthenticator(private val activity: Activity) {
    fun canAuthenticate(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return false
        val keyguard = activity.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        if (keyguard?.isKeyguardSecure != true) return false

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val manager = activity.getSystemService(Context.BIOMETRIC_SERVICE) as? BiometricManager
            manager?.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS
        } else {
            val fingerprint = activity.getSystemService(Context.FINGERPRINT_SERVICE) as? FingerprintManager
            fingerprint?.isHardwareDetected == true && fingerprint.hasEnrolledFingerprints()
        }
    }

    fun authenticate(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        authenticateInternal(
            title = "Enable Biometric Unlock",
            subtitle = "Confirm your fingerprint or device biometric",
            description = "Biometric unlock will work together with your App PIN.",
            onSuccess = onSuccess,
            onError = onError,
        )
    }

    fun authenticateForUnlock(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        authenticateInternal(
            title = "Unlock Indoone",
            subtitle = "Use your fingerprint or device biometric",
            description = "Authenticate to unlock Indoone.",
            onSuccess = onSuccess,
            onError = onError,
        )
    }

    private fun authenticateInternal(
        title: String,
        subtitle: String,
        description: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            onError("Biometric unlock requires Android 9 or newer.")
            return
        }

        val executor: Executor = activity.mainExecutor
        val prompt = BiometricPrompt.Builder(activity)
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)
            .setNegativeButton("Use App PIN", executor) { _, _ ->
                onError("Biometric authentication cancelled.")
            }
            .build()

        prompt.authenticate(
            CancellationSignal(),
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onError(errString.toString().ifBlank { "Biometric authentication failed." })
                }

                override fun onAuthenticationFailed() {
                    onError("Biometric authentication failed. Try again.")
                }
            },
        )
    }
}
