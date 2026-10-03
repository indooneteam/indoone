package com.indoone.notifications

import android.content.Context

class NotificationPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(
        "indoone_notifications",
        Context.MODE_PRIVATE,
    )

    fun isEnabled(): Boolean = preferences.getBoolean(KEY_MASTER, true)

    fun setEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_MASTER, enabled).apply()
    }

    fun isCategoryEnabled(category: NotificationCategory): Boolean =
        preferences.getBoolean("category_" + category.id, true)

    fun setCategoryEnabled(category: NotificationCategory, enabled: Boolean) {
        preferences.edit().putBoolean("category_" + category.id, enabled).apply()
    }

    fun wasPermissionRequested(): Boolean =
        preferences.getBoolean(KEY_PERMISSION_REQUESTED, false)

    fun markPermissionRequested() {
        preferences.edit().putBoolean(KEY_PERMISSION_REQUESTED, true).apply()
    }

    fun saveFcmToken(token: String) {
        preferences.edit().putString(KEY_FCM_TOKEN, token.trim()).apply()
    }

    fun fcmToken(): String = preferences.getString(KEY_FCM_TOKEN, "").orEmpty()

    fun registeredFcmToken(): String = preferences.getString(KEY_REGISTERED_FCM_TOKEN, "").orEmpty()

    fun markFcmTokenRegistered(token: String) {
        preferences.edit().putString(KEY_REGISTERED_FCM_TOKEN, token.trim()).apply()
    }

    companion object {
        private const val KEY_MASTER = "master_enabled"
        private const val KEY_PERMISSION_REQUESTED = "permission_requested"
        private const val KEY_FCM_TOKEN = "fcm_token"
        private const val KEY_REGISTERED_FCM_TOKEN = "registered_fcm_token"
    }
}
