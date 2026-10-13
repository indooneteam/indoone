package com.indoone.settings.update

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
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
 * dialog that started it. The dialog only observes this work: closing it changes
 * nothing, and the download also continues while the app is in the background.
 *
 * When the download finishes the worker posts a notification whose tap opens the
 * system package installer. The in-app dialog offers the same Install button, so
 * the user can install from either place.
 */
class UpdateDownloadWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun getForegroundInfo(): ForegroundInfo = foregroundInfo(0)

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
        runCatching { setForeground(foregroundInfo(0)) }

        val apkFile = runCatching {
            AppUpdateManager.downloadApk(applicationContext, update) { percent ->
                setProgress(workDataOf(KEY_PROGRESS to percent))
                runCatching { setForeground(foregroundInfo(percent)) }
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
            ),
        )
    }

    private fun foregroundInfo(percent: Int): ForegroundInfo {
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_indoone_notification)
            .setContentTitle("Downloading Indoone update")
            .setContentText(
                if (percent > 0) "$percent% downloaded" else "Starting the download…",
            )
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(100, percent, percent <= 0)
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
            .setContentTitle("Indoone $versionCode is ready to install")
            .setContentText("Tap to open the installer.")
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
                .addTag(UNIQUE_NAME)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(UNIQUE_NAME, ExistingWorkPolicy.REPLACE, request)
        }

        fun observe(context: Context): Flow<List<WorkInfo>> =
            WorkManager.getInstance(context).getWorkInfosForUniqueWorkFlow(UNIQUE_NAME)
    }
}
