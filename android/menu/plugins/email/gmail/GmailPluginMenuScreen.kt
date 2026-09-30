package com.indoone.menu.plugins.email.gmail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun GmailPluginMenuScreen(
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var connected by remember { mutableStateOf(false) }
    var checking by remember { mutableStateOf(true) }
    var connecting by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    fun checkStatus() {
        scope.launch {
            runCatching { GmailPluginApi.getConnectionStatus() }
                .onSuccess {
                    connected = it.connected
                    message = if (it.connected) "Gmail is connected to Indoone." else ""
                }
                .onFailure {
                    message = it.message ?: "Could not check Gmail connection."
                }
            checking = false
        }
    }

    LaunchedEffect(Unit) {
        checkStatus()
    }

    LaunchedEffect(connecting) {
        if (!connecting) return@LaunchedEffect
        repeat(90) {
            delay(2000L)
            val result = runCatching { GmailPluginApi.getConnectionStatus() }
            result.onSuccess {
                if (it.connected) {
                    connected = true
                    connecting = false
                    checking = false
                    message = "Gmail connected successfully."
                    return@LaunchedEffect
                }
            }
        }
        if (connecting) {
            connecting = false
            checking = false
            message = "Connection is still pending. Complete Google authorization and press Connect again."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "Back",
            color = Color(0xFF5E2DD2),
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 2.dp),
        )

        Text(
            text = "Gmail",
            color = Color(0xFF5E2DD2),
            fontSize = 24.sp,
        )

        Text(
            text = "Connect your Gmail account securely to Indoone.",
            color = Color(0xFF77707F),
            fontSize = 12.sp,
            lineHeight = 18.sp,
        )

        Button(
            onClick = {
                if (connected || connecting) return@Button
                connecting = true
                checking = true
                message = "Opening Google authorization..."
                scope.launch {
                    runCatching { GmailPluginApi.startConnection() }
                        .onSuccess { result ->
                            runCatching {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(result.authorizationUrl))
                                )
                            }.onFailure {
                                connecting = false
                                checking = false
                                message = it.message ?: "Could not open Google authorization."
                            }
                        }
                        .onFailure {
                            connecting = false
                            checking = false
                            message = it.message ?: "Could not start Gmail connection."
                        }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !connected && !connecting,
        ) {
            Text(if (connected) "CONNECTED" else "CONNECT GMAIL")
        }

        if (checking || connecting) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 4.dp))
        }

        Text(
            text = when {
                connected -> "✓ Gmail is connected. Indoone can now use the permitted Gmail capabilities."
                message.isNotBlank() -> message
                else -> "No Gmail account is connected yet."
            },
            color = if (connected) Color(0xFF287A46) else Color(0xFF4B4553),
            fontSize = 13.sp,
            lineHeight = 19.sp,
        )

        Text(
            text = "Google will ask you to approve the Gmail permissions. Your OAuth tokens are stored server-side in encrypted form.",
            color = Color(0xFF77707F),
            fontSize = 11.sp,
            lineHeight = 17.sp,
        )

        androidx.compose.material3.TextButton(onClick = onBack) {
            Text("Back")
        }
    }
}
