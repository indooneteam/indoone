package com.indoone.settings.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import org.json.JSONArray
import java.io.File
import java.util.Locale
import java.util.concurrent.TimeUnit

object AppUpdateManager {
    data class UpdateInfo(
        val versionCode: Long,
        val downloadUrl: String,
        val sizeBytes: Long,
    )

    private const val RELEASES_URL =
        "https://api.github.com/repos/indooneteam/indoone/releases?per_page=20"
    private const val RELEASE_TAG_PREFIX = "terminal-build-"
    private const val APK_ASSET_NAME = "indoone-terminal.apk"
    private const val UPDATE_DIR = "updates"
    private const val PROGRESS_BUFFER_BYTES = 64 * 1024
    private const val PROGRESS_INTERVAL_MS = 250L

    private val httpClient = OkHttpClient()

    /**
     * The APK download gets its own client, pinned to HTTP/1.1.
     *
     * On HTTP/2 one stream is paced by the connection's flow-control window, so the
     * throughput settles at roughly window / round-trip time. That window is 64 KB, so
     * on a mobile link whose round trip is a few hundred milliseconds the download
     * crawls at a couple of hundred KB/s — which is exactly the speed reported on
     * mobile data. Over Wi-Fi the same code looks fast because the round trip there is
     * a few milliseconds, so the same 64 KB refills almost instantly.
     *
     * HTTP/1.1 hands pacing back to TCP window scaling, which the platform sizes far
     * more generously than 64 KB. That is why a plain browser download is fast on the
     * same phone and the same network.
     */
    private val downloadClient = OkHttpClient.Builder()
        .protocols(listOf(Protocol.HTTP_1_1))
        .retryOnConnectionFailure(true)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * One place formats sizes, so the dialog and the notification cannot disagree
     * about how big the download is.
     */
    fun formatMegabytes(bytes: Long): String {
        if (bytes <= 0L) return "0 MB"
        return String.format(Locale.US, "%.1f MB", bytes.toDouble() / (1024.0 * 1024.0))
    }

    fun currentVersionCode(context: Context): Long {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            info.versionCode.toLong()
        }
    }

    fun apkFile(context: Context): File =
        File(File(context.cacheDir, UPDATE_DIR), APK_ASSET_NAME)

    /**
     * True when the system will let us open the package installer. Android only
     * allows installing packages after the user has allowed it for this app.
     */
    fun canInstall(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
            context.packageManager.canRequestPackageInstalls()

    /**
     * Opens the "install unknown apps" screen. This is only ever called from a
     * button the user pressed: jumping here on its own made the app look like it
     * was closing the moment an update was started.
     */
    fun openInstallPermissionSettings(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        runCatching {
            context.startActivity(
                Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:${context.packageName}"),
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }

    /**
     * The intent that installs a downloaded APK. When the app is not allowed to
     * install packages yet this is the settings screen instead, so a caller always
     * has something useful to open.
     */
    fun installerIntent(context: Context, apkFile: File): Intent {
        if (!canInstall(context)) {
            return Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}"),
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val apkUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile,
        )

        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun installApk(context: Context, apkFile: File) {
        runCatching { context.startActivity(installerIntent(context, apkFile)) }
    }

    suspend fun checkForUpdate(context: Context): UpdateInfo? = withContext(Dispatchers.IO) {
        val installedVersionCode = currentVersionCode(context)

        val request = Request.Builder()
            .url(RELEASES_URL)
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", "Indoone-Android-Updater")
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                error("Update check failed: HTTP ${response.code}")
            }

            val body = response.body?.string().orEmpty()
            val releases = JSONArray(body)

            for (index in 0 until releases.length()) {
                val release = releases.optJSONObject(index) ?: continue
                if (release.optBoolean("draft") || release.optBoolean("prerelease")) continue

                val tag = release.optString("tag_name")
                if (!tag.startsWith(RELEASE_TAG_PREFIX)) continue

                val versionCode = tag.removePrefix(RELEASE_TAG_PREFIX).toLongOrNull() ?: continue
                val assets = release.optJSONArray("assets") ?: continue

                for (assetIndex in 0 until assets.length()) {
                    val asset = assets.optJSONObject(assetIndex) ?: continue
                    if (asset.optString("name") != APK_ASSET_NAME) continue

                    val downloadUrl = asset.optString("browser_download_url")
                    if (versionCode > installedVersionCode && downloadUrl.isNotBlank()) {
                        return@withContext UpdateInfo(
                            versionCode = versionCode,
                            downloadUrl = downloadUrl,
                            sizeBytes = asset.optLong("size"),
                        )
                    }
                }
            }

            null
        }
    }

    /**
     * Downloads the APK into the cache and reports real byte progress.
     *
     * This is called from the worker, not from a screen, so the download belongs to
     * the work rather than to the dialog. Leaving the app does not stop it.
     *
     * Progress is throttled by time rather than by percentage. Each report writes to
     * the work database and updates a notification from the same coroutine that is
     * draining the socket, and every pause lets the receive window close; on a mobile
     * link reopening it costs a full round trip. Reporting on every percent meant
     * around a hundred such pauses per download.
     */
    suspend fun downloadApk(
        context: Context,
        update: UpdateInfo,
        onProgress: suspend (downloadedBytes: Long, totalBytes: Long) -> Unit,
    ): File = withContext(Dispatchers.IO) {
        val updateDir = File(context.cacheDir, UPDATE_DIR).apply { mkdirs() }
        val apkFile = File(updateDir, APK_ASSET_NAME)
        if (apkFile.exists()) apkFile.delete()

        val request = Request.Builder()
            .url(update.downloadUrl)
            .header("User-Agent", "Indoone-Android-Updater")
            .build()

        downloadClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                error("APK download failed: HTTP ${response.code}")
            }

            val body = response.body ?: error("APK download returned no file.")
            val totalBytes = body.contentLength().takeIf { it > 0L } ?: update.sizeBytes
            onProgress(0L, totalBytes)

            body.byteStream().use { input ->
                apkFile.outputStream().use { output ->
                    val buffer = ByteArray(PROGRESS_BUFFER_BYTES)
                    var copied = 0L
                    var lastReportedAt = 0L

                    while (true) {
                        val read = input.read(buffer)
                        if (read <= 0) break

                        output.write(buffer, 0, read)
                        copied += read

                        // A few reports a second is plenty for the figure the user
                        // reads, and it keeps the read loop moving.
                        val now = System.currentTimeMillis()
                        if (now - lastReportedAt >= PROGRESS_INTERVAL_MS) {
                            lastReportedAt = now
                            onProgress(copied, totalBytes)
                        }
                    }

                    onProgress(copied, totalBytes)
                }
            }
        }

        if (!apkFile.exists() || apkFile.length() == 0L) {
            error("Downloaded APK is empty.")
        }

        apkFile
    }
}
