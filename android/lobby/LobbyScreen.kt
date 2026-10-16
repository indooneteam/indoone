package com.indoone.lobby

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.indoone.menu.AppBottomNav
import com.indoone.menu.AppTab
import com.indoone.menu.AppTopBar

@Composable
fun LobbyScreen(
    state: LobbyState = LobbyState(),
    onMenuClick: () -> Unit = {},
    onAccountsClick: () -> Unit = {},
    onLobbyClick: () -> Unit = {},
    onVibeClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppTopBar(onMenuClick = onMenuClick)

            // The Lobby body is deliberately empty for now. What goes here is being
            // decided next, so nothing is shown in the meantime.
            Spacer(modifier = Modifier.weight(1f))

            AppBottomNav(
                activeTab = AppTab.LOBBY,
                onAccountsClick = onAccountsClick,
                onLobbyClick = onLobbyClick,
                onVibeClick = onVibeClick,
                onSettingsClick = onSettingsClick,
            )
        }
    }
}
