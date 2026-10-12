package com.indoone.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object IndooneNotificationChannels {
    const val DEFAULT_CHANNEL_ID = "updates"

    fun create(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(NotificationManager::class.java)
        NotificationCategory.entries.forEach { category ->
            manager.createNotificationChannel(
                NotificationChannel(
                    category.id,
                    category.title,
                    importanceFor(category),
                ).apply {
                    description = category.description
                },
            )
        }
    }

    private fun importanceFor(category: NotificationCategory): Int =
        when (category) {
            NotificationCategory.SECURITY -> NotificationManager.IMPORTANCE_HIGH
            else -> NotificationManager.IMPORTANCE_DEFAULT
        }
}
