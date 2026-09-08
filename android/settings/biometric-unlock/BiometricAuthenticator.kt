package com.indoone.settings.biometric

import androidx.fragment.app.FragmentActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

class BiometricAuthenticator(private val activity: FragmentActivity) {
    fun canAuthenticate(): Boolean {
        val manager = BiometricManager.from(activity)
        val result = manager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL,
        )
        return result == BiometricManager.BIOMETRIC_SUCCESS
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
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(
            activity,
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

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.BIOMETRIC_WEAK or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL,
            )
            .build()

        prompt.authenticate(promptInfo)
    }
}
