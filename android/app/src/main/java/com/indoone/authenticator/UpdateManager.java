package com.indoone.authenticator;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.core.content.pm.PackageInfoCompat;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public final class UpdateManager {
    private static final String UPDATE_URL = "https://indooneteam.github.io/indoone/develop/update.json";
    private static final String INDUS_APPSTORE_PACKAGE = "com.indus.appstore";
    private static final String INDUS_SEARCH_URL = "https://www.indusappstore.com/search/?ts=Indoone";

    private final MainActivity activity;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public UpdateManager(MainActivity activity) {
        this.activity = activity;
    }

    public void checkForUpdate() {
        new Thread(() -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(UPDATE_URL).openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.setRequestMethod("GET");
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) return;

                StringBuilder body = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) body.append(line);
                } finally {
                    connection.disconnect();
                }

                JSONObject json = new JSONObject(body.toString());
                long installedCode = PackageInfoCompat.getLongVersionCode(
                        activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0)
                );
                long remoteCode = json.optLong("versionCode", installedCode);
                if (remoteCode <= installedCode) return;

                String versionName = json.optString("versionName", "New version");
                String notes = json.optString("notes", "Performance and security improvements.");
                String storeUrl = json.optString("storeUrl", INDUS_SEARCH_URL);
                if (storeUrl.isEmpty()) storeUrl = INDUS_SEARCH_URL;

                final String resolvedStoreUrl = storeUrl;
                mainHandler.post(() -> showUpdateDialog(versionName, notes, resolvedStoreUrl));
            } catch (Exception ignored) {
                // Update checks must never block or break app startup.
            }
        }).start();
    }

    private void showUpdateDialog(String versionName, String notes, String storeUrl) {
        new AlertDialog.Builder(activity)
                .setTitle("New Indoone update")
                .setMessage("Version " + versionName + " is available.\n\n" + notes + "\n\nUpdate through the Indus Appstore to keep Android's trusted app-store installation flow.")
                .setNegativeButton("Later", null)
                .setPositiveButton("Update", (dialog, which) -> openIndusStore(storeUrl))
                .setCancelable(true)
                .show();
    }

    private void openIndusStore(String storeUrl) {
        try {
            PackageManager packageManager = activity.getPackageManager();
            Intent storeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(storeUrl));
            storeIntent.setPackage(INDUS_APPSTORE_PACKAGE);
            if (storeIntent.resolveActivity(packageManager) != null) {
                activity.startActivity(storeIntent);
                return;
            }

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(storeUrl));
            activity.startActivity(browserIntent);
        } catch (Exception error) {
            new AlertDialog.Builder(activity)
                    .setTitle("Update unavailable")
                    .setMessage("Please open the Indus Appstore and update Indoone from its app listing.")
                    .setPositiveButton("OK", null)
                    .show();
        }
    }
}
