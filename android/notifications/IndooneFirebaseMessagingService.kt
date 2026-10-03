package com.indoone.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class IndooneFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        NotificationPreferences(this).saveFcmToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        // When Indoone is already open, the user is actively waiting for the
        // response in-app. Suppress the push notification in that state.
        if (IndooneAppState.isForeground) return

        val data = message.data

        val category = data["category"]
            ?.trim()
            ?.lowercase()
            ?.let { raw -> NotificationCategory.entries.firstOrNull { it.id == raw } }
            ?: NotificationCategory.UPDATES

        val title = data["title"]
            ?.takeIf { it.isNotBlank() }
            ?: message.notification?.title
            ?: "Indoone"

        val body = data["body"]
            ?.takeIf { it.isNotBlank() }
            ?: message.notification?.body
            ?: return

        IndooneNotificationManager.show(
            context = this,
            category = category,
            title = title,
            body = body,
            openRoute = data["route"],
        )
    }
}
