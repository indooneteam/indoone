package com.indoone.connect

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.indoone.menu.AppBottomNav
import com.indoone.menu.AppTab
import com.indoone.menu.AppTopBar
import androidx.compose.foundation.layout.safeDrawingPadding

@Composable
fun ConnectScreen(
    state: ConnectState = ConnectState(),
    onMenuClick: () -> Unit = {},
    onPair: () -> Unit = {},
    onConnect: () -> Unit = {},
    onDevices: () -> Unit = {},
    onAccountsClick: () -> Unit = {},
    onLobbyClick: () -> Unit = {},
    onConnectClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxSize().safeDrawingPadding(),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                AppTopBar(onMenuClick = onMenuClick)
                Column(
                    modifier = Modifier.weight(1f).padding(horizontal = 18.dp, vertical = 18.dp),
                ) {
                    Text(
                        text = state.eyebrow,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.padding(top = 2.dp))
                    Text(
                        text = state.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.padding(top = 6.dp))
                    Text(
                        text = state.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.padding(top = 16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        ConnectActionCard("Pair", "Pair another Indoone device", Modifier.weight(1f), onPair)
                        ConnectActionCard("Connect", "Choose a nearby device", Modifier.weight(1f), onConnect)
                    }
                    Spacer(Modifier.padding(top = 10.dp))
                    ConnectActionCard("Devices", "Manage connected devices", Modifier.fillMaxWidth(), onDevices)
                }
                AppBottomNav(
                    activeTab = AppTab.CONNECT,
                    onAccountsClick = onAccountsClick,
                    onLobbyClick = onLobbyClick,
                    onConnectClick = onConnectClick,
                    onSettingsClick = onSettingsClick,
                )
            }
        }
    }
}

@Composable
private fun ConnectActionCard(
    title: String,
    description: String,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier.alpha(0.62f),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        onClick = {},
        enabled = false,
    ) {
        Column(modifier = Modifier.padding(13.dp)) {
            Text(
                text = when (title) { "Pair" -> "⌁"; "Connect" -> "↔"; else -> "▣" },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.padding(top = 9.dp))
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.padding(top = 3.dp))
            Text(description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.padding(top = 5.dp))
            Text("COMING SOON", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
        }
    }
}
