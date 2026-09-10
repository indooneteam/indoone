package com.indoone.home

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class ChatResponse(
    val conversationId: String,
    val reply: String,
)

object ChatApi {
    private const val BACKEND_URL = "http://10.0.2.2:8000"
    private const val REQUEST_TIMEOUT_MS = 15_000

    fun sendMessage(message: String, conversationId: String? = null): ChatResponse {
        val connection = (URL("$BACKEND_URL/api/chat").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = REQUEST_TIMEOUT_MS
            readTimeout = REQUEST_TIMEOUT_MS
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
        }

        return try {
            val payload = JSONObject().apply {
                put("message", message)
                conversationId?.takeIf { it.isNotBlank() }?.let { put("conversation_id", it) }
            }.toString()

            connection.outputStream.use { output ->
                output.write(payload.toByteArray(Charsets.UTF_8))
            }

            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val body = stream?.use { input ->
                BufferedReader(InputStreamReader(input, Charsets.UTF_8)).use { reader ->
                    reader.readText()
                }
            }.orEmpty()

            if (responseCode !in 200..299) {
                val detail = runCatching { JSONObject(body).optString("detail") }.getOrDefault("")
                throw ChatApiException(
                    detail.ifBlank { "Indoone backend returned HTTP $responseCode" }
                )
            }

            val json = JSONObject(body)
            val reply = json.optString("reply")
            val returnedConversationId = json.optString("conversation_id")
            if (returnedConversationId.isBlank()) {
                throw ChatApiException("Indoone backend returned no conversation id")
            }
            if (reply.isBlank()) {
                throw ChatApiException("Indoone backend returned an empty reply")
            }
            ChatResponse(
                conversationId = returnedConversationId,
                reply = reply,
            )
        } finally {
            connection.disconnect()
        }
    }
}

class ChatApiException(message: String) : Exception(message)
