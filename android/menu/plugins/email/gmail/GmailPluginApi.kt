package com.indoone.menu.plugins.email.gmail

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.indoone.BuildConfig
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class GmailConnectionStatus(
    val connected: Boolean,
    val emailAddress: String? = null,
    val profileSaved: Boolean = false,
)

data class GmailProfileConnection(
    val connected: Boolean,
    val emailAddress: String,
    val scope: String,
)

object GmailPluginApi {
    private const val CONNECT_TIMEOUT_MS = 10_000
    private const val READ_TIMEOUT_MS = 20_000
    const val GMAIL_SCOPE = "https://www.googleapis.com/auth/gmail.modify"

    suspend fun getGoogleServerClientId(): String = withContext(Dispatchers.IO) {
        val backendUrl = requireBackendUrl()
        val authToken = requireAuthToken()
        val connection = openConnection(
            url = "${backendUrl}/api/integrations/gmail/auth-config",
            method = "GET",
            authToken = authToken,
        )
        try {
            val body = execute(connection, null)
            JSONObject(body).optString("server_client_id").trim()
                .takeIf { it.isNotBlank() }
                ?: throw GmailPluginException("Google server OAuth client is not configured on Indoone backend.")
        } finally {
            connection.disconnect()
        }
    }

    suspend fun storeServerAuthCode(
        serverAuthCode: String,
        grantedScope: String = GMAIL_SCOPE,
    ): GmailConnectionStatus = withContext(Dispatchers.IO) {
        val backendUrl = requireBackendUrl()
        val userId = requireUserId()
        val authToken = requireAuthToken()
        val code = serverAuthCode.trim()
        if (code.isBlank()) {
            throw GmailPluginException("Google did not return a server authorization code.")
        }
        val payload = JSONObject().apply {
            put("user_id", userId)
            put("server_auth_code", code)
            put("scope", grantedScope.ifBlank { GMAIL_SCOPE })
        }.toString()

        val connection = openConnection(
            url = "${backendUrl}/api/integrations/gmail/server-auth-code",
            method = "POST",
            authToken = authToken,
        )
        try {
            val body = execute(connection, payload)
            val json = JSONObject(body)
            val isConnected = json.optBoolean("connected", false)
            val emailAddress = json.optJSONObject("summary")
                ?.optString("email_address")
                ?.trim()
                .orEmpty()
            if (isConnected) {
                saveProfileConnection(
                    emailAddress = emailAddress,
                    scope = json.optString("scope").trim().ifBlank {
                        grantedScope.ifBlank { GMAIL_SCOPE }
                    },
                )
            }
            GmailConnectionStatus(
                connected = isConnected,
                emailAddress = emailAddress.takeIf { it.isNotBlank() },
                profileSaved = isConnected,
            )
        } finally {
            connection.disconnect()
        }
    }

    suspend fun storeNativeAccessToken(
        accessToken: String,
        grantedScope: String = GMAIL_SCOPE,
    ): GmailConnectionStatus = withContext(Dispatchers.IO) {
        val backendUrl = requireBackendUrl()
        val userId = requireUserId()
        val authToken = requireAuthToken()
        val token = accessToken.trim()
        if (token.isBlank()) {
            throw GmailPluginException("Google did not return an access token.")
        }
        val payload = JSONObject().apply {
            put("user_id", userId)
            put("access_token", token)
            put("scope", grantedScope.ifBlank { GMAIL_SCOPE })
        }.toString()

        val connection = openConnection(
            url = "${backendUrl}/api/integrations/gmail/native-token",
            method = "POST",
            authToken = authToken,
        )
        try {
            val body = execute(connection, payload)
            val json = JSONObject(body)
            val isConnected = json.optBoolean("connected", false)
            val emailAddress = json.optJSONObject("summary")
                ?.optString("email_address")
                ?.trim()
                .orEmpty()
            if (isConnected) {
                saveProfileConnection(
                    emailAddress = emailAddress,
                    scope = grantedScope.ifBlank { GMAIL_SCOPE },
                )
            }
            GmailConnectionStatus(
                connected = isConnected,
                emailAddress = emailAddress.takeIf { it.isNotBlank() },
                profileSaved = isConnected,
            )
        } finally {
            connection.disconnect()
        }
    }

    suspend fun getConnectionStatus(): GmailConnectionStatus = withContext(Dispatchers.IO) {
        val backendUrl = requireBackendUrl()
        val userId = requireUserId()
        val authToken = requireAuthToken()
        val savedConnection = readProfileConnection()
        val url = "${backendUrl}/api/integrations/gmail/status?user_id=${java.net.URLEncoder.encode(userId, "UTF-8")}"
        val connection = openConnection(url, "GET", authToken)
        try {
            val body = execute(connection, null)
            val json = JSONObject(body)
            val isConnected = json.optBoolean("connected", false)
            val backendScope = json.optJSONObject("metadata")
                ?.optString("scope")
                ?.trim()
                .orEmpty()
            val emailAddress = savedConnection?.emailAddress.orEmpty()
            if (isConnected) {
                saveProfileConnection(
                    emailAddress = emailAddress,
                    scope = backendScope.ifBlank { savedConnection?.scope.orEmpty().ifBlank { GMAIL_SCOPE } },
                )
            }
            GmailConnectionStatus(
                connected = isConnected,
                emailAddress = emailAddress.takeIf { it.isNotBlank() },
                profileSaved = isConnected || savedConnection?.connected == true,
            )
        } finally {
            connection.disconnect()
        }
    }

    suspend fun readProfileConnection(): GmailProfileConnection? = withContext(Dispatchers.IO) {
        val reference = gmailConnectionReference(requireUserId())
        val snapshot = Tasks.await(reference.get())
        if (!snapshot.exists()) {
            null
        } else {
            GmailProfileConnection(
                connected = snapshot.child("connected").getValue(Boolean::class.javaObjectType) ?: false,
                emailAddress = snapshot.child("emailAddress").getValue(String::class.java).orEmpty(),
                scope = snapshot.child("scope").getValue(String::class.java).orEmpty(),
            )
        }
    }

    private suspend fun saveProfileConnection(emailAddress: String, scope: String) = withContext(Dispatchers.IO) {
        val userId = requireUserId()
        val metadata = mapOf(
            "provider" to "gmail",
            "providerName" to "Gmail",
            "connected" to true,
            "emailAddress" to emailAddress.trim(),
            "scope" to scope.trim().ifBlank { GMAIL_SCOPE },
            "updatedAt" to ServerValue.TIMESTAMP,
            "lastVerifiedAt" to ServerValue.TIMESTAMP,
        )
        Tasks.await(gmailConnectionReference(userId).setValue(metadata))
    }

    private fun gmailConnectionReference(userId: String) =
        FirebaseDatabase.getInstance().reference
            .child("users")
            .child(userId)
            .child("profile")
            .child("connections")
            .child("plugins")
            .child("email")
            .child("gmail")

    private fun requireBackendUrl(): String {
        val value = BuildConfig.INDOONE_BACKEND_URL.trimEnd('/')
        if (value.isBlank()) throw GmailPluginException("Indoone backend URL is not configured.")
        return value
    }

    private fun requireUserId(): String =
        FirebaseAuth.getInstance().currentUser?.uid?.takeIf { it.isNotBlank() }
            ?: throw GmailPluginException("Please sign in to Indoone first.")

    private fun requireAuthToken(): String {
        val user = FirebaseAuth.getInstance().currentUser
            ?: throw GmailPluginException("Please sign in to Indoone first.")
        return try {
            Tasks.await(user.getIdToken(false)).token
                ?.takeIf { it.isNotBlank() }
                ?: throw GmailPluginException("Could not get your Indoone authentication token.")
        } catch (error: GmailPluginException) {
            throw error
        } catch (error: Exception) {
            throw GmailPluginException("Could not refresh your Indoone authentication session.", error)
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
                throw GmailPluginException(
                    message.takeIf { it.isNotBlank() }
                        ?: "Indoone backend returned HTTP $responseCode.",
                )
            }
            return body
        } catch (error: GmailPluginException) {
            throw error
        } catch (error: Exception) {
            throw GmailPluginException(
                "Could not reach Indoone backend. Check your internet connection.",
                error,
            )
        }
    }
}

class GmailPluginException(message: String, cause: Throwable? = null) : Exception(message, cause)
