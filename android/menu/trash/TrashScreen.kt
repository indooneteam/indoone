package com.indoone.menu.trash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.indoone.accounts.AccountRepository
import com.indoone.accounts.TrashRecord
import com.indoone.menu.AppBottomNav
import com.indoone.menu.AppTab
import com.indoone.menu.AppTopBar
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.max

@Composable
fun TrashScreen(
    repository: AccountRepository,
    onBack: () -> Unit,
    onAccountsClick: () -> Unit,
    onLobbyClick: () -> Unit,
    onConnectClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var trash by remember { mutableStateOf<List<TrashRecord>>(emptyList()) }
    var selectedDelete by remember { mutableStateOf<TrashRecord?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    fun refresh() {
        scope.launch {
            runCatching { repository.listTrash() }
                .onSuccess {
                    trash = it
                    error = null
                }
                .onFailure { error = it.message ?: "Could not load Trash" }
        }
    }

    LaunchedEffect(repository) { refresh() }

    Column(modifier = Modifier.fillMaxSize()) {
        AppTopBar(onMenuClick = onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            Text("TRASH", style = MaterialTheme.typography.labelSmall)
            Text("Deleted accounts", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(
                "Deleted accounts stay here for 30 days. Restore an account or permanently delete it sooner.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(16.dp))

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
            }

            if (trash.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 2.dp,
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(22.dp)) {
                        Text("Trash is empty", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Deleted accounts stay here for 30 days. You can restore them before they expire.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(trash, key = { it.account.id }) { item ->
                        TrashRow(
                            item = item,
                            onRestore = {
                                scope.launch {
                                    runCatching { repository.restoreFromTrash(item.account.id) }
                                        .onSuccess { refresh() }
                                        .onFailure { error = it.message ?: "Could not restore account" }
                                }
                            },
                            onPermanentDelete = { selectedDelete = item },
                        )
                    }
                }
            }
        }
        AppBottomNav(
            activeTab = AppTab.SETTINGS,
            onAccountsClick = onAccountsClick,
            onLobbyClick = onLobbyClick,
            onConnectClick = onConnectClick,
            onSettingsClick = onSettingsClick,
        )
    }

    selectedDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { selectedDelete = null },
            title = { Text("Permanently delete ${item.account.name}?") },
            text = { Text("This removes the account from Trash and cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDelete = null
                        scope.launch {
                            runCatching { repository.permanentlyDeleteFromTrash(item.account.id) }
                                .onSuccess { refresh() }
                                .onFailure { error = it.message ?: "Could not permanently delete account" }
                        }
                    },
                ) { Text("Delete permanently", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { selectedDelete = null }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun TrashRow(
    item: TrashRecord,
    onRestore: () -> Unit,
    onPermanentDelete: () -> Unit,
) {
    val remainingMs = max(0L, item.purgeAt - System.currentTimeMillis())
    val days = max(1, ceil(remainingMs / (24.0 * 60.0 * 60.0 * 1000.0)).toInt())

    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Text(item.account.name.ifBlank { "Account" }, fontWeight = FontWeight.SemiBold)
            Text(
                "${item.account.email.ifBlank { "Authenticator account" }} · $days day${if (days == 1) "" else "s"} left",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onRestore, modifier = Modifier.weight(1f)) { Text("Restore") }
                OutlinedButton(onClick = onPermanentDelete, modifier = Modifier.weight(1f)) { Text("Delete") }
            }
        }
    }
}
