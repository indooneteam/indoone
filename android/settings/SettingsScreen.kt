package com.indoone.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.indoone.menu.AppBottomNav
import com.indoone.menu.AppTab
import com.indoone.menu.AppTopBar

@Composable
fun SettingsScreen(
    onMenuClick: () -> Unit = {},
    onAccountsClick: () -> Unit = {},
    onLobbyClick: () -> Unit = {},
    onConnectClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
            Column(modifier = Modifier.fillMaxSize()) {
                AppTopBar(onMenuClick = onMenuClick)
                Column(
                    modifier = Modifier.weight(1f).padding(horizontal = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "SETTINGS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "Settings is under development",
                        modifier = Modifier.padding(top = 6.dp),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
                AppBottomNav(
                    activeTab = AppTab.SETTINGS,
                    onAccountsClick = onAccountsClick,
                    onLobbyClick = onLobbyClick,
                    onConnectClick = onConnectClick,
                    onSettingsClick = onSettingsClick,
                )
            }
        }
    }
}
