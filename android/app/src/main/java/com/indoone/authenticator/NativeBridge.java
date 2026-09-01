package com.indoone.authenticator;

import android.webkit.JavascriptInterface;

import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.util.concurrent.Executor;

public final class NativeBridge {
    private final MainActivity activity;

    public NativeBridge(MainActivity activity) {
        this.activity = activity;
    }

    @JavascriptInterface
    public void authenticateBiometric() {
        activity.runOnUiThread(() -> {
            BiometricManager manager = BiometricManager.from(activity);
            int canAuth = manager.canAuthenticate(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG
                            | BiometricManager.Authenticators.DEVICE_CREDENTIAL);
            if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
                activity.sendBiometricResult(false, "Biometric authentication unavailable");
                return;
            }

            Executor executor = ContextCompat.getMainExecutor(activity);
            BiometricPrompt prompt = new BiometricPrompt(activity, executor,
                    new BiometricPrompt.AuthenticationCallback() {
                        @Override
                        public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                            activity.sendBiometricResult(true, "Biometric authentication successful");
                        }

                        @Override
                        public void onAuthenticationError(int errorCode, CharSequence errString) {
                            activity.sendBiometricResult(false, String.valueOf(errString));
                        }

                        @Override
                        public void onAuthenticationFailed() {
                            activity.sendBiometricResult(false, "Authentication failed");
                        }
                    });

            BiometricPrompt.PromptInfo info = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Unlock Indoone")
                    .setSubtitle("Use your biometric or device credential")
                    .setAllowedAuthenticators(
                            BiometricManager.Authenticators.BIOMETRIC_STRONG
                                    | BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                    .build();
            prompt.authenticate(info);
        });
    }

    @JavascriptInterface
    public void requestCameraPermission() {
        activity.requestCameraPermission();
    }
}
