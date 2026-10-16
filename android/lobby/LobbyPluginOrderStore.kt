package com.indoone.lobby

import android.content.Context

/**
 * Remembers the order in which plugins were connected.
 *
 * The Lobby shows connected plugins in the order they were connected: the first
 * one connected stays first, a plugin connected later joins at the end, and when
 * one is disconnected the ones after it move up. Nothing in the profile records
 * that order, so it is kept here, per signed-in user.
 */
class LobbyPluginOrderStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    /**
     * Drops plugins that are no longer connected, keeps the ones that are in the
     * order we already knew, and appends any newly connected plugin at the end.
     * The reconciled order is saved and returned.
     */
    fun reconcile(userKey: String, connected: Map<String, Boolean>): List<String> {
        val stillConnected = order(userKey).filter { connected[it] == true }
        val newlyConnected = connected.filterValues { it }.keys.filter { it !in stillConnected }
        val nextOrder = stillConnected + newlyConnected
        save(userKey, nextOrder)
        return nextOrder
    }

    private fun order(userKey: String): List<String> =
        preferences.getString(key(userKey), "")
            .orEmpty()
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

    private fun save(userKey: String, order: List<String>) {
        preferences.edit().putString(key(userKey), order.joinToString(",")).apply()
    }

    private fun key(userKey: String): String = "order_" + userKey.ifBlank { "guest" }

    private companion object {
        const val PREFERENCES_NAME = "indoone_lobby_plugins"
    }
}
