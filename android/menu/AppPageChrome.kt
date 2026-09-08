package com.indoone.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppTopBar(
    onMenuClick: () -> Unit,
    trailingIcon: String? = null,
    onTrailingClick: () -> Unit = {},
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clickable(onClick = onMenuClick),
                contentAlignment = Alignment.Center,
            ) {
                Text("☰", color = Color(0xFF242129), fontSize = 22.sp)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(Color(0xFF6D35E8), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("✦", color = Color.White, fontSize = 16.sp)
                }
                Text(
                    text = "Indoone",
                    color = Color(0xFF5E2DD2),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clickable(enabled = trailingIcon != null, onClick = onTrailingClick),
                contentAlignment = Alignment.Center,
            ) {
                trailingIcon?.let { Text(it, color = Color(0xFF242129), fontSize = 24.sp) }
            }
        }
        HorizontalDivider(color = Color(0xFFF0EEF5))
    }
}

@Composable
fun AppBottomNav(
    activeTab: AppTab,
    onAccountsClick: () -> Unit,
    onLobbyClick: () -> Unit,
    onConnectClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEAF2)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(67.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppBottomNavItem("♟", "Accounts", activeTab == AppTab.ACCOUNTS, onAccountsClick)
            AppBottomNavItem("◆", "Lobby", activeTab == AppTab.LOBBY, onLobbyClick)
            AppBottomNavItem("↔", "Connect", activeTab == AppTab.CONNECT, onConnectClick)
            AppBottomNavItem("☷", "Settings", activeTab == AppTab.SETTINGS, onSettingsClick)
        }
    }
}

enum class AppTab { ACCOUNTS, LOBBY, CONNECT, SETTINGS }

@Composable
private fun RowScope.AppBottomNavItem(
    icon: String,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    val contentColor = if (active) Color(0xFF6B34DF) else Color(0xFF99939F)
    Column(
        modifier = Modifier
            .weight(1f)
            .height(67.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(icon, color = contentColor)
        Text(label, style = MaterialTheme.typography.labelSmall, color = contentColor)
    }
}
