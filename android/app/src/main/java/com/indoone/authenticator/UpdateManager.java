package com.indoone.authenticator;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

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
        int horizontalPadding = dp(24);
        int verticalPadding = dp(22);

        LinearLayout content = new LinearLayout(activity);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        content.setPadding(
                horizontalPadding,
                verticalPadding,
                horizontalPadding,
                dp(18)
        );

        TextView title = new TextView(activity);
        title.setText("Indoone " + versionName + " is here!");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setTextColor(Color.rgb(25, 25, 35));
        title.setGravity(Gravity.CENTER);
        content.addView(title, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        TextView subtitle = new TextView(activity);
        subtitle.setText("Update now to get the latest features, improvements and bug fixes.");
        subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        subtitle.setTextColor(Color.rgb(70, 70, 82));
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, dp(10), 0, 0);
        content.addView(subtitle, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        ScrollView notesScroll = new ScrollView(activity);
        notesScroll.setFillViewport(true);

        TextView notesText = new TextView(activity);
        notesText.setText(notes);
        notesText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        notesText.setTextColor(Color.rgb(55, 55, 68));
        notesText.setGravity(Gravity.CENTER);
        notesText.setPadding(0, dp(18), 0, dp(18));
        notesScroll.addView(notesText);

        LinearLayout.LayoutParams notesParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0
        );
        notesParams.weight = 1;
        notesParams.topMargin = dp(4);
        notesParams.bottomMargin = dp(4);
        content.addView(notesScroll, notesParams);

        Button updateButton = createPrimaryButton("Update Now");
        Button laterButton = createSecondaryButton("Later");

        updateButton.setOnClickListener(view -> {
            downloadAndOpenWithSystem(downloadUrl);
        });

        laterButton.setOnClickListener(view -> {
            // The dialog closes without starting an update.
        });

        content.addView(updateButton, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(50)
        ));

        LinearLayout.LayoutParams laterParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(50)
        );
        laterParams.topMargin = dp(10);
        content.addView(laterButton, laterParams);

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setView(content)
                .setCancelable(true)
                .create();

        updateButton.setOnClickListener(view -> {
            dialog.dismiss();
            downloadAndOpenWithSystem(downloadUrl);
        });

        laterButton.setOnClickListener(view -> dialog.dismiss());

        dialog.setOnShowListener(ignored -> {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(createDialogBackground());
                dialog.getWindow().setLayout(
                        dp(340),
                        dp(520)
                );
            }
        });

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(createDialogBackground());
            dialog.getWindow().setLayout(
                    dp(340),
                    dp(520)
            );
        }
    }

    private Button createPrimaryButton(String text) {
        Button button = new Button(activity);
        button.setText(text);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        button.setTextColor(Color.WHITE);
        button.setAllCaps(false);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setGravity(Gravity.CENTER);
        button.setBackground(createPrimaryButtonBackground());
        return button;
    }

    private Button createSecondaryButton(String text) {
        Button button = new Button(activity);
        button.setText(text);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        button.setTextColor(Color.rgb(45, 45, 58));
        button.setAllCaps(false);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setGravity(Gravity.CENTER);
        button.setBackground(createSecondaryButtonBackground());
        return button;
    }

    private GradientDrawable createDialogBackground() {
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.WHITE);
        background.setCornerRadius(dp(28));
        return background;
    }

    private GradientDrawable createPrimaryButtonBackground() {
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.rgb(109, 53, 232));
        background.setCornerRadius(dp(16));
        return background;
    }

    private GradientDrawable createSecondaryButtonBackground() {
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.rgb(242, 242, 248));
        background.setCornerRadius(dp(16));
        return background;
    }

    private int dp(int value) {
        return Math.round(
                value * activity.getResources().getDisplayMetrics().density
        );
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
