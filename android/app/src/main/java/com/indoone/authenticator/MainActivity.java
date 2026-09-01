package com.indoone.authenticator;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

public class MainActivity extends FragmentActivity {
    private static final int CAMERA_REQUEST_CODE = 41;
    private WebView webView;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        webView.addJavascriptInterface(new NativeBridge(this), "IndooneNative");
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public void onPermissionRequest(final PermissionRequest request) {
                runOnUiThread(() -> {
                    if (request.getResources() != null) request.grant(request.getResources());
                });
            }
        });
        webView.loadUrl("file:///android_asset/index.html");
        setContentView(webView);
    }

    public void requestCameraPermission() {
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            sendCameraPermissionResult(true, "Camera permission already granted");
            return;
        }
        requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            sendCameraPermissionResult(granted, granted ? "Camera permission granted" : "Camera permission denied");
        }
    }

    public void sendCameraPermissionResult(boolean success, String message) {
        String safe = message == null ? "" : message.replace("\\", "\\\\").replace("'", "\\'");
        if (webView != null) {
            webView.evaluateJavascript("window.dispatchEvent(new CustomEvent('indoone-camera-permission',{detail:{success:" + success + ",message:'" + safe + "'}}));", null);
        }
    }

    public void sendBiometricResult(boolean success, String message) {
        String safe = message == null ? "" : message.replace("\\", "\\\\").replace("'", "\\'");
        webView.evaluateJavascript("window.dispatchEvent(new CustomEvent('indoone-biometric-result',{detail:{success:" + success + ",message:'" + safe + "'}}));", null);
    }

    @Override public void onBackPressed() {
        if (webView.canGoBack()) webView.goBack(); else super.onBackPressed();
    }
}
