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
    )

    private const val RELEASES_URL =
        "https://api.github.com/repos/indooneteam/indoone/releases?per_page=20"
    private const val RELEASE_TAG_PREFIX = "terminal-build-"
    private const val APK_ASSET_NAME = "indoone-terminal.apk"

    private val httpClient = OkHttpClient()

    suspend fun checkForUpdate(context: Context): UpdateInfo? = withContext(Dispatchers.IO) {
        val currentVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            context.packageManager
                .getPackageInfo(context.packageName, 0)
                .longVersionCode
        } else {
            @Suppress("DEPRECATION")
            context.packageManager
                .getPackageInfo(context.packageName, 0)
                .versionCode
                .toLong()
        }

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
                    if (versionCode > currentVersionCode && downloadUrl.isNotBlank()) {
                        return@withContext UpdateInfo(versionCode, downloadUrl)
                    }
                }
            }

            null
        }
    }

    suspend fun downloadAndInstall(context: Context, update: UpdateInfo) = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !context.packageManager.canRequestPackageInstalls()
        ) {
            withContext(Dispatchers.Main) {
                context.startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:${context.packageName}"),
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
            }
            return@withContext
        }

        val updateDir = File(context.cacheDir, "updates").apply { mkdirs() }
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
            body.byteStream().use { input ->
                apkFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }

        if (!apkFile.exists() || apkFile.length() == 0L) {
            error("Downloaded APK is empty.")
        }

        withContext(Dispatchers.Main) {
            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile,
            )
            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(installIntent)
        }
    }
}
