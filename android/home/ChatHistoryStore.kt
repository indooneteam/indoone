package com.indoone.home

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Persists completed AI conversations locally on the device. */
object ChatHistoryStore {
    private const val PREFS_NAME = "indoone_ai_chat_history"
    private const val KEY_CHATS = "chats"
    private const val MAX_CHATS = 50

    data class StoredMessage(
        val text: String,
        val fromUser: Boolean,
    )

    data class StoredChat(
        val id: String,
        val title: String,
        val messages: List<StoredMessage>,
        val updatedAt: Long,
    )

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(context: Context): List<StoredChat> {
        val raw = prefs(context).getString(KEY_CHATS, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList(array.length()) {
                for (index in 0 until array.length()) {
                    val chat = array.optJSONObject(index) ?: continue
                    val id = chat.optString("id")
                    if (id.isBlank()) continue
                    val messagesJson = chat.optJSONArray("messages") ?: continue
                    val messages = buildList(messagesJson.length()) {
                        for (messageIndex in 0 until messagesJson.length()) {
                            val message = messagesJson.optJSONObject(messageIndex) ?: continue
                            val text = message.optString("text")
                            if (text.isBlank()) continue
                            add(
                                StoredMessage(
                                    text = text,
                                    fromUser = message.optBoolean("fromUser", false),
                                )
                            )
                        }
                    }
                    if (messages.isEmpty()) continue
                    add(
                        StoredChat(
                            id = id,
                            title = chat.optString("title").ifBlank { "New chat" },
                            messages = messages,
                            updatedAt = chat.optLong("updatedAt", 0L),
                        )
                    )
                }
            }.sortedByDescending { it.updatedAt }
        }.getOrDefault(emptyList())
    }

    fun save(context: Context, id: String, messages: List<StoredMessage>) {
        if (id.isBlank() || messages.isEmpty()) return

        val title = messages
            .firstOrNull { it.fromUser }
            ?.text
            ?.trim()
            ?.take(60)
            ?.ifBlank { "New chat" }
            ?: "New chat"

        val updated = StoredChat(
            id = id,
            title = title,
            messages = messages,
            updatedAt = System.currentTimeMillis(),
        )

        val next = (load(context).filterNot { it.id == id } + updated)
            .sortedByDescending { it.updatedAt }
            .take(MAX_CHATS)
        write(context, next)
    }

    fun delete(context: Context, id: String) {
        if (id.isBlank()) return
        write(context, load(context).filterNot { it.id == id })
    }

    private fun write(context: Context, chats: List<StoredChat>) {
        val array = JSONArray()
        chats.forEach { chat ->
            val messages = JSONArray()
            chat.messages.forEach { message ->
                messages.put(
                    JSONObject()
                        .put("text", message.text)
                        .put("fromUser", message.fromUser)
                )
            }
            array.put(
                JSONObject()
                    .put("id", chat.id)
                    .put("title", chat.title)
                    .put("updatedAt", chat.updatedAt)
                    .put("messages", messages)
            )
        }
        prefs(context).edit().putString(KEY_CHATS, array.toString()).apply()
    }
}
