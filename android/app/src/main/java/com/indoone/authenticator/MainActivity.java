package com.indoone.authenticator;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import java.io.InputStream;

public class MainActivity extends FragmentActivity {
    private static final int CAMERA_REQUEST_CODE = 41;
    private static final int NEARBY_REQUEST_CODE = 42;
    private static final int BLUETOOTH_ENABLE_REQUEST_CODE = 43;
    private static final int WIFI_ENABLE_REQUEST_CODE = 44;
    private static final String APP_BASE_URL = "https://indooneteam.github.io/indoone/develop/";
    private WebView webView;
    private NearbyConnectionManager nearbyConnectionManager;
    private UpdateManager updateManager;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        // Indoone is a fixed phone-style UI. Prevent browser-like pinch/double-tap zoom.
        s.setSupportZoom(false);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setLoadWithOverviewMode(false);
        s.setUseWideViewPort(false);
        s.setTextZoom(100);
        nearbyConnectionManager = new NearbyConnectionManager(this);
        updateManager = new UpdateManager(this);
        webView.addJavascriptInterface(new NativeBridge(this), "IndooneNative");
        webView.setWebViewClient(new WebViewClient() {
            @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return serveBundledAsset(request.getUrl());
            }

            @Override public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                return serveBundledAsset(Uri.parse(url));
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public void onPermissionRequest(final PermissionRequest request) {
                runOnUiThread(() -> {
                    if (request.getResources() != null) request.grant(request.getResources());
                });
            }
        });
        loadBundledWebApp();
        setContentView(webView);
        webView.postDelayed(() -> updateManager.checkForUpdate(), 1800);
    }

    private void loadBundledWebApp() {
        try (InputStream input = getAssets().open("index.html")) {
            byte[] bytes = new byte[input.available()];
            int offset = 0;
            while (offset < bytes.length) {
                int read = input.read(bytes, offset, bytes.length - offset);
                if (read < 0) break;
                offset += read;
            }
            String html = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
            webView.loadDataWithBaseURL(APP_BASE_URL, html, "text/html", "UTF-8", APP_BASE_URL);
        } catch (Exception error) {
            webView.loadUrl("file:///android_asset/index.html");
        }
    }

    private WebResourceResponse serveBundledAsset(Uri uri) {
        if (uri == null) return null;
        String url = uri.toString();
        if (!url.startsWith(APP_BASE_URL)) return null;

        String relative = url.substring(APP_BASE_URL.length());
        int queryIndex = relative.indexOf('?');
        if (queryIndex >= 0) relative = relative.substring(0, queryIndex);
        int fragmentIndex = relative.indexOf('#');
        if (fragmentIndex >= 0) relative = relative.substring(0, fragmentIndex);
        if (relative.isEmpty()) relative = "index.html";
        relative = relative.replace("%20", " ");
        if (relative.contains("..")) return null;

        String mime = mimeType(relative);
        if (mime == null) return null;

        try {
            AssetManager assets = getAssets();
            InputStream input = assets.open(relative, AssetManager.ACCESS_STREAMING);
            return new WebResourceResponse(mime, "UTF-8", input);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String mimeType(String path) {
        String value = String.valueOf(path).toLowerCase(java.util.Locale.US);
        if (value.endsWith(".html") || value.endsWith(".htm")) return "text/html";
        if (value.endsWith(".css")) return "text/css";
        if (value.endsWith(".js")) return "application/javascript";
        if (value.endsWith(".json")) return "application/json";
        if (value.endsWith(".svg")) return "image/svg+xml";
        if (value.endsWith(".png")) return "image/png";
        if (value.endsWith(".jpg") || value.endsWith(".jpeg")) return "image/jpeg";
        if (value.endsWith(".webp")) return "image/webp";
        if (value.endsWith(".gif")) return "image/gif";
        if (value.endsWith(".ico")) return "image/x-icon";
        return null;
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
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.BLUETOOTH_SCAN);
            }
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE);
            }
        } else if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2
                && checkSelfPermission(Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES);
        }

        if (!permissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]), NEARBY_REQUEST_CODE);
            return;
        }

        ensureNearbyTransportReady();
    }

    private void ensureNearbyTransportReady() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            BluetoothManager manager = getSystemService(BluetoothManager.class);
            BluetoothAdapter adapter = manager == null ? null : manager.getAdapter();

            if (adapter == null) {
                sendNearbyEvent("permissions", "denied", "");
                return;
            }

            if (!adapter.isEnabled()) {
                try {
                    startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), BLUETOOTH_ENABLE_REQUEST_CODE);
                } catch (Exception error) {
                    sendNearbyEvent("error", "bluetoothEnableUnavailable", error.getMessage());
                }
                return;
            }
        }

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if (wifiManager == null) {
            sendNearbyEvent("permissions", "denied", "");
            return;
        }

        if (!wifiManager.isWifiEnabled()) {
            try {
                Intent wifiIntent = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                        ? new Intent(Settings.Panel.ACTION_WIFI)
                        : new Intent(Settings.ACTION_WIFI_SETTINGS);
                startActivityForResult(wifiIntent, WIFI_ENABLE_REQUEST_CODE);
            } catch (Exception error) {
                try {
                    startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), WIFI_ENABLE_REQUEST_CODE);
                } catch (Exception fallbackError) {
                    sendNearbyEvent("error", "wifiEnableUnavailable", fallbackError.getMessage());
                }
            }
            return;
        }

        sendNearbyEvent("permissions", "granted", "");
    }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BLUETOOTH_ENABLE_REQUEST_CODE || requestCode == WIFI_ENABLE_REQUEST_CODE) {
            ensureNearbyTransportReady();
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            sendCameraPermissionResult(granted, granted ? "Camera permission granted" : "Camera permission denied");
        } else if (requestCode == NEARBY_REQUEST_CODE) {
            boolean granted = grantResults.length > 0;
            for (int result : grantResults) granted &= result == PackageManager.PERMISSION_GRANTED;
            if (granted) {
                ensureNearbyTransportReady();
            } else {
                sendNearbyEvent("permissions", "denied", "");
            }
        }
    }

    public void sendCameraPermissionResult(boolean success, String message) {
        String safe = escape(message);
        if (webView != null) webView.evaluateJavascript("window.dispatchEvent(new CustomEvent('indoone-camera-permission',{detail:{success:" + success + ",message:'" + safe + "'}}));", null);
    }

    public void sendBiometricResult(boolean success, String message, String pin) {
        String safe = escape(message);
        String safePin = escape(pin);
        if (webView != null) webView.evaluateJavascript("window.dispatchEvent(new CustomEvent('indoone-biometric-result',{detail:{success:" + success + ",message:'" + safe + "',pin:'" + safePin + "'}}));", null);
    }

    public void sendNearbyEvent(String type, String message, String endpointId) {
        sendNearbyEvent(type, message, endpointId, "", false);
    }

    public void sendNearbyEvent(String type, String message, String endpointId, String authenticationDigits, boolean incoming) {
        String safeType = escape(type);
        String safeMessage = escape(message);
        String safeEndpoint = escape(endpointId);
        String safeDigits = escape(authenticationDigits);
        if (webView != null) webView.evaluateJavascript("window.dispatchEvent(new CustomEvent('indoone-nearby',{detail:{type:'" + safeType + "',message:'" + safeMessage + "',endpointId:'" + safeEndpoint + "',authenticationDigits:'" + safeDigits + "',incoming:" + incoming + "'}}));", null);
    }

    public NearbyConnectionManager getNearbyConnectionManager() { return nearbyConnectionManager; }

    private static String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("'", "\\'");
    }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack(); else super.onBackPressed();
    }
}
