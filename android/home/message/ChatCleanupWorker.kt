package com.indoone.home.message

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.indoone.home.message.CloudChatRepository

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
