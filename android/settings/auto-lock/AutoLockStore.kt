package com.indoone.settings.autolock

import android.content.Context

class AutoLockStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun minutes(): Int {
        val value = prefs.getInt(KEY_MINUTES, DEFAULT_MINUTES)
        return if (value in ALLOWED_MINUTES) value else DEFAULT_MINUTES
    }

    fun setMinutes(value: Int) {
        prefs.edit().putInt(KEY_MINUTES, value.takeIf { it in ALLOWED_MINUTES } ?: DEFAULT_MINUTES).apply()
    }

    companion object {
        const val DEFAULT_MINUTES = 1
        val ALLOWED_MINUTES = setOf(1, 5, 15)
        private const val PREFS_NAME = "indoone_auto_lock"
        private const val KEY_MINUTES = "minutes"
    }
}
