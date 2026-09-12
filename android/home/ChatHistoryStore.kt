package com.indoone.home

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object ChatHistoryStore {
    private const val PREFS_NAME = "indoone_ai_chat_history"
    private const val KEY_CHATS = "chats"
    private const val KEY_NEW_CHAT_REQUEST = "new_chat_request"
    private const val MAX_CHATS = 50
    private const val LOCAL_USER_KEY = "local"

    data class StoredMessage(val text: String, val fromUser: Boolean)
    data class StoredChat(val id: String, val title: String, val messages: List<StoredMessage>, val updatedAt: Long)

    private fun prefs(context: Context, userKey: String) = context.applicationContext.getSharedPreferences("$PREFS_NAME.$userKey", Context.MODE_PRIVATE)

    private fun parseChats(raw: String?): List<StoredChat> {
        if (raw.isNullOrBlank()) return emptyList()
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
                            add(StoredMessage(text, message.optBoolean("fromUser", false)))
                        }
                    }
                    if (messages.isEmpty()) continue
                    add(StoredChat(id, chat.optString("title").ifBlank { "New chat" }, messages, chat.optLong("updatedAt", 0L)))
                }
            }.sortedByDescending { it.updatedAt }
        }.getOrDefault(emptyList())
    }

    fun requestNewChat(context: Context, userKey: String) {
        if (userKey.isNotBlank()) prefs(context, userKey).edit().putBoolean(KEY_NEW_CHAT_REQUEST, true).apply()
    }

    private fun consumeNewChatRequest(context: Context, userKey: String): Boolean {
        val requested = prefs(context, userKey).getBoolean(KEY_NEW_CHAT_REQUEST, false)
        if (requested) prefs(context, userKey).edit().remove(KEY_NEW_CHAT_REQUEST).apply()
        return requested
    }

    fun load(context: Context, userKey: String): List<StoredChat> {
        if (consumeNewChatRequest(context, userKey)) return emptyList()
        val primary = parseChats(prefs(context, userKey).getString(KEY_CHATS, null))
        if (primary.isNotEmpty() || userKey == LOCAL_USER_KEY) return primary
        return parseChats(prefs(context, LOCAL_USER_KEY).getString(KEY_CHATS, null))
    }

    fun save(context: Context, userKey: String, id: String, messages: List<StoredMessage>) {
        if (userKey.isBlank() || id.isBlank() || messages.isEmpty()) return
        val title = messages.firstOrNull { it.fromUser }?.text?.trim()?.take(60)?.ifBlank { "New chat" } ?: "New chat"
        val updated = StoredChat(id, title, messages, System.currentTimeMillis())
        val next = (load(context, userKey).filterNot { it.id == id } + updated).sortedByDescending { it.updatedAt }.take(MAX_CHATS)
        write(context, userKey, next)
    }

    fun delete(context: Context, userKey: String, id: String) {
        if (userKey.isBlank() || id.isBlank()) return
        write(context, userKey, load(context, userKey).filterNot { it.id == id })
    }

    private fun write(context: Context, userKey: String, chats: List<StoredChat>) {
        val array = JSONArray()
        chats.forEach { chat ->
            val messages = JSONArray()
            chat.messages.forEach { message -> messages.put(JSONObject().put("text", message.text).put("fromUser", message.fromUser)) }
            array.put(JSONObject().put("id", chat.id).put("title", chat.title).put("updatedAt", chat.updatedAt).put("messages", messages))
        }
        prefs(context, userKey).edit().putString(KEY_CHATS, array.toString()).commit()
    }
}
