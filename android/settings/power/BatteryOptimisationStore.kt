package com.indoone.settings.power

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings

/**
 * The battery-optimisation exemption, which is what keeps a Vibe session alive
 * while the app is in another app or the screen is off.
 *
 * Asking for it shows a system dialog that covers the app. Asking on every launch
 * made the app look like it closed the moment it opened, so the request is
 * remembered and made at most once per install.
 */
class BatteryOptimisationStore(context: Context) {
    private val preferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE,
    )

    fun isExempt(context: Context): Boolean {
        val power = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return power.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun wasRequested(): Boolean = preferences.getBoolean(KEY_REQUESTED, false)

    private fun markRequested() {
        preferences.edit().putBoolean(KEY_REQUESTED, true).apply()
    }

    /** Asks once, ever, and only while the exemption is not already in place. */
    fun requestOnce(activity: Activity) {
        if (wasRequested() || isExempt(activity)) return
        markRequested()
        runCatching {
            activity.startActivity(
                Intent(
                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                    Uri.parse("package:${activity.packageName}"),
                ),
            )
        }
    }

    companion object {
        private const val PREFS_NAME = "indoone_battery_optimisation"
        private const val KEY_REQUESTED = "requested"
    }
}
