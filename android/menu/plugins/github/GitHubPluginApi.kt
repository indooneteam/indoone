package com.indoone.menu.plugins.github

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.indoone.BuildConfig
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class GitHubConnectResult(val authorizationUrl: String)
data class GitHubConnectionStatus(
    val connected: Boolean,
    val login: String = "",
    val providerHealthy: Boolean = false,
)
data class GitHubRepository(
    val fullName: String,
    val isPrivate: Boolean,
    val defaultBranch: String,
    val htmlUrl: String,
)

object GitHubPluginApi {
    private const val CONNECT_TIMEOUT_MS = 10_000
    private const val READ_TIMEOUT_MS = 20_000
    private const val CALLBACK_PATH = "/api/integrations/github/callback"

    suspend fun startConnection(): GitHubConnectResult = withContext(Dispatchers.IO) {
        val backendUrl = requireBackendUrl()
        val payload = JSONObject().apply {
            put("user_id", requireUserId())
            put("redirect_uri", backendUrl + CALLBACK_PATH)
        }
        val result = request(
            "POST",
            "${backendUrl}/api/integrations/github/connect",
            payload,
        )
        val authorizationUrl = result.optString("authorization_url").trim()
        if (authorizationUrl.isBlank()) {
            throw GitHubPluginException("Indoone backend returned no GitHub authorization URL.")
        }
        GitHubConnectResult(authorizationUrl)
    }

    suspend fun getConnectionStatus(): GitHubConnectionStatus = withContext(Dispatchers.IO) {
        val backendUrl = requireBackendUrl()
        val userId = requireUserId()
        val status = request(
            "GET",
            "${backendUrl}/api/integrations/github/status?user_id=${URLEncoder.encode(userId, "UTF-8")}",
        )
        val connected = status.optBoolean("connected", false)
        if (!connected) return@withContext GitHubConnectionStatus(connected = false)

        val probe = runCatching {
            request(
                "POST",
                "${backendUrl}/api/integrations/github/probe",
                JSONObject().put("user_id", userId),
            )
        }.getOrNull()
        GitHubConnectionStatus(
            connected = true,
            login = probe?.optJSONObject("summary")?.optString("login").orEmpty(),
            providerHealthy = probe?.optBoolean("provider_ok", false) == true,
        )
    }

    suspend fun listRepositories(page: Int = 1, perPage: Int = 10): List<GitHubRepository> =
        withContext(Dispatchers.IO) {
            val backendUrl = requireBackendUrl()
            val result = request(
                "POST",
                "${backendUrl}/api/integrations/github/repositories",
                JSONObject().apply {
                    put("user_id", requireUserId())
                    put("page", page)
                    put("per_page", perPage.coerceIn(1, 100))
                },
            )
            val items: JSONArray = result.optJSONArray("repositories") ?: JSONArray()
            buildList {
                for (index in 0 until items.length()) {
                    val item = items.optJSONObject(index) ?: continue
                    add(
                        GitHubRepository(
                            fullName = item.optString("full_name").trim(),
                            isPrivate = item.optBoolean("private", false),
                            defaultBranch = item.optString("default_branch").trim(),
                            htmlUrl = item.optString("html_url").trim(),
                        ),
                    )
                }
            }
        }

    private fun requireBackendUrl(): String =
        BuildConfig.INDOONE_BACKEND_URL.trimEnd('/').takeIf { it.isNotBlank() }
            ?: throw GitHubPluginException("Indoone backend URL is not configured.")

    private fun requireUserId(): String =
        FirebaseAuth.getInstance().currentUser?.uid?.takeIf { it.isNotBlank() }
            ?: throw GitHubPluginException("Please sign in to Indoone first.")

    private fun requireAuthToken(): String {
        val user = FirebaseAuth.getInstance().currentUser
            ?: throw GitHubPluginException("Please sign in to Indoone first.")
        return try {
            Tasks.await(user.getIdToken(false)).token
                ?.takeIf { it.isNotBlank() }
                ?: throw GitHubPluginException("Could not get your Indoone authentication token.")
        } catch (error: GitHubPluginException) {
            throw error
        } catch (error: Exception) {
            throw GitHubPluginException("Could not refresh your Indoone authentication session.", error)
        }
    }

    private fun request(method: String, url: String, payload: JSONObject? = null): JSONObject {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            doInput = true
            doOutput = payload != null
            useCaches = false
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", "Bearer ${requireAuthToken()}")
            if (payload != null) setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        }
        try {
            if (payload != null) {
                connection.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }
            }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.use { input ->
                BufferedReader(InputStreamReader(input, Charsets.UTF_8)).use { it.readText() }
            }.orEmpty()
            if (code !in 200..299) {
                val message = runCatching {
                    JSONObject(body).optString("detail").ifBlank { JSONObject(body).optString("message") }
                }.getOrDefault("").trim()
                throw GitHubPluginException(
                    message.takeIf { it.isNotBlank() } ?: "Indoone backend returned HTTP $code.",
                )
            }
            return if (body.isBlank()) JSONObject() else JSONObject(body)
        } catch (error: GitHubPluginException) {
            throw error
        } catch (error: Exception) {
            throw GitHubPluginException("Could not reach the Indoone backend. Check your connection.", error)
        } finally {
            connection.disconnect()
        }
    }
}

class GitHubPluginException(message: String, cause: Throwable? = null) : Exception(message, cause)
