package com.indoone.menu.memory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indoone.menu.data.UserDataRepository
import kotlinx.coroutines.launch

private data class MemoryItem(val key: String, val value: String)

@Composable
fun MemoryMenuScreen(onBack: () -> Unit) {
    val repository = remember { UserDataRepository() }
    val scope = rememberCoroutineScope()
    var memories by remember { mutableStateOf<List<MemoryItem>>(emptyList()) }
    var status by remember { mutableStateOf<String?>(null) }
    var showAdd by remember { mutableStateOf(false) }
    var newKey by remember { mutableStateOf("") }
    var newValue by remember { mutableStateOf("") }

    fun reload() {
        scope.launch {
            runCatching { repository.readMemory() }
                .onSuccess { raw ->
                    memories = raw.mapNotNull { (key, value) ->
                        val map = value as? Map<*, *> ?: return@mapNotNull null
                        val text = map["value"]?.toString().orEmpty()
                        if (text.isBlank()) null else MemoryItem(key, text)
                    }.sortedBy { it.key }
                }
                .onFailure { status = it.message ?: "Could not load memory." }
        }
    }

    LaunchedEffect(Unit) { reload() }

    Column(modifier = Modifier.fillMaxSize().padding(22.dp)) {
        Button(onClick = onBack) { Text("Back") }
        Text("Memory", color = Color(0xFF5E2DD2), fontSize = 24.sp, modifier = Modifier.padding(top = 12.dp))
        Text("Important long-term information. The AI memory engine will validate changes before automatically adding or replacing memories.", color = Color(0xFF77707F), fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp, bottom = 12.dp))
        Button(onClick = { showAdd = true }, modifier = Modifier.fillMaxWidth()) { Text("Add memory") }
        status?.let { Text(it, color = Color(0xFFD93025), fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp)) }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 14.dp)) {
            items(memories, key = { it.key }) { memory ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(memory.key, color = Color(0xFF5E2DD2), fontSize = 12.sp)
                        Text(memory.value, color = Color(0xFF26212D), fontSize = 14.sp, modifier = Modifier.padding(top = 2.dp))
                    }
                    TextButton(onClick = {
                        scope.launch {
                            runCatching { repository.deleteMemory(memory.key) }
                                .onSuccess { reload() }
                                .onFailure { status = it.message ?: "Could not delete memory." }
                        }
                    }) { Text("Delete") }
                }
            }
        }
    }

    if (showAdd) {
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("Add memory") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(newKey, { newKey = it }, label = { Text("Key") })
                    OutlinedTextField(newValue, { newValue = it }, label = { Text("Value") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        runCatching { repository.setMemory(newKey, newValue) }
                            .onSuccess {
                                showAdd = false
                                newKey = ""
                                newValue = ""
                                reload()
                            }
                            .onFailure { status = it.message ?: "Could not save memory." }
                    }
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Cancel") } },
        )
    }
}
