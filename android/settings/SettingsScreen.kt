package com.indoone.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import com.indoone.menu.AppBottomNav
import com.indoone.menu.AppTab
import com.indoone.menu.AppTopBar

@Composable
fun SettingsScreen(
    onMenuClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onAccountsClick: () -> Unit = {},
    onLobbyClick: () -> Unit = {},
    onConnectClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
            AppTopBar(onMenuClick = onMenuClick)
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 18.dp),
            ) {
                Text("SETTINGS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text("Settings", modifier = Modifier.padding(top = 3.dp), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.padding(top = 18.dp))
                Surface(onClick = onProfileClick, modifier = Modifier.fillMaxWidth(), shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Manage your email and mobile number", modifier = Modifier.padding(top = 4.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            AppBottomNav(AppTab.SETTINGS, onAccountsClick, onLobbyClick, onConnectClick, onSettingsClick)
        }
    }
}
