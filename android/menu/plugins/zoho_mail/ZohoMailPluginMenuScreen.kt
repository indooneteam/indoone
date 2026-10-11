package com.indoone.menu.plugins.zoho_mail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
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
import kotlinx.coroutines.launch

@Composable
fun ZohoMailPluginMenuScreen(
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var connected by remember { mutableStateOf(false) }
    var checking by remember { mutableStateOf(true) }
    var connecting by remember { mutableStateOf(false) }
    var scopeText by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    fun refreshStatus() {
        checking = true
        scope.launch {
            runCatching { ZohoMailPluginApi.getConnectionStatus() }
                .onSuccess { result ->
                    connected = result.connected
                    scopeText = result.scope
                    message = if (result.connected) {
                        "Zoho Mail is connected to Indoone."
                    } else {
                        "Zoho Mail is not connected yet. Connect your account to continue."
                    }
                }
                .onFailure { error ->
                    connected = false
                    message = error.message ?: "Could not check the Zoho Mail connection."
                }
            checking = false
        }
    }

    fun startAuthorization() {
        if (connecting) return
        connecting = true
        message = "Preparing secure Zoho Mail authorization..."
        scope.launch {
            runCatching {
                val authorizationUrl = ZohoMailPluginApi.createAuthorizationUrl()
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(authorizationUrl)))
            }.onSuccess {
                message = "Finish authorizing Zoho Mail in your browser, return to Indoone, then check the connection status."
            }.onFailure { error ->
                message = error.message ?: "Could not start Zoho Mail authorization."
            }
            connecting = false
        }
    }

    LaunchedEffect(Unit) {
        refreshStatus()
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
            modifier = Modifier.padding(bottom = 2.dp).clickable(onClick = onBack),
        )
        Text(
            text = "Zoho Mail",
            color = Color(0xFFE42527),
            fontSize = 24.sp,
        )
        Text(
            text = "Connect your Zoho Mail account securely through the Indoone backend.",
            color = Color(0xFF77707F),
            fontSize = 12.sp,
            lineHeight = 18.sp,
        )
        Button(
            onClick = { startAuthorization() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !connecting && !checking,
        ) {
            Text(if (connected) "RECONNECT ZOHO MAIL" else "CONNECT ZOHO MAIL")
        }
        OutlinedButton(
            onClick = { refreshStatus() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !checking && !connecting,
        ) {
            Text("CHECK CONNECTION")
        }
        if (checking || connecting) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 4.dp))
        }
        Text(
            text = when {
                connected -> "✓ Connected to Zoho Mail."
                else -> message
            },
            color = if (connected) Color(0xFF16803C) else Color(0xFF77707F),
            fontSize = 13.sp,
            lineHeight = 19.sp,
        )
        if (connected && scopeText.isNotBlank()) {
            Text(
                text = "Granted permissions: $scopeText",
                color = Color(0xFF77707F),
                fontSize = 11.sp,
                lineHeight = 16.sp,
            )
        }
    }
}
