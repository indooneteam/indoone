package com.indoone.authenticator;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import androidx.core.content.pm.PackageInfoCompat;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public final class UpdateManager {
    private static final String UPDATE_URL = "https://indooneteam.github.io/indoone/develop/update.json?ts=";

    private final MainActivity activity;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private long downloadId = -1;

    public UpdateManager(MainActivity activity) {
        this.activity = activity;
    }

    public void checkForUpdate() {
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                String requestUrl = UPDATE_URL + System.currentTimeMillis();
                connection = (HttpURLConnection) new URL(requestUrl).openConnection();
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setUseCaches(false);
                connection.setRequestProperty("Cache-Control", "no-cache");
                connection.setRequestProperty("Pragma", "no-cache");
                connection.setRequestMethod("GET");
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) return;

                StringBuilder body = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) body.append(line);
                }

                JSONObject json = new JSONObject(body.toString());
                long installedCode = PackageInfoCompat.getLongVersionCode(
                        activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0)
                );
                long remoteCode = json.optLong("versionCode", 0);
                if (remoteCode <= installedCode) return;

                String versionName = json.optString("versionName", "New version");
                String notes = json.optString("notes", "Performance and security improvements.");
                String downloadUrl = json.optString("downloadUrl", "");
                if (downloadUrl.isEmpty()) return;

                mainHandler.post(() -> showUpdateDialog(versionName, notes, downloadUrl));
            } catch (Exception ignored) {
                // Update checks must never block or break app startup.
            } finally {
                if (connection != null) connection.disconnect();
            }
        }).start();
    }

    private void showUpdateDialog(String versionName, String notes, String downloadUrl) {
        new AlertDialog.Builder(activity)
                .setTitle("New Indoone update")
                .setMessage("Version " + versionName + " is available.\n\n" + notes + "\n\nThe APK will be downloaded and Android will ask for installation permission when required.")
                .setNegativeButton("Later", null)
                .setPositiveButton("Update", (dialog, which) -> downloadAndOpenWithSystem(downloadUrl))
                .setCancelable(true)
                .show();
    }

    private void downloadAndOpenWithSystem(String downloadUrl) {
        try {
            DownloadManager manager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl + (downloadUrl.contains("?") ? "&" : "?") + "ts=" + System.currentTimeMillis()));
            request.setTitle("Indoone update");
            request.setDescription("Downloading the latest Indoone APK");
            request.setMimeType("application/vnd.android.package-archive");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalFilesDir(activity, Environment.DIRECTORY_DOWNLOADS, "indoone-update.apk");
            downloadId = manager.enqueue(request);
            waitForDownload(manager);
        } catch (Exception error) {
            showError("Could not start the update download.");
        }
    }

    private void waitForDownload(DownloadManager manager) {
        mainHandler.postDelayed(() -> {
            try (Cursor cursor = manager.query(new DownloadManager.Query().setFilterById(downloadId))) {
                if (cursor == null || !cursor.moveToFirst()) {
                    showError("The update download could not be found.");
                    return;
                }

                int status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    Uri uri = manager.getUriForDownloadedFile(downloadId);
                    if (uri == null) {
                        showError("The downloaded update file is unavailable.");
                        return;
                    }
                    openSystemPackageHandler(uri);
                    return;
                }

                if (status == DownloadManager.STATUS_FAILED) {
                    showError("The APK download failed. Please try again.");
                    return;
                }
            } catch (Exception error) {
                showError("Could not open the downloaded update.");
                return;
            }
            waitForDownload(manager);
        }, 700);
    }

    private void openSystemPackageHandler(Uri apkUri) {
        try {
            Intent installIntent = new Intent(Intent.ACTION_VIEW);
            installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(installIntent);
        } catch (Exception error) {
            try {
                Intent fallback = new Intent(Intent.ACTION_VIEW, apkUri);
                fallback.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(fallback);
            } catch (Exception fallbackError) {
                showError("No Android app is available to open and install this APK.");
            }
        }
    }

    private void showError(String message) {
        mainHandler.post(() -> new AlertDialog.Builder(activity)
                .setTitle("Update unavailable")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show());
    }
}
