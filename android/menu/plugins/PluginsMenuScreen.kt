package com.indoone.menu.plugins

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LinkOff
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indoone.R
import com.indoone.menu.plugins.email.gmail.GmailPluginApi
import com.indoone.menu.plugins.zoho_mail.ZohoMailPluginApi
import kotlinx.coroutines.launch

private val AppPurple = Color(0xFF6C45D9)
private val Ink = Color(0xFF272238)
private val Muted = Color(0xFF777286)
private val PalePurple = Color(0xFFF4F0FF)
private val ConnectedGreen = Color(0xFF218653)

@Composable
fun PluginsMenuScreen(
    onBack: () -> Unit,
    onEmailClick: () -> Unit,
    onZohoMailClick: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var checking by remember { mutableStateOf(true) }
    var gmailConnected by remember { mutableStateOf<Boolean?>(null) }
    var gmailAddress by remember { mutableStateOf("") }
    var zohoConnected by remember { mutableStateOf<Boolean?>(null) }
    var zohoAddress by remember { mutableStateOf("") }
    var pendingDisconnect by remember { mutableStateOf<String?>(null) }
    var disconnectingProvider by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        checking = true
        runCatching { GmailPluginApi.getConnectionStatus() }
            .onSuccess { result ->
                gmailConnected = result.connected
                gmailAddress = result.emailAddress.orEmpty()
            }
            .onFailure {
                gmailConnected = null
                statusMessage = "Could not check the Email connection. Try again."
            }

        runCatching { ZohoMailPluginApi.getConnectionStatus() }
            .onSuccess { result ->
                zohoConnected = result.connected
                if (result.connected) {
                    runCatching { ZohoMailPluginApi.listAccounts().firstOrNull()?.emailAddress.orEmpty() }
                        .onSuccess { zohoAddress = it }
                        .onFailure { zohoAddress = "" }
                } else {
                    zohoAddress = ""
                }
            }
            .onFailure {
                zohoConnected = null
                if (statusMessage.isBlank()) {
                    statusMessage = "Could not check the Zoho Mail connection. Try again."
                }
            }
        checking = false
    }

    fun disconnect(provider: String) {
        if (disconnectingProvider != null) return
        scope.launch {
            disconnectingProvider = provider
            statusMessage = ""
            try {
                when (provider) {
                    "email" -> {
                        GmailPluginApi.disconnect()
                        gmailConnected = false
                        gmailAddress = ""
                    }
                    "zoho-mail" -> {
                        ZohoMailPluginApi.disconnect()
                        zohoConnected = false
                        zohoAddress = ""
                    }
                }
                statusMessage = if (provider == "email") "Email disconnected." else "Zoho Mail disconnected."
            } catch (error: Exception) {
                statusMessage = error.message
                    ?.takeIf { it.isNotBlank() }
                    ?: "Could not disconnect this account. Please try again."
            } finally {
                disconnectingProvider = null
                pendingDisconnect = null
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = AppPurple,
                    )
                }
                Text(
                    text = "Plugins",
                    color = Ink,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            MailProviderRow(
                title = "Email",
                accountAddress = gmailAddress,
                connected = gmailConnected,
                isZoho = false,
                busy = checking || disconnectingProvider == "email",
                onAdd = onEmailClick,
                onReauthorize = onEmailClick,
                onDisconnect = { pendingDisconnect = "email" },
            )

            MailProviderRow(
                title = "Zoho Mail",
                accountAddress = zohoAddress,
                connected = zohoConnected,
                isZoho = true,
                busy = checking || disconnectingProvider == "zoho-mail",
                onAdd = onZohoMailClick,
                onReauthorize = onZohoMailClick,
                onDisconnect = { pendingDisconnect = "zoho-mail" },
            )

            if (checking) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = AppPurple,
                        strokeWidth = 2.dp,
                    )
                    Text(
                        text = "Checking connections…",
                        modifier = Modifier.padding(start = 8.dp),
                        color = Muted,
                        fontSize = 12.sp,
                    )
                }
            }

            if (statusMessage.isNotBlank()) {
                Text(
                    text = statusMessage,
                    color = if (statusMessage.endsWith("disconnected.")) ConnectedGreen else Muted,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                )
            }

            Spacer(modifier = Modifier.size(4.dp))
        }
    }

    if (pendingDisconnect != null) {
        val disconnectTitle = if (pendingDisconnect == "email") "Disconnect Email?" else "Disconnect Zoho Mail?"
        AlertDialog(
            onDismissRequest = {
                if (disconnectingProvider == null) pendingDisconnect = null
            },
            title = { Text(disconnectTitle, color = Ink, fontWeight = FontWeight.SemiBold) },
            text = {
                Text(
                    "Indoone AI will no longer be able to use this email account until you connect it again.",
                    color = Muted,
                )
            },
            confirmButton = {
                TextButton(
                    enabled = disconnectingProvider == null,
                    onClick = { disconnect(pendingDisconnect.orEmpty()) },
                ) {
                    Text(
                        if (disconnectingProvider != null) "Disconnecting…" else "Disconnect",
                        color = AppPurple,
                    )
                }
            },
            dismissButton = {
                TextButton(
                    enabled = disconnectingProvider == null,
                    onClick = { pendingDisconnect = null },
                ) {
                    Text("Cancel", color = Muted)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(22.dp),
        )
    }
}

@Composable
private fun MailProviderRow(
    title: String,
    accountAddress: String,
    connected: Boolean?,
    isZoho: Boolean,
    busy: Boolean,
    onAdd: () -> Unit,
    onReauthorize: () -> Unit,
    onDisconnect: () -> Unit,
) {
    var menuExpanded by remember(title) { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Color(0xFFECE7F8)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(15.dp),
                color = PalePurple,
            ) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isZoho) {
                        Image(
                            painter = painterResource(R.drawable.zoho_mail_logo),
                            contentDescription = "Zoho Mail logo",
                            modifier = Modifier.size(40.dp),
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = "Email",
                            tint = AppPurple,
                            modifier = Modifier.size(29.dp),
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f).padding(start = 12.dp, end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = title,
                    color = Ink,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                when (connected) {
                    true -> {
                        Text(
                            text = accountAddress.ifBlank { if (isZoho) "Zoho Mail account" else "Gmail account" },
                            color = Muted,
                            fontSize = 12.sp,
                            maxLines = 1,
                        )
                        Text(
                            text = "● Connected",
                            color = ConnectedGreen,
                            fontSize = 11.sp,
                        )
                    }
                    false -> Text(
                        text = if (isZoho) "Connect your Zoho Mail account" else "Connect your Gmail account",
                        color = Muted,
                        fontSize = 12.sp,
                    )
                    null -> Text(
                        text = "Connection status unavailable",
                        color = Muted,
                        fontSize = 11.sp,
                    )
                }
            }

            if (connected == true) {
                androidx.compose.foundation.layout.Box {
                    IconButton(
                        enabled = !busy,
                        onClick = { menuExpanded = true },
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = "More options for $title",
                            tint = AppPurple,
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        containerColor = Color.White,
                    ) {
                        DropdownMenuItem(
                            text = { Text("Reauthorize", color = Ink) },
                            leadingIcon = {
                                Icon(Icons.Outlined.Refresh, contentDescription = null, tint = AppPurple)
                            },
                            onClick = {
                                menuExpanded = false
                                onReauthorize()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Disconnect", color = Ink) },
                            leadingIcon = {
                                Icon(Icons.Outlined.LinkOff, contentDescription = null, tint = AppPurple)
                            },
                            onClick = {
                                menuExpanded = false
                                onDisconnect()
                            },
                        )
                    }
                }
            } else {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = CircleShape,
                    color = if (connected != true && !busy) AppPurple else Color(0xFFEAE5F7),
                    onClick = { if (connected != true && !busy) onAdd() },
                ) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (busy) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(17.dp),
                                color = AppPurple,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = "Connect $title",
                                tint = Color.White,
                                modifier = Modifier.size(25.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
