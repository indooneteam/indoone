package com.indoone.authenticator;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;

import java.nio.charset.StandardCharsets;

/** Native transport layer for nearby device discovery and user-approved pairing. */
public final class NearbyConnectionManager {
    private static final String TAG = "IndooneNearby";
    public static final String SERVICE_ID = "com.indoone.authenticator.connect";
    private static final Strategy STRATEGY = Strategy.P2P_POINT_TO_POINT;

    private final MainActivity activity;
    private final com.google.android.gms.nearby.connection.ConnectionsClient client;

    public NearbyConnectionManager(MainActivity activity) {
        this.activity = activity;
        Context context = activity.getApplicationContext();
        this.client = Nearby.getConnectionsClient(context);
    }

    public void startAdvertising(String deviceName) {
        AdvertisingOptions options = new AdvertisingOptions.Builder().setStrategy(STRATEGY).build();
        client.startAdvertising(
                deviceName == null || deviceName.trim().isEmpty() ? "Indoone Device" : deviceName.trim(),
                SERVICE_ID,
                lifecycleCallback,
                options
        ).addOnSuccessListener(unused -> emit("advertisingStarted", "true", null))
         .addOnFailureListener(error -> emit("error", "advertisingFailed", error.getMessage()));
    }

    public void startDiscovery() {
        DiscoveryOptions options = new DiscoveryOptions.Builder().setStrategy(STRATEGY).build();
        client.startDiscovery(SERVICE_ID, endpointDiscoveryCallback, options)
                .addOnSuccessListener(unused -> emit("discoveryStarted", "true", null))
                .addOnFailureListener(error -> emit("error", "discoveryFailed", error.getMessage()));
    }

    public void requestConnection(String endpointId, String deviceName) {
        if (endpointId == null || endpointId.isEmpty()) {
            emit("error", "missingEndpoint", null);
            return;
        }
        client.requestConnection(
                deviceName == null || deviceName.trim().isEmpty() ? "Indoone Device" : deviceName.trim(),
                endpointId,
                lifecycleCallback
        ).addOnSuccessListener(unused -> emit("requestSent", "true", endpointId))
         .addOnFailureListener(error -> emit("error", "requestFailed", error.getMessage()));
    }

    public void acceptConnection(String endpointId) {
        if (endpointId == null || endpointId.isEmpty()) return;
        client.acceptConnection(endpointId, payloadCallback)
                .addOnSuccessListener(unused -> emit("accepted", "true", endpointId))
                .addOnFailureListener(error -> emit("error", "acceptFailed", error.getMessage()));
    }

    public void rejectConnection(String endpointId) {
        if (endpointId == null || endpointId.isEmpty()) return;
        client.rejectConnection(endpointId)
                .addOnSuccessListener(unused -> emit("rejected", "true", endpointId))
                .addOnFailureListener(error -> emit("error", "rejectFailed", error.getMessage()));
    }

    public void stop() {
        client.stopAdvertising();
        client.stopDiscovery();
        client.stopAllEndpoints();
        emit("stopped", "true", null);
    }

    public void sendText(String endpointId, String text) {
        if (endpointId == null || endpointId.isEmpty()) return;
        byte[] bytes = (text == null ? "" : text).getBytes(StandardCharsets.UTF_8);
        client.sendPayload(endpointId, Payload.fromBytes(bytes))
                .addOnFailureListener(error -> emit("error", "sendFailed", error.getMessage()));
    }

    private final EndpointDiscoveryCallback endpointDiscoveryCallback = new EndpointDiscoveryCallback() {
        @Override public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info) {
            emit("endpointFound", info.getEndpointName(), endpointId);
        }

        @Override public void onEndpointLost(String endpointId) {
            emit("endpointLost", endpointId, null);
        }
    };

    private final ConnectionLifecycleCallback lifecycleCallback = new ConnectionLifecycleCallback() {
        @Override public void onConnectionInitiated(String endpointId, ConnectionInfo info) {
            emit("connectionInitiated", info.getEndpointName(), endpointId, info.getAuthenticationDigits(), info.isIncomingConnection());
        }

        @Override public void onConnectionResult(String endpointId, ConnectionResolution result) {
            emit("connectionResult", result.getStatus().isSuccess() ? "connected" : "rejected", endpointId);
        }

        @Override public void onDisconnected(String endpointId) {
            emit("disconnected", endpointId, null);
        }
    };

    private final PayloadCallback payloadCallback = new PayloadCallback() {
        @Override public void onPayloadReceived(String endpointId, Payload payload) {
            if (payload.getType() != Payload.Type.BYTES || payload.asBytes() == null) return;
            String text = new String(payload.asBytes(), StandardCharsets.UTF_8);
            emit("payload", text, endpointId);
        }

        @Override public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
            // File transfer progress will be added in the transfer layer.
        }
    };

    private void emit(String type, String message, String endpointId) {
        emit(type, message, endpointId, null, false);
    }

    private void emit(String type, String message, String endpointId, String authDigits, boolean incoming) {
        String safeType = escape(type);
        String safeMessage = escape(message);
        String safeEndpoint = escape(endpointId == null ? "" : endpointId);
        String safeDigits = escape(authDigits == null ? "" : authDigits);
        Log.d(TAG, safeType + ": " + safeMessage + " / " + safeEndpoint);
        activity.sendNearbyEvent(safeType, safeMessage, safeEndpoint, safeDigits, incoming);
    }

    private static String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("'", "\\'");
    }
}
