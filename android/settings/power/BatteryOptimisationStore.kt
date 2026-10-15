package com.indoone.settings.power

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings

/**
 * The battery-optimisation exemption is what keeps a Vibe session alive while the
 * app is in another app or the screen is off.
 *
 * Asking for it opens a full system screen. That request must never run on the
 * launch path: run there, it covered the app the moment it opened, so the app
 * looked like it closed on launch. It is offered only from a screen the user
 * opened, and remembered so it is asked at most once.
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

    /**
     * commit(), not apply(): the flag must already be on disk before the system
     * screen takes the app off the foreground, or it can be lost and asked again.
     */
    fun markRequested() {
        preferences.edit().putBoolean(KEY_REQUESTED, true).commit()
    }

    /** True while the user should be offered the exemption: missing and not yet asked. */
    fun shouldOffer(context: Context): Boolean = !wasRequested() && !isExempt(context)

    /** Opens the system exemption screen. Only ever called from a tap. */
    fun openExemptionSettings(activity: Activity) {
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
