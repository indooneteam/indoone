package com.indoone.menu.plugins.github

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val GithubPurple = Color(0xFF5E2DD2)
private val GithubInk = Color(0xFF272238)
private val GithubMuted = Color(0xFF777286)
private val GithubPale = Color(0xFFF4F0FF)
private val GithubGreen = Color(0xFF218653)

@Composable
fun GitHubPluginMenuScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf(GitHubConnectionStatus(connected = false)) }
    var loading by remember { mutableStateOf(true) }
    var connecting by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("Checking GitHub connection…") }
    val repositories = remember { mutableStateListOf<GitHubRepository>() }

    fun refreshConnection() {
        scope.launch {
            loading = true
            try {
                status = GitHubPluginApi.getConnectionStatus()
                if (status.connected && status.providerHealthy) {
                    repositories.clear()
                    repositories.addAll(GitHubPluginApi.listRepositories())
                    message = "Live GitHub API check succeeded."
                } else if (status.connected) {
                    repositories.clear()
                    message = "OAuth is saved, but GitHub API verification failed. Reconnect and review permissions."
                } else {
                    repositories.clear()
                    message = "No GitHub account is connected yet."
                }
            } catch (error: Exception) {
                message = error.message?.takeIf { it.isNotBlank() }
                    ?: "Could not check the GitHub connection."
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { refreshConnection() }

    LaunchedEffect(connecting) {
        if (!connecting) return@LaunchedEffect
        repeat(60) {
            delay(2_000L)
            val value = runCatching { GitHubPluginApi.getConnectionStatus() }.getOrNull()
            if (value?.connected == true && value.providerHealthy) {
                status = value
                connecting = false
                loading = false
                message = "GitHub connected and verified. Loading repositories…"
                runCatching { GitHubPluginApi.listRepositories() }
                    .onSuccess { repositories.clear(); repositories.addAll(it) }
                    .onFailure { message = it.message ?: "Connected; repository list could not be loaded." }
                return@LaunchedEffect
            }
        }
        if (connecting) {
            connecting = false
            loading = false
            message = "Authorization is not confirmed yet. Finish GitHub approval in the browser, then press Refresh."
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = GithubPurple)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("GitHub", color = GithubInk, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                    Text("Developer integration", color = GithubMuted, fontSize = 12.sp)
                }
                IconButton(onClick = { refreshConnection() }, enabled = !loading && !connecting) {
                    Icon(Icons.Outlined.Refresh, contentDescription = "Refresh GitHub connection", tint = GithubPurple)
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = GithubPale,
                border = BorderStroke(1.dp, Color(0xFFE5DDFB)),
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Connect GitHub to Indoone", color = GithubInk, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Review public repositories, assigned issues, and pull requests. Issue creation and comments still require explicit approval.",
                        color = GithubMuted, fontSize = 12.sp, lineHeight = 18.sp,
                    )
                    Button(
                        onClick = {
                            if (connecting || loading || status.connected) return@Button
                            connecting = true
                            loading = true
                            message = "Opening GitHub authorization…"
                            scope.launch {
                                runCatching { GitHubPluginApi.startConnection() }
                                    .onSuccess { result ->
                                        runCatching {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(result.authorizationUrl)))
                                        }.onFailure {
                                            connecting = false
                                            loading = false
                                            message = it.message ?: "Could not open GitHub authorization."
                                        }
                                    }
                                    .onFailure {
                                        connecting = false
                                        loading = false
                                        message = it.message ?: "Could not start GitHub authorization."
                                    }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !status.connected && !loading && !connecting,
                        colors = ButtonDefaults.buttonColors(containerColor = GithubPurple),
                    ) {
                        Text(if (status.connected) "CONNECTED" else if (connecting) "WAITING FOR AUTHORIZATION…" else "CONNECT GITHUB")
                    }
                    if (loading || connecting) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = GithubPurple, strokeWidth = 2.dp)
                            Text(message, color = GithubMuted, fontSize = 12.sp)
                        }
                    } else {
                        val displayLogin = status.login.ifBlank { "GitHub user" }
                        Text(
                            text = if (status.connected && status.providerHealthy) "✓ Connected as " + displayLogin else message,
                            color = if (status.connected && status.providerHealthy) GithubGreen else GithubMuted,
                            fontSize = 12.sp, lineHeight = 18.sp,
                        )
                    }
                    if (status.connected) {
                        OutlinedButton(
                            onClick = { refreshConnection() },
                            enabled = !loading && !connecting,
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("TEST CONNECTION / REFRESH") }
                    }
                }
            }

            if (status.connected && status.providerHealthy) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Repositories", color = GithubInk, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                        Text("Most recently updated · first 10", color = GithubMuted, fontSize = 11.sp)
                    }
                    Text(repositories.size.toString(), color = GithubPurple, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                }
                if (repositories.isEmpty() && !loading) {
                    Text("No repositories were returned for this account.", color = GithubMuted, fontSize = 12.sp)
                }
                repositories.take(10).forEach { repository ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFECE7F8)),
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp)) {
                            Text(repository.fullName, color = GithubInk, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            val visibility = if (repository.isPrivate) "Private" else "Public"
                            val branchLabel = repository.defaultBranch.takeIf { it.isNotBlank() }?.let { "Default: $it" }.orEmpty()
                            Text(
                                text = listOf(visibility, branchLabel).filter { it.isNotBlank() }.joinToString(" · "),
                                color = GithubMuted, fontSize = 11.sp,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Text(
                "For least privilege, this OAuth setup requests public-repository access. Private-repository access needs a separately configured GitHub App with granular permissions.",
                color = GithubMuted, fontSize = 11.sp, lineHeight = 16.sp,
            )
            TextButton(onClick = onBack, modifier = Modifier.align(Alignment.Start)) { Text("Back", color = GithubPurple) }
        }
    }
}
