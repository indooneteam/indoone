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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontFamily
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
    val directoryContents = remember { mutableStateListOf<GitHubRepositoryEntry>() }
    var currentPath by remember { mutableStateOf("") }
    var selectedFileContent by remember { mutableStateOf<GitHubRepositoryFileContent?>(null) }
    var editingFile by remember { mutableStateOf(false) }
    var editedFileContent by remember { mutableStateOf("") }
    var fileCommitMessage by remember { mutableStateOf("") }
    var showFileCommitConfirmation by remember { mutableStateOf(false) }
    var committingFile by remember { mutableStateOf(false) }
    var fileEditInstruction by remember { mutableStateOf("") }
    var suggestingFileEdit by remember { mutableStateOf(false) }
    var codeQuery by remember { mutableStateOf("") }
    val codeMatches = remember { mutableStateListOf<GitHubCodeSearchMatch>() }
    var codeSearchLoading by remember { mutableStateOf(false) }
    var codeSearchMessage by remember { mutableStateOf("") }

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

    LaunchedEffect(activeTab, status.connected, status.providerHealthy, selectedRepository, currentPath) {
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
                3 -> {
                    if (selectedRepository.isBlank()) {
                        directoryContents.clear()
                        browseMessage = "Choose a repository before browsing files."
                    } else {
                        directoryContents.clear()
                        directoryContents.addAll(
                            GitHubPluginApi.listRepositoryContents(selectedRepository, currentPath),
                        )
                        selectedFileContent = null
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
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 10.dp),
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
                    Tab(selected = activeTab == 3, onClick = { activeTab = 3 }, text = { Text("Files & code") })
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
                    2 -> {
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
                    else -> {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Files & code", color = GithubInk, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                                Text("Browse folders, read source files, and search code", color = GithubMuted, fontSize = 11.sp)
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
                                            currentPath = ""
                                            selectedFileContent = null
                                            editingFile = false
                                            showFileCommitConfirmation = false
                                            repositoryMenuExpanded = false
                                        },
                                    )
                                }
                            }
                        }
                        Text(selectedRepository.ifBlank { "No repository selected" }, color = GithubPurple, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(
                                onClick = {
                                    currentPath = currentPath.substringBeforeLast("/", "")
                                    selectedFileContent = null
                                    editingFile = false
                                    showFileCommitConfirmation = false
                                },
                                enabled = currentPath.isNotBlank() && !browseLoading,
                            ) { Text("Up one folder") }
                            Text(currentPath.ifBlank { "/" }, color = GithubMuted, fontSize = 12.sp)
                        }
                        if (browseLoading) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = GithubPurple, strokeWidth = 2.dp)
                                Text("Loading repository files…", color = GithubMuted, fontSize = 12.sp)
                            }
                        } else if (browseMessage.isNotBlank()) {
                            Text(browseMessage, color = GithubMuted, fontSize = 12.sp)
                        } else if (directoryContents.isEmpty() && selectedFileContent == null) {
                            Text("This folder is empty, or GitHub returned no entries.", color = GithubMuted, fontSize = 12.sp)
                        }
                        directoryContents.take(100).forEach { entry ->
                            Surface(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    if (entry.type == "dir") {
                                        currentPath = entry.path
                                        selectedFileContent = null
                                        editingFile = false
                                        showFileCommitConfirmation = false
                                    } else if (entry.type == "file") {
                                        scope.launch {
                                            browseMessage = ""
                                            browseLoading = true
                                            try {
                                                val loadedFile = GitHubPluginApi.readRepositoryFile(selectedRepository, entry.path)
                                                selectedFileContent = loadedFile
                                                editedFileContent = loadedFile.content
                                                editingFile = false
                                            } catch (error: Exception) {
                                                browseMessage = error.message?.takeIf { it.isNotBlank() }
                                                    ?: "Could not read this file."
                                            } finally {
                                                browseLoading = false
                                            }
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFECE7F8)),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Text(if (entry.type == "dir") "📁" else "📄", fontSize = 17.sp)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(entry.name, color = GithubInk, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        Text(if (entry.type == "dir") "Folder" else "${entry.size} bytes", color = GithubMuted, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                        selectedFileContent?.let { file ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFF8F7FB),
                                border = BorderStroke(1.dp, Color(0xFFECE7F8)),
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(file.path, color = GithubPurple, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    if (editingFile && !file.truncated && file.sha.matches(Regex("[0-9a-fA-F]{40}"))) {
                                        Text(
                                            "Edit the text, review the commit message, then confirm before writing to GitHub.",
                                            color = GithubMuted, fontSize = 11.sp, lineHeight = 16.sp,
                                        )
                                        OutlinedTextField(
                                            value = fileEditInstruction,
                                            onValueChange = { fileEditInstruction = it },
                                            modifier = Modifier.fillMaxWidth(),
                                            label = { Text("Describe the change for Indoone AI") },
                                            minLines = 2,
                                            maxLines = 4,
                                        )
                                        Button(
                                            onClick = {
                                                val instruction = fileEditInstruction.trim()
                                                if (instruction.isBlank() || suggestingFileEdit || committingFile) return@Button
                                                scope.launch {
                                                    suggestingFileEdit = true
                                                    browseMessage = ""
                                                    try {
                                                        val proposal = GitHubPluginApi.suggestRepositoryFileEdit(
                                                            repository = selectedRepository,
                                                            path = file.path,
                                                            instruction = instruction,
                                                            currentContent = editedFileContent,
                                                        )
                                                        editedFileContent = proposal
                                                        fileCommitMessage = "AI edit: ${instruction.take(140)}"
                                                        browseMessage = "AI proposal ready. Review the complete file before committing."
                                                    } catch (error: Exception) {
                                                        browseMessage = error.message?.takeIf { it.isNotBlank() }
                                                            ?: "Indoone AI could not prepare a file proposal."
                                                    } finally {
                                                        suggestingFileEdit = false
                                                    }
                                                }
                                            },
                                            enabled = !suggestingFileEdit && !committingFile && fileEditInstruction.isNotBlank(),
                                            colors = ButtonDefaults.buttonColors(containerColor = GithubPurple),
                                        ) {
                                            Text(if (suggestingFileEdit) "Preparing proposal…" else "Ask Indoone AI")
                                        }
                                        OutlinedTextField(
                                            value = editedFileContent,
                                            onValueChange = { editedFileContent = it },
                                            modifier = Modifier.fillMaxWidth(),
                                            label = { Text("File content") },
                                            minLines = 8,
                                            maxLines = 30,
                                        )
                                        OutlinedTextField(
                                            value = fileCommitMessage,
                                            onValueChange = { fileCommitMessage = it },
                                            modifier = Modifier.fillMaxWidth(),
                                            label = { Text("Commit message") },
                                            singleLine = true,
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Button(
                                                onClick = { showFileCommitConfirmation = true },
                                                enabled = !committingFile &&
                                                    editedFileContent.isNotBlank() &&
                                                    editedFileContent != file.content &&
                                                    editedFileContent.toByteArray(Charsets.UTF_8).size <= 50_000 &&
                                                    fileCommitMessage.isNotBlank(),
                                                colors = ButtonDefaults.buttonColors(containerColor = GithubPurple),
                                            ) { Text("Review commit") }
                                            TextButton(
                                                onClick = {
                                                    editingFile = false
                                                    editedFileContent = file.content
                                                    fileCommitMessage = ""
                                                    fileEditInstruction = ""
                                                },
                                                enabled = !committingFile,
                                            ) { Text("Cancel edit", color = GithubPurple) }
                                        }
                                        Text(
                                            "Target: repository default branch. Files larger than 50 KB cannot be committed here.",
                                            color = GithubMuted, fontSize = 11.sp, lineHeight = 16.sp,
                                        )
                                    } else {
                                        Text(file.content, color = GithubInk, fontSize = 11.sp, lineHeight = 15.sp, fontFamily = FontFamily.Monospace)
                                        if (file.truncated) {
                                            Text("File output is truncated by the backend safety limit; editing is disabled.", color = GithubMuted, fontSize = 11.sp)
                                        } else if (file.sha.matches(Regex("[0-9a-fA-F]{40}"))) {
                                            TextButton(
                                                onClick = {
                                                    editedFileContent = file.content
                                                    fileCommitMessage = "Update ${file.path}"
                                                    fileEditInstruction = ""
                                                    editingFile = true
                                                },
                                                enabled = !committingFile,
                                            ) { Text("Edit file", color = GithubPurple) }
                                        } else {
                                            Text("Editing is unavailable because GitHub did not provide a valid file SHA.", color = GithubMuted, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                        if (showFileCommitConfirmation) {
                            selectedFileContent?.let { file ->
                                AlertDialog(
                                    onDismissRequest = { if (!committingFile) showFileCommitConfirmation = false },
                                    title = { Text("Confirm GitHub commit") },
                                    text = {
                                        Text(
                                            "Repository: $selectedRepository\nFile: ${file.path}\nCommit: ${fileCommitMessage.trim()}\n\nThis writes the edited file to the repository's default branch. Confirm only if you approve these exact changes.",
                                        )
                                    },
                                    confirmButton = {
                                        TextButton(
                                            enabled = !committingFile,
                                            onClick = {
                                                showFileCommitConfirmation = false
                                                scope.launch {
                                                    committingFile = true
                                                    browseMessage = ""
                                                    try {
                                                        val approvalToken = GitHubPluginApi.requestRepositoryFileCommitApproval()
                                                        val result = GitHubPluginApi.commitRepositoryFile(
                                                            repository = selectedRepository,
                                                            path = file.path,
                                                            content = editedFileContent,
                                                            message = fileCommitMessage.trim(),
                                                            expectedSha = file.sha,
                                                            approvalToken = approvalToken,
                                                        )
                                                        selectedFileContent = GitHubRepositoryFileContent(
                                                            path = result.path,
                                                            sha = result.fileSha,
                                                            htmlUrl = result.fileUrl,
                                                            content = editedFileContent,
                                                            truncated = false,
                                                        )
                                                        editingFile = false
                                                        fileCommitMessage = ""
                                                        browseMessage = "Committed successfully: ${result.commitSha.take(7)}"
                                                    } catch (error: Exception) {
                                                        browseMessage = error.message?.takeIf { it.isNotBlank() }
                                                            ?: "Could not commit the file. Please retry."
                                                    } finally {
                                                        committingFile = false
                                                    }
                                                }
                                            },
                                        ) { Text(if (committingFile) "Committing…" else "Confirm commit") }
                                    },
                                    dismissButton = {
                                        TextButton(
                                            onClick = { showFileCommitConfirmation = false },
                                            enabled = !committingFile,
                                        ) { Text("Cancel") }
                                    },
                                )
                            }
                        }
                        Text("Search this repository's code", color = GithubInk, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = codeQuery,
                            onValueChange = { codeQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Function name or text") },
                            singleLine = true,
                        )
                        Button(
                            onClick = {
                                if (selectedRepository.isBlank() || codeQuery.isBlank() || codeSearchLoading) return@Button
                                scope.launch {
                                    codeSearchLoading = true
                                    codeSearchMessage = ""
                                    codeMatches.clear()
                                    try {
                                        codeMatches.addAll(GitHubPluginApi.searchRepositoryCode(selectedRepository, codeQuery))
                                        if (codeMatches.isEmpty()) codeSearchMessage = "No matching code was found."
                                    } catch (error: Exception) {
                                        codeSearchMessage = error.message?.takeIf { it.isNotBlank() }
                                            ?: "Code search failed."
                                    } finally {
                                        codeSearchLoading = false
                                    }
                                }
                            },
                            enabled = selectedRepository.isNotBlank() && codeQuery.isNotBlank() && !codeSearchLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = GithubPurple),
                        ) {
                            Text(if (codeSearchLoading) "Searching…" else "Search code")
                        }
                        if (codeSearchMessage.isNotBlank()) {
                            Text(codeSearchMessage, color = GithubMuted, fontSize = 12.sp)
                        }
                        codeMatches.take(20).forEach { match ->
                            Surface(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    if (match.htmlUrl.isNotBlank()) {
                                        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(match.htmlUrl))) }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFECE7F8)),
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                    Text(match.path, color = GithubInk, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    match.snippets.take(2).forEach { snippet ->
                                        Text(snippet, color = GithubMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace, maxLines = 4)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.size(12.dp))
            Text(
                "For least privilege, this OAuth setup requests public-repository access. Private-repository access needs a separately configured GitHub App with granular permissions.",
                color = GithubMuted, fontSize = 11.sp, lineHeight = 16.sp,
            )
            TextButton(onClick = onBack, modifier = Modifier.align(Alignment.Start)) { Text("Back", color = GithubPurple) }
        }
    }
}
