package com.indoone.home

import com.indoone.BuildConfig
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.SocketTimeoutException
import java.net.UnknownHostException


data class ChatResponse(
    val conversationId: String,
    val reply: String,
)

object ChatApi {
    private const val CONNECT_TIMEOUT_MS = 10_000
    private const val READ_TIMEOUT_MS = 120_000

    fun sendMessage(message: String, conversationId: String? = null): ChatResponse {
        val backendUrl = BuildConfig.INDOONE_BACKEND_URL.trimEnd('/')
        if (backendUrl.isBlank()) {
            throw ChatApiException("Indoone backend URL is not configured")
        }

        val connection = (URL("$backendUrl/api/chat").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            doOutput = true
            doInput = true
            useCaches = false
            instanceFollowRedirects = true
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
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

            val json = runCatching { JSONObject(body) }
                .getOrElse { throw ChatApiException("Indoone backend returned invalid JSON") }
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
        } catch (error: SocketTimeoutException) {
            throw ChatApiException("Indoone AI is taking too long to respond. Please try again.", error)
        } catch (error: UnknownHostException) {
            throw ChatApiException("Indoone AI backend could not be reached. Check your internet connection.", error)
        } finally {
            connection.disconnect()
        }
    }
}

class ChatApiException(message: String, cause: Throwable? = null) : Exception(message, cause)
