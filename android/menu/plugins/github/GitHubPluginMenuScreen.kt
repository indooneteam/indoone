package com.indoone.menu.plugins.github

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
    val issues = remember { mutableStateListOf<GitHubIssue>() }
    val pullRequests = remember { mutableStateListOf<GitHubPullRequest>() }
    var activeTab by remember { mutableStateOf(0) }
    var browseLoading by remember { mutableStateOf(false) }
    var browseMessage by remember { mutableStateOf("") }
    var selectedRepository by remember { mutableStateOf("") }
    var repositoryMenuExpanded by remember { mutableStateOf(false) }

    fun refreshConnection() {
        scope.launch {
            loading = true
            try {
                status = GitHubPluginApi.getConnectionStatus()
                if (status.connected && status.providerHealthy) {
                    repositories.clear()
                    repositories.addAll(GitHubPluginApi.listRepositories())
                    if (selectedRepository.isBlank() || repositories.none { it.fullName == selectedRepository }) {
                        selectedRepository = repositories.firstOrNull()?.fullName.orEmpty()
                    }
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

    LaunchedEffect(activeTab, status.connected, status.providerHealthy, selectedRepository) {
        if (!status.connected || !status.providerHealthy || activeTab == 0) return@LaunchedEffect
        browseLoading = true
        browseMessage = ""
        try {
            when (activeTab) {
                1 -> {
                    issues.clear()
                    issues.addAll(GitHubPluginApi.listAssignedIssues())
                }
                2 -> {
                    if (selectedRepository.isBlank()) {
                        pullRequests.clear()
                        browseMessage = "Connect an account with at least one accessible public repository first."
                    } else {
                        pullRequests.clear()
                        pullRequests.addAll(GitHubPluginApi.listPullRequests(selectedRepository))
                    }
                }
            }
        } catch (error: Exception) {
            browseMessage = error.message?.takeIf { it.isNotBlank() }
                ?: "Could not load GitHub data. Try refreshing."
        } finally {
            browseLoading = false
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
                TabRow(selectedTabIndex = activeTab) {
                    Tab(selected = activeTab == 0, onClick = { activeTab = 0 }, text = { Text("Repositories") })
                    Tab(selected = activeTab == 1, onClick = { activeTab = 1 }, text = { Text("Assigned issues") })
                    Tab(selected = activeTab == 2, onClick = { activeTab = 2 }, text = { Text("Pull requests") })
                }
                when (activeTab) {
                    0 -> {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Repositories", color = GithubInk, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                                Text("Recently updated · first 10", color = GithubMuted, fontSize = 11.sp)
                            }
                            Text(repositories.size.toString(), color = GithubPurple, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                        }
                        if (repositories.isEmpty() && !loading) {
                            Text("No repositories were returned for this account.", color = GithubMuted, fontSize = 12.sp)
                        }
                        repositories.take(10).forEach { repository ->
                            Surface(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    if (repository.htmlUrl.isNotBlank()) {
                                        runCatching {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(repository.htmlUrl)))
                                        }
                                    }
                                },
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
                    1 -> {
                        Text("Open issues assigned to your GitHub account", color = GithubMuted, fontSize = 12.sp)
                        if (browseLoading) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = GithubPurple, strokeWidth = 2.dp)
                                Text("Loading assigned issues…", color = GithubMuted, fontSize = 12.sp)
                            }
                        } else if (browseMessage.isNotBlank()) {
                            Text(browseMessage, color = GithubMuted, fontSize = 12.sp)
                        } else if (issues.isEmpty()) {
                            Text("No open assigned issues were returned.", color = GithubMuted, fontSize = 12.sp)
                        }
                        issues.take(10).forEach { issue ->
                            Surface(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    if (issue.htmlUrl.isNotBlank()) {
                                        runCatching {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(issue.htmlUrl)))
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFECE7F8)),
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("#${issue.number} · ${issue.title}", color = GithubInk, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        listOf(issue.state, issue.labels.joinToString(", ")).filter { it.isNotBlank() }.joinToString(" · "),
                                        color = GithubMuted, fontSize = 11.sp,
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Pull requests", color = GithubInk, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                                Text("Open pull requests in the selected repository", color = GithubMuted, fontSize = 11.sp)
                            }
                            TextButton(onClick = { repositoryMenuExpanded = true }, enabled = repositories.isNotEmpty()) {
                                Text("Choose repo")
                            }
                            DropdownMenu(
                                expanded = repositoryMenuExpanded,
                                onDismissRequest = { repositoryMenuExpanded = false },
                            ) {
                                repositories.forEach { repository ->
                                    DropdownMenuItem(
                                        text = { Text(repository.fullName) },
                                        onClick = {
                                            selectedRepository = repository.fullName
                                            repositoryMenuExpanded = false
                                        },
                                    )
                                }
                            }
                        }
                        Text(selectedRepository.ifBlank { "No repository selected" }, color = GithubPurple, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        if (browseLoading) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = GithubPurple, strokeWidth = 2.dp)
                                Text("Loading pull requests…", color = GithubMuted, fontSize = 12.sp)
                            }
                        } else if (browseMessage.isNotBlank()) {
                            Text(browseMessage, color = GithubMuted, fontSize = 12.sp)
                        } else if (pullRequests.isEmpty()) {
                            Text("No open pull requests were returned for this repository.", color = GithubMuted, fontSize = 12.sp)
                        }
                        pullRequests.take(10).forEach { pullRequest ->
                            Surface(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    if (pullRequest.htmlUrl.isNotBlank()) {
                                        runCatching {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(pullRequest.htmlUrl)))
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFECE7F8)),
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("#${pullRequest.number} · ${pullRequest.title}", color = GithubInk, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        if (pullRequest.draft) "Draft · ${pullRequest.state}" else pullRequest.state,
                                        color = GithubMuted, fontSize = 11.sp,
                                    )
                                }
                            }
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
