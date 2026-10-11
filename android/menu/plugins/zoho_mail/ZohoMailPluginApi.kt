package com.indoone.menu.plugins.zoho_mail

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.indoone.BuildConfig
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ZohoMailConnectionStatus(
    val connected: Boolean,
    val scope: String = "",
    val updatedAt: String? = null,
)

object ZohoMailPluginApi {
    private const val CONNECT_TIMEOUT_MS = 10_000
    private const val READ_TIMEOUT_MS = 20_000

    suspend fun createAuthorizationUrl(): String = withContext(Dispatchers.IO) {
        val backendUrl = requireBackendUrl()
        val userId = requireUserId()
        val authToken = requireAuthToken()
        val payload = JSONObject().put("user_id", userId).toString()
        val connection = openConnection(
            url = "${backendUrl}/api/integrations/zoho-mail/start",
            method = "POST",
            authToken = authToken,
        )
        try {
            val body = execute(connection, payload)
            JSONObject(body).optString("authorization_url").trim()
                .takeIf { it.startsWith("https://") }
                ?: throw ZohoMailPluginException("Zoho Mail backend returned no valid authorization URL.")
        } finally {
            connection.disconnect()
        }
    }

    suspend fun getConnectionStatus(): ZohoMailConnectionStatus = withContext(Dispatchers.IO) {
        val backendUrl = requireBackendUrl()
        val userId = requireUserId()
        val authToken = requireAuthToken()
        val encodedUserId = URLEncoder.encode(userId, "UTF-8")
        val connection = openConnection(
            url = "${backendUrl}/api/integrations/zoho-mail/connection?user_id=$encodedUserId",
            method = "GET",
            authToken = authToken,
        )
        try {
            val body = execute(connection, null)
            val json = JSONObject(body)
            ZohoMailConnectionStatus(
                connected = json.optBoolean("connected", false),
                scope = json.optString("scope", ""),
                updatedAt = json.optString("updated_at").takeIf { it.isNotBlank() },
            )
        } finally {
            connection.disconnect()
        }
    }

    private fun requireBackendUrl(): String {
        val value = BuildConfig.INDOONE_BACKEND_URL.trimEnd('/')
        if (value.isBlank()) throw ZohoMailPluginException("Indoone backend URL is not configured.")
        return value
    }

    private fun requireUserId(): String =
        FirebaseAuth.getInstance().currentUser?.uid?.takeIf { it.isNotBlank() }
            ?: throw ZohoMailPluginException("Please sign in to Indoone first.")

    private fun requireAuthToken(): String {
        val user = FirebaseAuth.getInstance().currentUser
            ?: throw ZohoMailPluginException("Please sign in to Indoone first.")
        return try {
            Tasks.await(user.getIdToken(false)).token
                ?.takeIf { it.isNotBlank() }
                ?: throw ZohoMailPluginException("Could not get your Indoone authentication token.")
        } catch (error: ZohoMailPluginException) {
            throw error
        } catch (error: Exception) {
            throw ZohoMailPluginException("Could not refresh your Indoone authentication session.", error)
        }
    }

    private fun openConnection(
        url: String,
        method: String,
        authToken: String,
    ): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            doInput = true
            doOutput = method == "POST"
            useCaches = false
            instanceFollowRedirects = true
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", "Bearer $authToken")
            if (method == "POST") {
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            }
        }

    private fun execute(connection: HttpURLConnection, payload: String?): String {
        try {
            if (payload != null) {
                connection.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
            }
            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.use { input ->
                BufferedReader(InputStreamReader(input, Charsets.UTF_8)).use { it.readText() }
            }.orEmpty()
            if (responseCode !in 200..299) {
                val message = runCatching {
                    val errorJson = JSONObject(body)
                    errorJson.optString("message").ifBlank { errorJson.optString("detail") }
                }.getOrDefault("").trim()
                throw ZohoMailPluginException(
                    message.takeIf { it.isNotBlank() }
                        ?: "Indoone backend returned HTTP $responseCode.",
                )
            }
            return body
        } catch (error: ZohoMailPluginException) {
            throw error
        } catch (error: Exception) {
            throw ZohoMailPluginException(
                "Could not reach Indoone backend. Check your internet connection.",
                error,
            )
        }
    }
}

class ZohoMailPluginException(message: String, cause: Throwable? = null) : Exception(message, cause)
