package com.indoone.authenticator;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import androidx.core.content.pm.PackageInfoCompat;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public final class UpdateManager {
    private static final String UPDATE_URL = "https://indooneteam.github.io/indoone/develop/update.json";

    private final MainActivity activity;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private long downloadId = -1;

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
                }
                JSONObject json = new JSONObject(body.toString());
                long installedCode = PackageInfoCompat.getLongVersionCode(
                        activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0)
                );
                long remoteCode = json.optLong("versionCode", installedCode);
                if (remoteCode <= installedCode) return;
                String versionName = json.optString("versionName", "New version");
                String notes = json.optString("notes", "Performance and security improvements.");
                String downloadUrl = json.optString("downloadUrl", "");
                if (downloadUrl.isEmpty()) return;
                mainHandler.post(() -> showUpdateDialog(versionName, notes, downloadUrl));
            } catch (Exception ignored) {
                // Update checks must never block or break app startup.
            }
        }).start();
    }

    private void showUpdateDialog(String versionName, String notes, String downloadUrl) {
        new AlertDialog.Builder(activity)
                .setTitle("New Indoone update")
                .setMessage("Version " + versionName + " is available.\n\n" + notes)
                .setNegativeButton("Later", null)
                .setPositiveButton("Update", (dialog, which) -> downloadAndInstall(downloadUrl))
                .setCancelable(true)
                .show();
    }

    private void downloadAndInstall(String downloadUrl) {
        try {
            DownloadManager manager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
            request.setTitle("Indoone update");
            request.setDescription("Downloading the latest Indoone APK");
            request.setMimeType("application/vnd.android.package-archive");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalFilesDir(activity, Environment.DIRECTORY_DOWNLOADS, "indoone-update.apk");
            downloadId = manager.enqueue(request);
            waitForDownload(manager);
        } catch (Exception error) {
            new AlertDialog.Builder(activity).setTitle("Update failed").setMessage("Could not start the update download.").setPositiveButton("OK", null).show();
        }
    }

    private void waitForDownload(DownloadManager manager) {
        mainHandler.postDelayed(() -> {
            DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
            try (android.database.Cursor cursor = manager.query(query)) {
                if (cursor == null || !cursor.moveToFirst()) return;
                int status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    Uri uri = manager.getUriForDownloadedFile(downloadId);
                    if (uri != null) install(uri);
                    return;
                }
                if (status == DownloadManager.STATUS_FAILED) {
                    new AlertDialog.Builder(activity).setTitle("Update failed").setMessage("The APK download failed. Please try again.").setPositiveButton("OK", null).show();
                    return;
                }
            } catch (Exception ignored) {
                return;
            }
            waitForDownload(manager);
        }, 700);
    }

    private void install(Uri downloadUri) {
        if (!activity.getPackageManager().canRequestPackageInstalls()) {
            Intent settingsIntent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + activity.getPackageName()));
            activity.startActivity(settingsIntent);
            new AlertDialog.Builder(activity).setTitle("Allow app updates").setMessage("Enable 'Allow from this source', then return to Indoone and tap Update again.").setPositiveButton("OK", null).show();
            return;
        }

        Intent install = new Intent(Intent.ACTION_INSTALL_PACKAGE);
        install.setDataAndType(downloadUri, "application/vnd.android.package-archive");
        install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(install);
    }
}
