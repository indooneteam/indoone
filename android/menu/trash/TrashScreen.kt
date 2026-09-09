package com.indoone.menu.trash

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indoone.accounts.AccountRecord
import com.indoone.accounts.AccountRepository
import kotlinx.coroutines.delay

@Composable
fun TrashScreen(
    repository: AccountRepository,
    onBack: () -> Unit,
    onAccountsClick: () -> Unit,
    onLobbyClick: () -> Unit,
    onConnectClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    var trash by remember { mutableStateOf<List<AccountRecord>>(emptyList()) }
    var refreshTick by remember { mutableStateOf(0) }

    LaunchedEffect(refreshTick) {
        trash = repository.listTrash()
    }

    TrashAccountsBackdrop(
        onAccountsClick = onAccountsClick,
        onLobbyClick = onLobbyClick,
        onConnectClick = onConnectClick,
        onSettingsClick = onSettingsClick,
    )

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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 21.dp, vertical = 20.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TrashIndooneLogo(Modifier.size(35.dp))
                    Spacer(Modifier.size(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Trash", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = Color(0xFF201C25))
                        Text("Deleted accounts", fontSize = 11.sp, color = Color(0xFF8A8492))
                    }
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(35.dp)
                            .background(Color(0xFFF5F2F8), RoundedCornerShape(11.dp)),
                    ) {
                        Text("×", fontSize = 22.sp, color = Color(0xFF5D5666))
                    }
                }
                Spacer(Modifier.height(14.dp))

                if (trash.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, bottom = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        TrashEmptyIcon(Modifier.size(64.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("Trash is empty", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2C2733))
                        Spacer(Modifier.height(4.dp))
                        Text("Deleted accounts will appear here.", fontSize = 12.sp, color = Color(0xFF8A8492))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(trash, key = { it.id }) { account ->
                            TrashRow(
                                account = account,
                                onRestore = {
                                    repository.restoreFromTrash(account.id)
                                    refreshTick++
                                },
                                onDelete = {
                                    repository.permanentlyDeleteTrash(account.id)
                                    refreshTick++
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrashAccountsBackdrop(
    onAccountsClick: () -> Unit,
    onLobbyClick: () -> Unit,
    onConnectClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    com.indoone.accounts.AccountsScreen(
        onAccountsClick = onAccountsClick,
        onLobbyClick = onLobbyClick,
        onConnectClick = onConnectClick,
        onSettingsClick = onSettingsClick,
        onAccountClick = {},
        onAddAccountClick = {},
        onMenuClick = {},
        onSearchClick = {},
        onSortClick = {},
    )
}

@Composable
private fun TrashRow(
    account: AccountRecord,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
) {
    val period = account.period.coerceAtLeast(1)
    var seconds by remember(account.id) { mutableStateOf(period) }
    DisposableEffect(account.id, period) {
        val job = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).let { scope ->
            scope.launch {
                while (true) {
                    delay(1000)
                    seconds = if (seconds <= 1) period else seconds - 1
                }
            }
        }
        onDispose { job.cancel() }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF8F6FA),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TrashIndooneLogo(Modifier.size(35.dp))
            Spacer(Modifier.size(10.dp))
            Column(Modifier.weight(1f)) {
                Text(account.issuer, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2C2733))
                Text(account.accountName, fontSize = 11.sp, color = Color(0xFF8A8492))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(account.secret.takeLast(6).padStart(6, '•'), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2C2733))
                Spacer(Modifier.height(3.dp))
                Box(Modifier.size(35.dp), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.fillMaxSize()) {
                        val progress = seconds.toFloat() / period.toFloat()
                        drawCircle(Color(0xFFD9F0DF), style = Stroke(3.dp.toPx()))
                        drawArc(Color(0xFF20883E), -90f, progress * 360f, false, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
                    }
                    Text(seconds.toString(), color = Color(0xFF20883E), fontSize = 10.sp)
                }
            }
            Spacer(Modifier.size(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text("Restore", modifier = Modifier.clickable(onClick = onRestore), color = Color(0xFF20883E), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(5.dp))
                Text("Delete", modifier = Modifier.clickable(onClick = onDelete), color = Color(0xFFB42318), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun TrashIndooneLogo(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val scaleFactor = size.minDimension / 48f
        scale(scaleFactor) {
            rotate(45f, pivot = Offset(24f, 24f)) {
                drawRoundRect(
                    brush = Brush.linearGradient(
                        listOf(Color(0xFFC15CFF), Color(0xFF7C3AED), Color(0xFF22C7FF)),
                        Offset(11f, 11f),
                        Offset(37f, 37f),
                    ),
                    topLeft = Offset(11f, 11f),
                    size = Size(26f, 26f),
                    cornerRadius = CornerRadius(6f, 6f),
                )
            }
            val outer = Path().apply {
                moveTo(24f, 14f); lineTo(27.2f, 20.8f); lineTo(34f, 24f); lineTo(27.2f, 27.2f); lineTo(24f, 34f); lineTo(20.8f, 27.2f); lineTo(14f, 24f); lineTo(20.8f, 20.8f); close()
            }
            drawPath(outer, color = Color(0xFF0A0A18))
            val inner = Path().apply {
                moveTo(24f, 20.8f); lineTo(25.2f, 22.8f); lineTo(27.2f, 24f); lineTo(25.2f, 25.2f); lineTo(24f, 27.2f); lineTo(22.8f, 25.2f); lineTo(20.8f, 24f); lineTo(22.8f, 22.8f); close()
            }
            drawPath(inner, color = Color(0xFF60A5FA))
        }
    }
}

@Composable
private fun TrashEmptyIcon(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        drawRoundRect(
            brush = Brush.linearGradient(listOf(Color(0xFFE3D7FF), Color(0xFFDFF7FF))),
            topLeft = Offset(size.width * 0.22f, size.height * 0.18f),
            size = Size(size.width * 0.56f, size.height * 0.64f),
            cornerRadius = CornerRadius(size.width * 0.08f, size.width * 0.08f),
        )
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(size.width * 0.30f, size.height * 0.29f),
            size = Size(size.width * 0.40f, size.height * 0.08f),
            cornerRadius = CornerRadius(size.width * 0.03f, size.width * 0.03f),
        )
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(size.width * 0.30f, size.height * 0.44f),
            size = Size(size.width * 0.30f, size.height * 0.08f),
            cornerRadius = CornerRadius(size.width * 0.03f, size.width * 0.03f),
        )
    }
}
