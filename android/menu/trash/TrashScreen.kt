package com.indoone.menu.trash

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indoone.accounts.AccountRepository
import com.indoone.accounts.TrashRecord
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x8819141F)),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f),
            shape = RoundedCornerShape(25.dp),
            color = Color.White,
            shadowElevation = 14.dp,
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(23.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFF6331DB), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("I", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "Trash",
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2C2733),
                            )
                            Text(
                                "Deleted accounts",
                                modifier = Modifier.padding(top = 2.dp),
                                color = Color(0xFF8A8492),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }

                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(35.dp)
                            .background(Color(0xFFF5F2F8), RoundedCornerShape(11.dp)),
                    ) {
                        Text("×", fontSize = 21.sp, color = Color(0xFF2C2733))
                    }
                }

                Spacer(Modifier.size(10.dp))

                error?.let {
                    Text(
                        it,
                        modifier = Modifier.padding(bottom = 8.dp),
                        color = Color(0xFFB3261E),
                        fontSize = 11.sp,
                    )
                }

                if (trash.isEmpty()) {
                    EmptyTrashState()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
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
        }
    }

    selectedDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { selectedDelete = null },
            title = { Text("Permanently delete ${item.account.name}?") },
            text = {
                Text(
                    "This removes the account from Trash and all synced account copies. This action cannot be undone.",
                )
            },
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
                ) { Text("Delete", color = Color(0xFFB3261E)) }
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
    var offsetX by remember(item.account.id) { mutableFloatStateOf(0f) }
    val maxReveal = 84.dp.value

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF7EDEE), RoundedCornerShape(16.dp)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(84.dp)
                .align(Alignment.CenterEnd)
                .background(Color(0xFFF7E0E1), RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text("Delete", color = Color(0xFFB3261E), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .pointerInput(item.account.id) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, dragAmount ->
                            offsetX = (offsetX + dragAmount).coerceIn(-maxReveal, 0f)
                        },
                        onDragEnd = {
                            offsetX = if (offsetX <= -maxReveal / 2f) -maxReveal else 0f
                        },
                        onDragCancel = { offsetX = 0f },
                    )
                },
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE9E5EF)),
            shadowElevation = 3.dp,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 11.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color(0xFFF4F1F8), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        item.account.name.firstOrNull()?.uppercase() ?: "A",
                        color = Color(0xFF4285F4),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Spacer(Modifier.width(11.dp))

                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        item.account.name.ifBlank { "Account" },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF211D27),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        "${item.account.email.ifBlank { "Authenticator account" }} · $days day${if (days == 1) "" else "s"} left",
                        modifier = Modifier.padding(top = 4.dp),
                        color = Color(0xFF8A8490),
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Button(
                    onClick = onRestore,
                    modifier = Modifier.height(34.dp),
                ) {
                    Text("Restore", fontSize = 11.sp)
                }
            }
        }

        if (offsetX <= -maxReveal / 2f) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(84.dp, 64.dp),
                contentAlignment = Alignment.Center,
            ) {
                TextButton(onClick = onPermanentDelete) {
                    Text("Delete", color = Color(0xFFB3261E), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun EmptyTrashState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(Color(0xFFF0EAFF), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text("⌫", color = Color(0xFF7140DA), fontSize = 27.sp)
        }
        Text(
            "Trash is empty",
            modifier = Modifier.padding(top = 12.dp),
            color = Color(0xFF211D27),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            "Deleted accounts stay here for 30 days. Swipe left on an account to permanently delete it sooner.",
            modifier = Modifier.padding(top = 5.dp),
            color = Color(0xFF85808B),
            fontSize = 12.sp,
        )
    }
}
