package com.indoone.menu.trash

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indoone.accounts.AccountRecord
import com.indoone.accounts.AccountRepository
import com.indoone.accounts.TrashRecord
import com.indoone.accounts.accounts.list.AccountTotpGenerator
import com.indoone.menu.AppBottomNav
import com.indoone.menu.AppTab
import com.indoone.menu.AppTopBar
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
    var accounts by remember { mutableStateOf<List<AccountRecord>>(emptyList()) }
    var selectedDelete by remember { mutableStateOf<TrashRecord?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    fun refresh() {
        scope.launch {
            runCatching { repository.listTrash() }
                .onSuccess { trash = it; error = null }
                .onFailure { error = it.message ?: "Could not load Trash" }
            runCatching { repository.getAll() }
                .onSuccess { accounts = it }
        }
    }

    LaunchedEffect(repository) { refresh() }

    Box(Modifier.fillMaxSize()) {
        TrashAccountsBackdrop(accounts)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x5519141F))
                .clickable(onClick = onBack),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 5.dp, end = 5.dp, bottom = 14.dp)
                    .clickable(onClick = {}),
                shape = RoundedCornerShape(25.dp),
                color = Color.White,
                shadowElevation = 14.dp,
            ) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 21.dp, vertical = 20.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        TrashIndooneLogo(Modifier.size(36.dp))
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Trash", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2C2733))
                            Text("Deleted accounts", Modifier.padding(top = 2.dp), color = Color(0xFF8A8492), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                        IconButton(onClick = onBack, modifier = Modifier.size(35.dp).background(Color(0xFFF5F2F8), RoundedCornerShape(11.dp))) {
                            Text("×", fontSize = 21.sp, color = Color(0xFF2C2733))
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    error?.let { Text(it, Modifier.padding(bottom = 8.dp), color = Color(0xFFB3261E), fontSize = 11.sp) }
                    if (trash.isEmpty()) {
                        EmptyTrashState()
                    } else {
                        Spacer(Modifier.height(6.dp))
                        LazyColumn(Modifier.fillMaxWidth().heightIn(max = 470.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
    }

    selectedDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { selectedDelete = null },
            title = { Text("Permanently delete ${item.account.name}?") },
            text = { Text("This removes the account from Trash and all synced account copies. This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    selectedDelete = null
                    scope.launch {
                        runCatching { repository.permanentlyDeleteFromTrash(item.account.id) }
                            .onSuccess { refresh() }
                            .onFailure { error = it.message ?: "Could not permanently delete account" }
                    }
                }) { Text("Delete", color = Color(0xFFB3261E)) }
            },
            dismissButton = { TextButton(onClick = { selectedDelete = null }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun TrashAccountsBackdrop(accounts: List<AccountRecord>) {
    val nowMillis = System.currentTimeMillis()
    Box(Modifier.fillMaxSize().background(Color.White)) {
        Column(Modifier.fillMaxSize()) {
            AppTopBar(onMenuClick = {}, onSearchClick = {})
            Surface(
                modifier = Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 4.dp).height(44.dp),
                shape = RoundedCornerShape(14.dp), color = Color(0xFFFAF9FC), border = BorderStroke(1.dp, Color(0xFFE6E2ED)),
            ) {
                Row(Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("⌕", color = Color(0xFF77717F), fontSize = 19.sp)
                    Spacer(Modifier.width(9.dp)); Text("Search accounts", color = Color(0xFF77717F), fontSize = 14.sp)
                    Spacer(Modifier.weight(1f)); Text("×", color = Color(0xFF77717F), fontSize = 22.sp)
                }
            }
            Column(Modifier.fillMaxWidth().weight(1f).padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 104.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Column {
                        Text("SECURE & PRIVATE", color = Color(0xFF7650D8), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.3.sp)
                        Text("Your accounts", Modifier.padding(top = 1.dp), color = Color(0xFF17151D), fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    }
                    Surface(shape = RoundedCornerShape(10.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E0ED))) {
                        Text("Sort ↕", Modifier.padding(horizontal = 11.dp, vertical = 8.dp), color = Color(0xFF5F566B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(14.dp))
                Column(verticalArrangement = Arrangement.spacedBy(11.dp)) { accounts.take(4).forEach { TrashBackdropAccountRow(it, nowMillis) } }
            }
        }
        AppBottomNav(activeTab = AppTab.ACCOUNTS, onAccountsClick = {}, onLobbyClick = {}, onConnectClick = {}, onSettingsClick = {}, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun TrashBackdropAccountRow(account: AccountRecord, nowMillis: Long) {
    val period = account.period.coerceAtLeast(1)
    val seconds = (period - ((nowMillis / 1000L) % period).toInt()).coerceIn(1, period)
    val code = runCatching { AccountTotpGenerator.generate(account.secret, nowMillis, period, account.digits, account.algorithm) }.getOrDefault("------")
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFECE9F0)), shadowElevation = 2.dp) {
        Row(Modifier.fillMaxWidth().padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).background(Color(0xFFF5F3F8), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) { Text(account.name.firstOrNull()?.uppercase() ?: "?", color = Color(0xFF4285F4), fontSize = 21.sp, fontWeight = FontWeight.Black) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(account.name, color = Color(0xFF17151D), fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(account.email, Modifier.padding(top = 3.dp), color = Color(0xFF89838F), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(code, Modifier.padding(top = 2.dp), color = if (seconds >= 15) Color(0xFF20883E) else if (seconds >= 5) Color(0xFFAD8500) else Color(0xFFC62828), fontSize = 23.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            Text("☆", color = Color(0xFFB7A8D3), fontSize = 20.sp)
            Spacer(Modifier.width(7.dp))
            Box(Modifier.size(35.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val progress = seconds.toFloat() / period.toFloat()
                    drawCircle(Color(0xFFD9F0DF), style = androidx.compose.ui.graphics.drawscope.Stroke(3.dp.toPx()))
                    drawArc(Color(0xFF20883E), -90f, progress * 360f, false, style = androidx.compose.ui.graphics.drawscope.Stroke(3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))
                }
                Text(seconds.toString(), color = Color(0xFF20883E), fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun TrashIndooneLogo(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val scaleFactor = size.minDimension / 48f
        androidx.compose.ui.graphics.drawscope.scale(scaleFactor) {
            androidx.compose.ui.graphics.drawscope.rotate(45f, pivot = androidx.compose.ui.geometry.Offset(24f, 24f)) {
                drawRoundRect(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFFC15CFF), Color(0xFF7C3AED), Color(0xFF22C7FF)), androidx.compose.ui.geometry.Offset(11f, 11f), androidx.compose.ui.geometry.Offset(37f, 37f)),
                    topLeft = androidx.compose.ui.geometry.Offset(11f, 11f), size = androidx.compose.ui.geometry.Size(26f, 26f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
                )
            }
            val outer = Path().apply { moveTo(24f, 14f); lineTo(27.2f, 20.8f); lineTo(34f, 24f); lineTo(27.2f, 27.2f); lineTo(24f, 34f); lineTo(20.8f, 27.2f); lineTo(14f, 24f); lineTo(20.8f, 20.8f); close() }
            drawPath(outer, color = Color(0xFF0A0A18))
            val inner = Path().apply { moveTo(24f, 20.8f); lineTo(25.2f, 22.8f); lineTo(27.2f, 24f); lineTo(25.2f, 25.2f); lineTo(24f, 27.2f); lineTo(22.8f, 25.2f); lineTo(20.8f, 24f); lineTo(22.8f, 22.8f); close() }
            drawPath(inner, color = Color(0xFF60A5FA))
        }
    }
}

@Composable
private fun TrashRow(item: TrashRecord, onRestore: () -> Unit, onPermanentDelete: () -> Unit) {
    val remainingMs = max(0L, item.purgeAt - System.currentTimeMillis())
    val days = max(1, ceil(remainingMs / (24.0 * 60.0 * 60.0 * 1000.0)).toInt())
    var offsetX by remember(item.account.id) { mutableFloatStateOf(0f) }
    val maxReveal = 84.dp.value
    Box(Modifier.fillMaxWidth().background(Color(0xFFF7EDEE), RoundedCornerShape(16.dp))) {
        Box(Modifier.fillMaxHeight().width(84.dp).align(Alignment.CenterEnd).background(Color(0xFFF7E0E1), RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)), contentAlignment = Alignment.Center) { Text("Delete", color = Color(0xFFB3261E), fontSize = 12.sp, fontWeight = FontWeight.Bold) }
        Surface(
            Modifier.fillMaxWidth().offset { IntOffset(offsetX.roundToInt(), 0) }.pointerInput(item.account.id) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, dragAmount -> offsetX = (offsetX + dragAmount).coerceIn(-maxReveal, 0f) },
                    onDragEnd = { offsetX = if (offsetX <= -maxReveal / 2f) -maxReveal else 0f },
                    onDragCancel = { offsetX = 0f },
                )
            },
            shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE9E5EF)), shadowElevation = 3.dp,
        ) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 11.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(42.dp).background(Color(0xFFF4F1F8), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) { Text(item.account.name.firstOrNull()?.uppercase() ?: "A", color = Color(0xFF4285F4), fontSize = 18.sp, fontWeight = FontWeight.Bold) }
                Spacer(Modifier.width(11.dp))
                Column(Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(item.account.name.ifBlank { "Account" }, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF211D27), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${item.account.email.ifBlank { "Authenticator account" }} · $days day${if (days == 1) "" else "s"} left", Modifier.padding(top = 4.dp), color = Color(0xFF8A8490), fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Button(onClick = onRestore, modifier = Modifier.height(34.dp)) { Text("Restore", fontSize = 11.sp) }
            }
        }
        if (offsetX <= -maxReveal / 2f) {
            Box(Modifier.align(Alignment.CenterEnd).size(84.dp, 64.dp), contentAlignment = Alignment.Center) { TextButton(onClick = onPermanentDelete) { Text("Delete", color = Color(0xFFB3261E), fontSize = 12.sp, fontWeight = FontWeight.Bold) } }
        }
    }
}

@Composable
private fun EmptyTrashState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 32.dp, bottom = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(56.dp).background(Color(0xFFF0EAFF), RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) { Text("⌫", color = Color(0xFF7140DA), fontSize = 27.sp) }
        Text("Trash is empty", Modifier.padding(top = 12.dp), color = Color(0xFF211D27), fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text("Deleted accounts stay here for 30 days. Swipe left on an account to permanently delete it sooner.", Modifier.padding(top = 5.dp), color = Color(0xFF85808B), fontSize = 12.sp)
    }
}
