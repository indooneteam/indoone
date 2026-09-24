package com.indoone.home

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ChatCleanupWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result =
        runCatching {
            if (com.google.firebase.auth.FirebaseAuth.getInstance().currentUser != null) {
                CloudCleanup.run()
            }
            Result.success()
        }.getOrElse {
            Result.retry()
        }

    private object CloudCleanup {
        suspend fun run() {
            CloudChatRepository().cleanupExpiredConversations()
        }
    }
}
