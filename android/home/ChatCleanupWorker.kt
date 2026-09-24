package com.indoone.home

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.indoone.menu.data.CloudChatRepository

class ChatCleanupWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result =
        runCatching {
            if (FirebaseAuth.getInstance().currentUser != null) {
                CloudChatRepository().cleanupExpiredConversations()
            }
            Result.success()
        }.getOrElse {
            Result.retry()
        }
}
