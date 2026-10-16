package com.indoone.settings.update

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.indoone.MainActivity
import com.indoone.R
import com.indoone.notifications.NotificationCategory
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * Downloads an Indoone update as background work, so the download outlives the
 * dialog that started it. The dialog only observes this work: leaving the app
 * changes nothing, and the work survives the process being backgrounded.
 *
 * Progress is published in real bytes, so the app can show how much has arrived
 * and how much is left rather than an abstract percentage.
 */
class UpdateDownloadWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun getForegroundInfo(): ForegroundInfo = foregroundInfo(0L, 0L)

    override suspend fun doWork(): Result {
        val downloadUrl = inputData.getString(KEY_DOWNLOAD_URL)
        val versionCode = inputData.getLong(KEY_VERSION_CODE, 0L)
        val sizeBytes = inputData.getLong(KEY_SIZE_BYTES, 0L)

        if (downloadUrl.isNullOrBlank()) {
            return Result.failure(workDataOf(KEY_ERROR to "The update has no download link."))
        }

        val update = AppUpdateManager.UpdateInfo(
            versionCode = versionCode,
            downloadUrl = downloadUrl,
            sizeBytes = sizeBytes,
        )

        // Android 12 and later can refuse to promote the worker to a foreground
        // service. The download is still worth attempting without the promotion,
        // and the work data reports the progress either way.
        runCatching { setForeground(foregroundInfo(0L, sizeBytes)) }

        val apkFile = runCatching {
            AppUpdateManager.downloadApk(applicationContext, update) { downloaded, total ->
                setProgress(
                    workDataOf(
                        KEY_DOWNLOADED_BYTES to downloaded,
                        KEY_TOTAL_BYTES to total,
                        KEY_PROGRESS to percentOf(downloaded, total),
                    ),
                )
                runCatching { setForeground(foregroundInfo(downloaded, total)) }
            }
        }.getOrElse { error ->
            return Result.failure(
                workDataOf(KEY_ERROR to (error.message ?: "The update download failed.")),
            )
        }

        postReadyNotification(apkFile, versionCode)

        return Result.success(
            workDataOf(
                KEY_VERSION_CODE to versionCode,
                KEY_APK_PATH to apkFile.absolutePath,
                KEY_TOTAL_BYTES to apkFile.length(),
            ),
        )
    }

    private fun foregroundInfo(downloadedBytes: Long, totalBytes: Long): ForegroundInfo {
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_indoone_notification)
            .setContentTitle("Downloading Indoone update")
            .setContentText(progressText(downloadedBytes, totalBytes))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(100, percentOf(downloadedBytes, totalBytes), totalBytes <= 0L)
            .setContentIntent(openAppIntent())
            .build()

        return ForegroundInfo(DOWNLOAD_NOTIFICATION_ID, notification)
    }

    private fun postReadyNotification(apkFile: File, versionCode: Long) {
        val installer = PendingIntent.getActivity(
            applicationContext,
            READY_REQUEST_CODE,
            AppUpdateManager.installerIntent(applicationContext, apkFile),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_indoone_notification)
            .setContentTitle("Indoone update ready to install")
            .setContentText(
                "${AppUpdateManager.formatMegabytes(apkFile.length())} downloaded. " +
                    "Tap to open the installer.",
            )
            .setAutoCancel(true)
            .setContentIntent(installer)
            .build()

        // Without the notification permission the download still finished; only the
        // nudge is missing, and the dialog inside the app still offers Install.
        runCatching {
            NotificationManagerCompat.from(applicationContext)
                .notify(READY_NOTIFICATION_ID, notification)
        }
    }

    private fun openAppIntent(): PendingIntent = PendingIntent.getActivity(
        applicationContext,
        OPEN_REQUEST_CODE,
        Intent(applicationContext, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    companion object {
        const val UNIQUE_NAME = "indoone-update-download"
        const val KEY_DOWNLOAD_URL = "download_url"
        const val KEY_VERSION_CODE = "version_code"
        const val KEY_SIZE_BYTES = "size_bytes"
        const val KEY_DOWNLOADED_BYTES = "downloaded_bytes"
        const val KEY_TOTAL_BYTES = "total_bytes"
        const val KEY_PROGRESS = "progress"
        const val KEY_APK_PATH = "apk_path"
        const val KEY_ERROR = "error"

        // The channel already exists: IndooneNotificationChannels creates it at
        // startup, so no new channel is needed here.
        private val CHANNEL_ID = NotificationCategory.UPDATES.id

        private const val DOWNLOAD_NOTIFICATION_ID = 4201
        private const val READY_NOTIFICATION_ID = 4202
        private const val OPEN_REQUEST_CODE = 4201
        private const val READY_REQUEST_CODE = 4202

        fun enqueue(context: Context, update: AppUpdateManager.UpdateInfo) {
            val request = OneTimeWorkRequestBuilder<UpdateDownloadWorker>()
                .setInputData(
                    workDataOf(
                        KEY_DOWNLOAD_URL to update.downloadUrl,
                        KEY_VERSION_CODE to update.versionCode,
                        KEY_SIZE_BYTES to update.sizeBytes,
                    ),
                )
                // A download with no connection fails on its first read. Waiting for one
                // is cheaper than failing and having the user start it again.
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build(),
                )
                .addTag(UNIQUE_NAME)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(UNIQUE_NAME, ExistingWorkPolicy.REPLACE, request)
        }

        fun observe(context: Context): Flow<List<WorkInfo>> =
            WorkManager.getInstance(context).getWorkInfosForUniqueWorkFlow(UNIQUE_NAME)
    }

    private fun progressText(downloadedBytes: Long, totalBytes: Long): String {
        if (totalBytes <= 0L) {
            return "${AppUpdateManager.formatMegabytes(downloadedBytes)} downloaded"
        }
        val remaining = (totalBytes - downloadedBytes).coerceAtLeast(0L)
        return "${AppUpdateManager.formatMegabytes(downloadedBytes)} of " +
            "${AppUpdateManager.formatMegabytes(totalBytes)} · " +
            "${AppUpdateManager.formatMegabytes(remaining)} left"
    }

    private fun percentOf(downloadedBytes: Long, totalBytes: Long): Int {
        if (totalBytes <= 0L) return 0
        return ((downloadedBytes * 100L) / totalBytes).toInt().coerceIn(0, 100)
    }
}
