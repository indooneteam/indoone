package com.indoone.settings.biometric

import android.app.Activity
import android.content.Context
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.CancellationSignal
import androidx.core.content.ContextCompat

class BiometricAuthenticator(private val activity: Activity) {
    fun canAuthenticate(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val manager = activity.getSystemService(android.hardware.biometrics.BiometricManager::class.java)
            manager?.canAuthenticate() == android.hardware.biometrics.BiometricManager.BIOMETRIC_SUCCESS
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val manager = activity.getSystemService(Context.FINGERPRINT_SERVICE) as? FingerprintManager
            manager?.isHardwareDetected == true && manager.hasEnrolledFingerprints()
        } else {
            false
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
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ->
                authenticateWithBiometricPrompt(title, subtitle, description, onSuccess, onError)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ->
                authenticateWithFingerprint(onSuccess, onError)
            else -> onError("Biometric authentication is not supported on this Android version.")
        }
    }

    private fun authenticateWithBiometricPrompt(
        title: String,
        subtitle: String,
        description: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : android.hardware.biometrics.BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(
                result: android.hardware.biometrics.BiometricPrompt.AuthenticationResult,
            ) {
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onError(errString.toString().ifBlank { "Biometric authentication failed." })
            }

            override fun onAuthenticationFailed() {
                onError("Biometric authentication failed. Try again.")
            }
        }

        val builder = android.hardware.biometrics.BiometricPrompt.Builder(activity)
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder.setDeviceCredentialAllowed(true)
        } else {
            builder.setNegativeButton("Cancel", executor) { _, _ ->
                onError("Biometric authentication cancelled.")
            }
        }

        builder.build().authenticate(CancellationSignal(), executor, callback)
    }

    private fun authenticateWithFingerprint(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        val manager = activity.getSystemService(Context.FINGERPRINT_SERVICE) as? FingerprintManager
        if (manager == null || !manager.isHardwareDetected || !manager.hasEnrolledFingerprints()) {
            onError("Biometric authentication is unavailable on this device.")
            return
        }

        manager.authenticate(
            null,
            CancellationSignal(),
            0,
            object : FingerprintManager.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult) {
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onError(errString.toString().ifBlank { "Biometric authentication failed." })
                }

                override fun onAuthenticationFailed() {
                    onError("Fingerprint authentication failed. Try again.")
                }
            },
            null,
        )
    }
}
