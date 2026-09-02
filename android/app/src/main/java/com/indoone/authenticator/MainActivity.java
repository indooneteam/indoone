package com.indoone.authenticator;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

public class MainActivity extends FragmentActivity {
    private static final int CAMERA_REQUEST_CODE = 41;
    private static final int NEARBY_REQUEST_CODE = 42;
    private WebView webView;
    private NearbyConnectionManager nearbyConnectionManager;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        nearbyConnectionManager = new NearbyConnectionManager(this);
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

    public void requestNearbyPermissions() {
        java.util.ArrayList<String> permissions = new java.util.ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN);
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT);
            permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE);
        } else {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES);
        }
        ArrayListCompat.request(this, permissions.toArray(new String[0]), NEARBY_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            sendCameraPermissionResult(granted, granted ? "Camera permission granted" : "Camera permission denied");
        } else if (requestCode == NEARBY_REQUEST_CODE) {
            boolean granted = grantResults.length > 0;
            for (int result : grantResults) granted &= result == PackageManager.PERMISSION_GRANTED;
            sendNearbyEvent("permissions", granted ? "granted" : "denied", "");
        }
    }

    public void sendCameraPermissionResult(boolean success, String message) {
        String safe = message == null ? "" : message.replace("\\", "\\\\").replace("'", "\\'");
        if (webView != null) {
            webView.evaluateJavascript("window.dispatchEvent(new CustomEvent('indoone-camera-permission',{detail:{success:" + success + ",message:'" + safe + "'}}));", null);
        }
    }

    public void sendBiometricResult(boolean success, String message, String pin) {
        String safe = message == null ? "" : message.replace("\\", "\\\\").replace("'", "\\'");
        String safePin = pin == null ? "" : pin.replace("\\", "\\\\").replace("'", "\\'");
        if (webView != null) {
            webView.evaluateJavascript("window.dispatchEvent(new CustomEvent('indoone-biometric-result',{detail:{success:" + success + ",message:'" + safe + "',pin:'" + safePin + "'}}));", null);
        }
    }

    public void sendNearbyEvent(String type, String message, String endpointId) {
        String safeType = escape(message == null ? type : type);
        String safeMessage = escape(message);
        String safeEndpoint = escape(endpointId);
        if (webView != null) {
            webView.evaluateJavascript("window.dispatchEvent(new CustomEvent('indoone-nearby',{detail:{type:'" + safeType + "',message:'" + safeMessage + "',endpointId:'" + safeEndpoint + "'}}));", null);
        }
    }

    public NearbyConnectionManager getNearbyConnectionManager() {
        return nearbyConnectionManager;
    }

    private static String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("'", "\\'");
    }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack(); else super.onBackPressed();
    }

    private static final class ArrayListCompat {
        static void request(MainActivity activity, String[] permissions, int requestCode) {
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
        }
    }
}
