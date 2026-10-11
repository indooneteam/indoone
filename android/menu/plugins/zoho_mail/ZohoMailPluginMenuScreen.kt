package com.indoone.menu.plugins.zoho_mail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
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
    var loading by remember { mutableStateOf(false) }
    var connecting by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("Checking your Zoho Mail connection...") }
    var scopeText by remember { mutableStateOf("") }

    var accounts by remember { mutableStateOf<List<ZohoMailAccount>>(emptyList()) }
    var selectedAccount by remember { mutableStateOf<ZohoMailAccount?>(null) }
    var folders by remember { mutableStateOf<List<ZohoMailFolder>>(emptyList()) }
    var selectedFolder by remember { mutableStateOf<ZohoMailFolder?>(null) }
    var messages by remember { mutableStateOf<List<ZohoMailMessage>>(emptyList()) }
    var selectedMessage by remember { mutableStateOf<ZohoMailMessage?>(null) }
    var messageContent by remember { mutableStateOf<ZohoMailMessageContent?>(null) }
    var searchKey by remember { mutableStateOf("") }

    var composeExpanded by remember { mutableStateOf(false) }
    var recipient by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var replyMessageId by remember { mutableStateOf<String?>(null) }

    suspend fun loadMailbox(account: ZohoMailAccount) {
        accounts = accounts.ifEmpty { listOf(account) }
        val loadedFolders = ZohoMailPluginApi.listFolders(account.id)
        folders = loadedFolders
        val preferredFolder = loadedFolders.firstOrNull { it.name.equals("Inbox", ignoreCase = true) }
            ?: loadedFolders.firstOrNull()
        selectedFolder = preferredFolder
        selectedMessage = null
        messageContent = null
        messages = if (preferredFolder != null) {
            ZohoMailPluginApi.listMessages(account.id, preferredFolder.id)
        } else {
            emptyList()
        }
    }

    suspend fun refreshConnectionAndMailbox() {
        loading = true
        try {
            val result = ZohoMailPluginApi.getConnectionStatus()
            connected = result.connected
            scopeText = result.scope
            if (!result.connected) {
                statusMessage = "Zoho Mail is not connected yet. Connect your account to continue."
                accounts = emptyList()
                selectedAccount = null
                folders = emptyList()
                selectedFolder = null
                messages = emptyList()
                selectedMessage = null
                messageContent = null
                return
            }

            statusMessage = "Zoho Mail connected. Loading mailbox..."
            val loadedAccounts = ZohoMailPluginApi.listAccounts()
            accounts = loadedAccounts
            val preferredAccount = loadedAccounts.firstOrNull {
                selectedAccount?.id == it.id
            } ?: loadedAccounts.firstOrNull()
            selectedAccount = preferredAccount
            if (preferredAccount == null) {
                folders = emptyList()
                selectedFolder = null
                messages = emptyList()
                statusMessage = "Zoho Mail is connected, but no mail accounts were returned."
            } else {
                loadMailbox(preferredAccount)
                statusMessage = "Connected to Zoho Mail as ${preferredAccount.emailAddress}."
            }
        } catch (error: Exception) {
            statusMessage = error.message ?: "Could not check the Zoho Mail connection."
        } finally {
            loading = false
        }
    }

    fun checkConnection() {
        if (loading || connecting) return
        scope.launch { refreshConnectionAndMailbox() }
    }

    fun startAuthorization() {
        if (loading || connecting) return
        connecting = true
        statusMessage = "Preparing secure Zoho Mail authorization..."
        scope.launch {
            try {
                val authorizationUrl = ZohoMailPluginApi.createAuthorizationUrl()
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(authorizationUrl)))
                statusMessage = "Finish authorization in your browser. Return here and tap CHECK CONNECTION."
            } catch (error: Exception) {
                statusMessage = error.message ?: "Could not start Zoho Mail authorization."
            } finally {
                connecting = false
            }
        }
    }

    fun selectAccount(account: ZohoMailAccount) {
        if (loading) return
        scope.launch {
            loading = true
            try {
                selectedAccount = account
                searchKey = ""
                loadMailbox(account)
                statusMessage = "Loaded mailbox for ${account.emailAddress}."
            } catch (error: Exception) {
                statusMessage = error.message ?: "Could not load this Zoho Mail account."
            } finally {
                loading = false
            }
        }
    }

    fun selectFolder(folder: ZohoMailFolder) {
        val account = selectedAccount ?: return
        if (loading) return
        scope.launch {
            loading = true
            try {
                selectedFolder = folder
                selectedMessage = null
                messageContent = null
                searchKey = ""
                messages = ZohoMailPluginApi.listMessages(account.id, folder.id)
                statusMessage = "Loaded ${folder.name}."
            } catch (error: Exception) {
                statusMessage = error.message ?: "Could not load this folder."
            } finally {
                loading = false
            }
        }
    }

    fun searchMessages() {
        val account = selectedAccount ?: return
        val folder = selectedFolder ?: return
        if (loading) return
        scope.launch {
            loading = true
            try {
                selectedMessage = null
                messageContent = null
                messages = ZohoMailPluginApi.listMessages(
                    accountId = account.id,
                    folderId = folder.id,
                    searchKey = searchKey,
                )
                statusMessage = if (searchKey.isBlank()) {
                    "Loaded messages from ${folder.name}."
                } else {
                    "Search completed."
                }
            } catch (error: Exception) {
                statusMessage = error.message ?: "Could not search Zoho Mail."
            } finally {
                loading = false
            }
        }
    }

    fun openMessage(item: ZohoMailMessage) {
        val account = selectedAccount ?: return
        val folderId = item.folderId.ifBlank { selectedFolder?.id.orEmpty() }
        if (loading || folderId.isBlank()) return
        scope.launch {
            loading = true
            try {
                selectedMessage = item
                messageContent = ZohoMailPluginApi.readMessage(account.id, folderId, item.id)
                statusMessage = "Opened email."
            } catch (error: Exception) {
                messageContent = null
                statusMessage = error.message ?: "Could not open this email."
            } finally {
                loading = false
            }
        }
    }

    fun sendEmail() {
        val account = selectedAccount ?: return
        if (loading) return
        if (recipient.isBlank() || subject.isBlank() || body.isBlank()) {
            statusMessage = "Enter the recipient, subject, and message before sending."
            return
        }
        scope.launch {
            loading = true
            try {
                val originalMessageId = replyMessageId
                if (!originalMessageId.isNullOrBlank()) {
                    ZohoMailPluginApi.replyToMessage(
                        accountId = account.id,
                        messageId = originalMessageId,
                        fromAddress = account.emailAddress,
                        toAddress = recipient,
                        subject = subject,
                        content = body,
                    )
                } else {
                    ZohoMailPluginApi.sendMessage(
                        accountId = account.id,
                        fromAddress = account.emailAddress,
                        toAddress = recipient,
                        subject = subject,
                        content = body,
                    )
                }
                recipient = ""
                subject = ""
                body = ""
                replyMessageId = null
                composeExpanded = false
                statusMessage = if (originalMessageId.isNullOrBlank()) {
                    "Email sent through Zoho Mail."
                } else {
                    "Reply sent through Zoho Mail."
                }
                selectedFolder?.let { folder ->
                    messages = ZohoMailPluginApi.listMessages(account.id, folder.id, searchKey = "")
                    searchKey = ""
                }
            } catch (error: Exception) {
                statusMessage = error.message ?: "Could not send the email."
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshConnectionAndMailbox()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Back",
            color = Color(0xFF5E2DD2),
            fontSize = 14.sp,
            modifier = Modifier.clickable(onClick = onBack),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Zoho Mail",
                    color = Color(0xFFE42527),
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Secure mailbox connection for Indoone",
                    color = Color(0xFF77707F),
                    fontSize = 12.sp,
                )
            }
            if (loading || connecting) {
                CircularProgressIndicator(modifier = Modifier.padding(start = 10.dp))
            }
        }

        Text(
            text = statusMessage,
            color = if (connected) Color(0xFF16803C) else Color(0xFF77707F),
            fontSize = 13.sp,
            lineHeight = 18.sp,
        )

        if (!connected) {
            Button(
                onClick = { startAuthorization() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading && !connecting,
            ) {
                Text("CONNECT ZOHO MAIL")
            }
        }
        OutlinedButton(
            onClick = { checkConnection() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading && !connecting,
        ) {
            Text(if (connected) "REFRESH MAILBOX" else "CHECK CONNECTION")
        }

        if (connected && accounts.isNotEmpty()) {
            SectionTitle("Accounts")
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                accounts.forEach { account ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedAccount?.id == account.id) Color(0xFFFFE8E8) else Color(0xFFF5F5F5),
                        modifier = Modifier.clickable { selectAccount(account) },
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                            Text(
                                account.displayName,
                                color = Color(0xFF2D2732),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                account.emailAddress,
                                color = Color(0xFF77717F),
                                fontSize = 11.sp,
                            )
                        }
                    }
                }
            }

            if (folders.isNotEmpty()) {
                SectionTitle("Folders")
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    folders.forEach { folder ->
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = if (selectedFolder?.id == folder.id) Color(0xFFECE5FF) else Color(0xFFF5F5F5),
                            modifier = Modifier.clickable { selectFolder(folder) },
                        ) {
                            Text(
                                text = folder.name,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                color = Color(0xFF332A41),
                                fontSize = 12.sp,
                            )
                        }
                    }
                }
            }

            SectionTitle("Inbox and search")
            OutlinedTextField(
                value = searchKey,
                onValueChange = { searchKey = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Search emails") },
                placeholder = { Text("Search Zoho Mail") },
            )
            Button(
                onClick = { searchMessages() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading && selectedAccount != null && selectedFolder != null,
            ) {
                Text("SEARCH / LOAD MESSAGES")
            }

            if (messages.isEmpty()) {
                Text(
                    text = "No email messages to show. Refresh or search this mailbox.",
                    color = Color(0xFF77707F),
                    fontSize = 12.sp,
                )
            } else {
                messages.take(50).forEach { item ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { openMessage(item) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (selectedMessage?.id == item.id) Color(0xFFFFF1F1) else Color(0xFFF8F8FA),
                    ) {
                        Column(
                            modifier = Modifier.padding(13.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = item.subject,
                                color = Color(0xFF27212E),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                            )
                            Text(
                                text = item.fromAddress.ifBlank { "Unknown sender" },
                                color = Color(0xFF5E2DD2),
                                fontSize = 12.sp,
                            )
                            if (item.summary.isNotBlank()) {
                                Text(
                                    text = item.summary.take(180),
                                    color = Color(0xFF77717F),
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp,
                                )
                            }
                        }
                    }
                }
            }

            messageContent?.let { detail ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF7F5FB),
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        SectionTitle("Selected email")
                        Text(detail.subject.ifBlank { selectedMessage?.subject ?: "(No subject)" }, fontWeight = FontWeight.Bold)
                        if (detail.fromAddress.isNotBlank()) Text("From: ${detail.fromAddress}", fontSize = 12.sp)
                        if (detail.toAddress.isNotBlank()) Text("To: ${detail.toAddress}", fontSize = 12.sp)
                        Text(
                            text = detail.content.ifBlank { "No readable message content was returned." },
                            color = Color(0xFF393340),
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                        )
                        OutlinedButton(
                            onClick = {
                                recipient = detail.fromAddress
                                subject = if (detail.subject.startsWith("Re:", ignoreCase = true)) detail.subject else "Re: ${detail.subject}"
                                body = ""
                                replyMessageId = selectedMessage?.id
                                composeExpanded = true
                            },
                            enabled = detail.fromAddress.contains("@"),
                        ) {
                            Text("PREPARE REPLY")
                        }
                    }
                }
            }

            SectionTitle(
                when {
                    !replyMessageId.isNullOrBlank() -> "Reply to email"
                    composeExpanded -> "Compose email"
                    else -> "Send an email"
                },
            )
            if (composeExpanded) {
                OutlinedTextField(
                    value = recipient,
                    onValueChange = { recipient = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("To email address") },
                )
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Subject") },
                )
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    label = { Text("Message") },
                )
                Button(
                    onClick = { sendEmail() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loading,
                ) {
                    Text(if (replyMessageId.isNullOrBlank()) "SEND EMAIL" else "SEND REPLY")
                }
                OutlinedButton(
                    onClick = { composeExpanded = false },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loading,
                ) {
                    Text("CANCEL")
                }
            } else {
                OutlinedButton(
                    onClick = {
                        composeExpanded = true
                        recipient = ""
                        subject = ""
                        body = ""
                        replyMessageId = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loading,
                ) {
                    Text("COMPOSE")
                }
            }

            if (scopeText.isNotBlank()) {
                Text(
                    text = "Granted permissions: $scopeText",
                    color = Color(0xFF77707F),
                    fontSize = 10.sp,
                    lineHeight = 15.sp,
                )
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SectionTitle(value: String) {
    Text(
        text = value,
        color = Color(0xFF2A2232),
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
    )
}
