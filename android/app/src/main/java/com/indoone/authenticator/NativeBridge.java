package com.indoone.authenticator;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.webkit.JavascriptInterface;

import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.concurrent.Executor;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public final class NativeBridge {
    private static final String KEY_ALIAS = "IndooneBiometricVaultKey";
    private static final String PREFS = "indoone_biometric";
    private static final String PREF_CIPHERTEXT = "pin_ciphertext";
    private static final String PREF_IV = "pin_iv";

    private final MainActivity activity;

    public NativeBridge(MainActivity activity) {
        this.activity = activity;
    }

    @JavascriptInterface public void authenticateBiometric() { authenticate(false); }
    @JavascriptInterface public void authenticateBiometricUnlock() { authenticate(true); }

    @JavascriptInterface
    public boolean hasBiometricSecret() {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return prefs.contains(PREF_CIPHERTEXT) && prefs.contains(PREF_IV) && getKey() != null;
    }

    @JavascriptInterface
    public boolean saveBiometricSecret(String pin) {
        if (pin == null || !pin.matches("\\d{4,12}")) return false;
        try {
            SecretKey key = getOrCreateKey();
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] ciphertext = cipher.doFinal(pin.getBytes(StandardCharsets.UTF_8));
            SharedPreferences prefs = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            prefs.edit()
                    .putString(PREF_CIPHERTEXT, Base64.encodeToString(ciphertext, Base64.NO_WRAP))
                    .putString(PREF_IV, Base64.encodeToString(cipher.getIV(), Base64.NO_WRAP))
                    .apply();
            return true;
        } catch (Exception error) { return false; }
    }

    @JavascriptInterface public void clearBiometricSecret() { activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply(); }
    @JavascriptInterface public void requestCameraPermission() { activity.requestCameraPermission(); }
    @JavascriptInterface public void requestNearbyPermissions() { activity.requestNearbyPermissions(); }

    @JavascriptInterface
    public void startNearbyAdvertising(String deviceName) {
        activity.runOnUiThread(() -> activity.getNearbyConnectionManager().startAdvertising(deviceName));
    }

    @JavascriptInterface
    public void startNearbyDiscovery() {
        activity.runOnUiThread(() -> activity.getNearbyConnectionManager().startDiscovery());
    }

    @JavascriptInterface
    public void connectNearbyEndpoint(String endpointId, String deviceName) {
        activity.runOnUiThread(() -> activity.getNearbyConnectionManager().requestConnection(endpointId, deviceName));
    }

    @JavascriptInterface
    public void acceptNearbyConnection(String endpointId) {
        activity.runOnUiThread(() -> activity.getNearbyConnectionManager().acceptConnection(endpointId));
    }

    @JavascriptInterface
    public void rejectNearbyConnection(String endpointId) {
        activity.runOnUiThread(() -> activity.getNearbyConnectionManager().rejectConnection(endpointId));
    }

    @JavascriptInterface
    public void sendNearbyText(String endpointId, String text) {
        activity.runOnUiThread(() -> activity.getNearbyConnectionManager().sendText(endpointId, text));
    }

    @JavascriptInterface
    public void stopNearby() {
        activity.runOnUiThread(() -> activity.getNearbyConnectionManager().stop());
    }

    private void authenticate(boolean unlockVault) {
        activity.runOnUiThread(() -> {
            BiometricManager manager = BiometricManager.from(activity);
            int canAuth = manager.canAuthenticate(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL);
            if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
                activity.sendBiometricResult(false, "Biometric authentication unavailable", null);
                return;
            }
            if (unlockVault && !hasBiometricSecret()) {
                activity.sendBiometricResult(false, "Biometric unlock is not configured", null);
                return;
            }

            Executor executor = ContextCompat.getMainExecutor(activity);
            BiometricPrompt prompt = new BiometricPrompt(activity, executor, new BiometricPrompt.AuthenticationCallback() {
                @Override public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                    if (!unlockVault) {
                        activity.sendBiometricResult(true, "Biometric authentication successful", null);
                        return;
                    }
                    try {
                        SecretKey key = getKey();
                        if (key == null) throw new IllegalStateException("Missing biometric key");
                        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, getIv()));
                        String pin = new String(cipher.doFinal(getCiphertext()), StandardCharsets.UTF_8);
                        if (!pin.matches("\\d{4,12}")) throw new IllegalStateException("Invalid stored PIN");
                        activity.sendBiometricResult(true, "Biometric authentication successful", pin);
                    } catch (Exception error) {
                        clearBiometricSecret();
                        activity.sendBiometricResult(false, "Biometric credential is unavailable", null);
                    }
                }
                @Override public void onAuthenticationError(int errorCode, CharSequence errString) { activity.sendBiometricResult(false, String.valueOf(errString), null); }
                @Override public void onAuthenticationFailed() { activity.sendBiometricResult(false, "Authentication failed", null); }
            });

            BiometricPrompt.PromptInfo info = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Unlock Indoone")
                    .setSubtitle(unlockVault ? "Use your fingerprint or device credential to unlock" : "Use your biometric or device credential")
                    .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                    .build();
            prompt.authenticate(info);
        });
    }

    private SecretKey getKey() {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            java.security.Key key = keyStore.getKey(KEY_ALIAS, null);
            return key instanceof SecretKey ? (SecretKey) key : null;
        } catch (Exception error) { return null; }
    }

    private SecretKey getOrCreateKey() throws Exception {
        SecretKey existing = getKey();
        if (existing != null) return existing;
        KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        generator.init(new KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build());
        return generator.generateKey();
    }

    private byte[] getIv() {
        String encoded = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(PREF_IV, null);
        if (encoded == null) throw new IllegalStateException("Missing biometric IV");
        return Base64.decode(encoded, Base64.NO_WRAP);
    }

    private byte[] getCiphertext() {
        String encoded = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(PREF_CIPHERTEXT, null);
        if (encoded == null) throw new IllegalStateException("Missing biometric credential");
        return Base64.decode(encoded, Base64.NO_WRAP);
    }
}
