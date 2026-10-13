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
import okhttp3.Request
import org.json.JSONArray
import java.io.File

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

    private val httpClient = OkHttpClient()

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
     * Opens the "install unknown apps" screen. That permission has to be granted
     * before the download is worth starting, otherwise the installer cannot open.
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
     * Downloads the APK into the cache and reports progress as a percentage.
     *
     * This is called from the worker, not from a screen, so the download belongs to
     * the work rather than to the dialog. Closing the dialog leaves it running, and
     * the work survives the app being sent to the background.
     */
    suspend fun downloadApk(
        context: Context,
        update: UpdateInfo,
        onProgress: suspend (Int) -> Unit,
    ): File = withContext(Dispatchers.IO) {
        val updateDir = File(context.cacheDir, UPDATE_DIR).apply { mkdirs() }
        val apkFile = File(updateDir, APK_ASSET_NAME)
        if (apkFile.exists()) apkFile.delete()

        val request = Request.Builder()
            .url(update.downloadUrl)
            .header("User-Agent", "Indoone-Android-Updater")
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                error("APK download failed: HTTP ${response.code}")
            }

            val body = response.body ?: error("APK download returned no file.")
            val totalBytes = body.contentLength().takeIf { it > 0L } ?: update.sizeBytes

            body.byteStream().use { input ->
                apkFile.outputStream().use { output ->
                    val buffer = ByteArray(PROGRESS_BUFFER_BYTES)
                    var copied = 0L
                    var lastPercent = -1

                    while (true) {
                        val read = input.read(buffer)
                        if (read <= 0) break

                        output.write(buffer, 0, read)
                        copied += read

                        if (totalBytes > 0L) {
                            val percent = ((copied * 100L) / totalBytes).toInt().coerceIn(0, 100)
                            if (percent != lastPercent) {
                                lastPercent = percent
                                onProgress(percent)
                            }
                        }
                    }
                }
            }
        }

        if (!apkFile.exists() || apkFile.length() == 0L) {
            error("Downloaded APK is empty.")
        }

        apkFile
    }
}
