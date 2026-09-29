package com.indoone.home

import android.util.Base64
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.indoone.BuildConfig
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.io.IOException
import java.net.URL
import java.net.SocketTimeoutException
import java.net.UnknownHostException


data class ChatResponse(
    val conversationId: String,
    val reply: String,
)

data class FileUploadResponse(
    val fileId: String,
    val filename: String,
    val bytes: Int,
    val textPreview: String,
)

object ChatApi {
    private const val CONNECT_TIMEOUT_MS = 10_000
    private const val READ_TIMEOUT_MS = 120_000
    private const val WARMUP_TIMEOUT_MS = 15_000
    private const val WARMUP_RETRIES = 2
    private const val WARMUP_RETRY_DELAY_MS = 1_500L
    private const val CHAT_TRANSIENT_RETRIES = 2
    private const val CHAT_RETRY_DELAY_MS = 1_500L
    private const val MAX_UPLOAD_BYTES = 2_000_000

    fun sendMessage(message: String, conversationId: String? = null, fileId: String? = null): ChatResponse {
        val backendUrl = requireBackendUrl()
        val authToken = requireAuthToken()

        // Render free instances can sleep between requests. Wake the backend
        // through its lightweight health endpoint before opening the chat POST,
        // so a cold-start gateway response does not become a user-visible 502.
        warmUpBackend(backendUrl)

        val payload = JSONObject().apply {
            put("message", message)
            conversationId?.takeIf { it.isNotBlank() }?.let { put("conversation_id", it) }
            fileId?.takeIf { it.isNotBlank() }?.let { put("file_id", it) }
        }.toString()

        var lastTransientError: ChatApiException? = null
        repeat(CHAT_TRANSIENT_RETRIES + 1) { attempt ->
            try {
                val connection = openConnection("$backendUrl/api/chat", authToken)
                return try {
                    val body = executeJson(connection, payload)
                    val json = JSONObject(body)
                    val reply = json.optString("reply")
                    val returnedConversationId = json.optString("conversation_id")
                    if (returnedConversationId.isBlank()) {
                        throw ChatApiException("Indoone backend returned no conversation id")
                    }
                    if (reply.isBlank()) {
                        throw ChatApiException("Indoone backend returned an empty reply")
                    }
                    ChatResponse(returnedConversationId, reply)
                } finally {
                    connection.disconnect()
                }
            } catch (error: ChatApiException) {
                val isTransient = error.statusCode == 429 || error.statusCode == 503
                if (!isTransient || attempt >= CHAT_TRANSIENT_RETRIES) {
                    throw error
                }
                lastTransientError = error
                try {
                    Thread.sleep(CHAT_RETRY_DELAY_MS * (attempt + 1L))
                } catch (_: InterruptedException) {
                    Thread.currentThread().interrupt()
                    throw error
                }
                warmUpBackend(backendUrl)
            }
        }

        throw lastTransientError ?: ChatApiException("Indoone backend request failed")
    }

    fun uploadTextFile(filename: String, bytes: ByteArray): FileUploadResponse {
        val safeFilename = filename.trim()
        if (safeFilename.isBlank()) throw ChatApiException("Please choose a file with a valid name.")
        if (bytes.isEmpty()) throw ChatApiException("The selected file is empty.")
        if (bytes.size > MAX_UPLOAD_BYTES) throw ChatApiException("File is too large. Maximum size is 2 MB.")
        val backendUrl = requireBackendUrl()
        val connection = openConnection("$backendUrl/api/files", requireAuthToken())
        return try {
            val payload = JSONObject().apply {
                put("filename", safeFilename)
                put("content_base64", Base64.encodeToString(bytes, Base64.NO_WRAP))
            }.toString()
            val body = executeJson(connection, payload)
            val json = JSONObject(body)
            val response = FileUploadResponse(
                fileId = json.optString("file_id"),
                filename = json.optString("filename"),
                bytes = json.optInt("bytes"),
                textPreview = json.optString("text_preview"),
            )
            if (response.fileId.isBlank()) throw ChatApiException("Indoone backend returned no file id")
            response
        } finally {
            connection.disconnect()
        }
    }

    private fun requireBackendUrl(): String {
        val backendUrl = BuildConfig.INDOONE_BACKEND_URL.trimEnd('/')
        if (backendUrl.isBlank()) throw ChatApiException("Indoone backend URL is not configured")
        return backendUrl
    }

    private fun requireAuthToken(): String {
        val user = FirebaseAuth.getInstance().currentUser
            ?: throw ChatApiException("Please sign in to use Indoone AI.")
        return try {
            Tasks.await(user.getIdToken(false)).token
                ?.takeIf { it.isNotBlank() }
                ?: throw ChatApiException("Could not get the Indoone authentication token.")
        } catch (error: ChatApiException) {
            throw error
        } catch (error: Exception) {
            throw ChatApiException("Could not refresh your Indoone authentication session. Please login again.", error)
        }
    }

    private fun warmUpBackend(backendUrl: String) {
        repeat(WARMUP_RETRIES) { attempt ->
            val connection = try {
                (URL("$backendUrl/health").openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = WARMUP_TIMEOUT_MS
                    readTimeout = WARMUP_TIMEOUT_MS
                    doInput = true
                    useCaches = false
                    instanceFollowRedirects = true
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("Cache-Control", "no-store")
                }
            } catch (_: IOException) {
                null
            }

            if (connection != null) {
                try {
                    if (connection.responseCode in 200..299) {
                        return
                    }
                } catch (_: IOException) {
                    // Keep the warm-up best-effort; the chat request below
                    // still reports the real backend error to the user.
                } finally {
                    connection.disconnect()
                }
            }

            if (attempt + 1 < WARMUP_RETRIES) {
                try {
                    Thread.sleep(WARMUP_RETRY_DELAY_MS)
                } catch (_: InterruptedException) {
                    Thread.currentThread().interrupt()
                    return
                }
            }
        }
    }

    private fun openConnection(url: String, authToken: String): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            doOutput = true
            doInput = true
            useCaches = false
            instanceFollowRedirects = true
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", "Bearer $authToken")
        }

    private fun executeJson(connection: HttpURLConnection, payload: String): String {
        try {
            connection.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.use { input ->
                BufferedReader(InputStreamReader(input, Charsets.UTF_8)).use { it.readText() }
            }.orEmpty()
            if (responseCode !in 200..299) {
                val errorMessage = runCatching {
                    val errorJson = JSONObject(body)
                    errorJson.optString("message").ifBlank {
                        errorJson.optString("detail")
                    }
                }.getOrDefault("").trim()

                val safeServerMessage = errorMessage
                    .takeIf { it.isNotBlank() && !it.contains("%02") && !it.contains("%0d") && !it.contains("%0D") }

                throw ChatApiException(
                    safeServerMessage?.let { "Indoone backend returned HTTP $responseCode: $it" }
                        ?: "Indoone backend returned HTTP $responseCode",
                    statusCode = responseCode,
                )
            }
            return body
        } catch (error: SocketTimeoutException) {
            throw ChatApiException("Indoone AI is taking too long to respond. Please try again.", error)
        } catch (error: UnknownHostException) {
            throw ChatApiException("Indoone AI backend could not be reached. Check your internet connection.", error)
        } catch (error: IOException) {
            throw ChatApiException(
                "Indoone AI network error (${error.javaClass.simpleName}). Please check your internet connection and try again.",
                error,
            )
        }
    }
}

class ChatApiException(
    message: String,
    cause: Throwable? = null,
    val statusCode: Int? = null,
) : Exception(message, cause)
