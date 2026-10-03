package com.indoone.notifications

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.indoone.BuildConfig
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object NotificationApi {
    fun registerToken(token: String): Boolean {
        val normalized = token.trim()
        if (normalized.isBlank()) return false

        val user = FirebaseAuth.getInstance().currentUser ?: return false
        val authToken = Tasks.await(user.getIdToken(false)).token?.takeIf { it.isNotBlank() }
            ?: return false

        val backendUrl = BuildConfig.INDOONE_BACKEND_URL.trimEnd('/')
        if (backendUrl.isBlank()) return false

        val connection = (URL("$backendUrl/api/notifications/register").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 10_000
            readTimeout = 15_000
            doOutput = true
            doInput = true
            useCaches = false
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", "Bearer $authToken")
        }

        return try {
            val payload = JSONObject().apply {
                put("token", normalized)
            }.toString()
            connection.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
            val status = connection.responseCode
            if (status !in 200..299) {
                runCatching {
                    connection.errorStream?.use { stream ->
                        BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).readText()
                    }
                }
                return false
            }
            true
        } finally {
            connection.disconnect()
        }
    }
}
