package com.indoone.home.message

import android.content.Context

/**
 * A thumb on a reply, kept on the device.
 *
 * Local only, deliberately. There is no endpoint that accepts a rating yet, and a button
 * that pretends to send one is worse than no button: the rating is stored here so it can
 * go to the backend the day it has somewhere to put it. When that endpoint exists, this
 * class is the only thing that changes.
 *
 * SharedPreferences rather than the pending-turn database: a rating is one short string
 * keyed by the message, it never needs a query, and it must not add a schema version to
 * the store that carries unsent turns.
 */
object MessageFeedbackStore {
    private const val PREFS = "indoone_message_feedback"
    private const val MAX_ENTRIES = 200

    private fun key(text: String): String = "r_" + text.hashCode().toString()

    fun record(context: Context, text: String, rating: String) {
        val value = text.trim()

        if (value.isEmpty() || (rating != UP && rating != DOWN)) {
            return
        }

        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(key(value), rating)

        // Keep the file from growing without bound on a long-lived install.
        if (prefs.all.size > MAX_ENTRIES) {
            editor.clear()
            editor.putString(key(value), rating)
        }

        editor.apply()
    }

    fun ratingFor(context: Context, text: String): String {
        val value = text.trim()

        if (value.isEmpty()) {
            return ""
        }

        return context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(key(value), "")
            .orEmpty()
    }

    const val UP = "up"
    const val DOWN = "down"
}
