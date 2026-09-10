package com.indoone.menu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

private fun iconBuilder(name: String, content: androidx.compose.ui.graphics.vector.PathBuilder.() -> Unit): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.8f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
            strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round,
            pathBuilder = content,
        )
    }.build()

private val AccountsNavIcon = iconBuilder("AccountsNav") {
    moveTo(9f, 5f); curveTo(7.343f, 5f, 6f, 6.343f, 6f, 8f); curveTo(6f, 9.657f, 7.343f, 11f, 9f, 11f); curveTo(10.657f, 11f, 12f, 9.657f, 12f, 8f); curveTo(12f, 6.343f, 10.657f, 5f, 9f, 5f)
    moveTo(4.5f, 19f); curveTo(5.1f, 16f, 6.6f, 14.5f, 9f, 14.5f); curveTo(11.4f, 14.5f, 12.9f, 16f, 13.5f, 19f)
    moveTo(16f, 6.5f); curveTo(14.619f, 6.5f, 13.5f, 7.619f, 13.5f, 9f); curveTo(13.5f, 10.381f, 14.619f, 11.5f, 16f, 11.5f); curveTo(17.381f, 11.5f, 18.5f, 10.381f, 18.5f, 9f); curveTo(18.5f, 7.619f, 17.381f, 6.5f, 16f, 6.5f)
    moveTo(13.5f, 18.5f); curveTo(14f, 16.3f, 15.2f, 15.1f, 17f, 15.1f); curveTo(18.7f, 15.1f, 19.9f, 16.3f, 20.5f, 18.5f)
}
private val LobbyNavIcon = iconBuilder("LobbyNav") {
    moveTo(12f, 3.5f); lineTo(20.5f, 12f); lineTo(12f, 20.5f); lineTo(3.5f, 12f); close()
    moveTo(12f, 9.6f); curveTo(10.675f, 9.6f, 9.6f, 10.675f, 9.6f, 12f); curveTo(9.6f, 13.325f, 10.675f, 14.4f, 12f, 14.4f); curveTo(13.325f, 14.4f, 14.4f, 13.325f, 14.4f, 12f); curveTo(14.4f, 10.675f, 13.325f, 9.6f, 12f, 9.6f)
}
private val ConnectNavIcon = iconBuilder("ConnectNav") {
    moveTo(5f, 12f); lineTo(19f, 12f); moveTo(8f, 7f); lineTo(3f, 12f); lineTo(8f, 17f); moveTo(16f, 7f); lineTo(21f, 12f); lineTo(16f, 17f)
}
private val SettingsNavIcon = iconBuilder("SettingsNav") {
    moveTo(5f, 7f); lineTo(19f, 7f); moveTo(5f, 17f); lineTo(19f, 17f)
    moveTo(10f, 7f); curveTo(10f, 8.105f, 9.105f, 9f, 8f, 9f); curveTo(6.895f, 9f, 6f, 8.105f, 6f, 7f); curveTo(6f, 5.895f, 6.895f, 5f, 8f, 5f); curveTo(9.105f, 5f, 10f, 5.895f, 10f, 7f)
    moveTo(18f, 17f); curveTo(18f, 18.105f, 17.105f, 19f, 16f, 19f); curveTo(14.895f, 19f, 14f, 18.105f, 14f, 17f); curveTo(14f, 15.895f, 14.895f, 15f, 16f, 15f); curveTo(17.105f, 15f, 18f, 15.895f, 18f, 17f)
}
val AppSearchIcon = iconBuilder("Search") {
    moveTo(11f, 17.5f); curveTo(7.41f, 17.5f, 4.5f, 14.59f, 4.5f, 11f); curveTo(4.5f, 7.41f, 7.41f, 4.5f, 11f, 4.5f); curveTo(14.59f, 4.5f, 17.5f, 7.41f, 17.5f, 11f); curveTo(17.5f, 14.59f, 14.59f, 17.5f, 11f, 17.5f)
    moveTo(16f, 16f); lineTo(20f, 20f)
}
private val MenuIcon = iconBuilder("Menu") {
    moveTo(4f, 7f); lineTo(20f, 7f); moveTo(4f, 12f); lineTo(20f, 12f); moveTo(4f, 17f); lineTo(20f, 17f)
}

@Composable
fun AppTopBar(
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit = {},
    trailingIcon: String? = null,
    onTrailingClick: () -> Unit = {},
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .padding(horizontal = 18.dp, vertical = 8.dp),
        ) {
            TextButton(
                onClick = onMenuClick,
                modifier = Modifier.align(Alignment.CenterStart),
                contentPadding = PaddingValues(8.dp),
            ) {
                Icon(
                    MenuIcon,
                    contentDescription = "Open menu",
                    modifier = Modifier.size(21.dp),
                    tint = Color(0xFF242129),
                )
            }

            Row(
                modifier = Modifier.align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                Image(
                    painter = painterResource(com.indoone.R.drawable.ic_indoone_logo),
                    contentDescription = "Indoone",
                    modifier = Modifier.size(34.dp),
                )
                Text(
                    "Indoone",
                    color = Color(0xFF5E2DD2),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 18.sp,
                )
            }

            TextButton(
                onClick = if (trailingIcon != null) onTrailingClick else onSearchClick,
                modifier = Modifier.align(Alignment.CenterEnd),
                contentPadding = PaddingValues(8.dp),
            ) {
                if (trailingIcon == null) {
                    Icon(
                        AppSearchIcon,
                        contentDescription = "Search accounts",
                        modifier = Modifier.size(21.dp),
                        tint = Color(0xFF242129),
                    )
                } else {
                    Text(trailingIcon, color = Color(0xFF242129), fontSize = 24.sp)
                }
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
        modifier = modifier.fillMaxWidth().navigationBarsPadding(),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFEEEAF2)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(67.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppBottomNavItem(AccountsNavIcon, "Home", activeTab == AppTab.HOME, onAccountsClick)
            AppBottomNavItem(LobbyNavIcon, "Lobby", activeTab == AppTab.LOBBY, onLobbyClick)
            AppBottomNavItem(ConnectNavIcon, "Connect", activeTab == AppTab.CONNECT, onConnectClick)
            AppBottomNavItem(SettingsNavIcon, "Settings", activeTab == AppTab.SETTINGS, onSettingsClick)
        }
    }
}

enum class AppTab { HOME, LOBBY, CONNECT, SETTINGS }

@Composable
private fun RowScope.AppBottomNavItem(
    icon: ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    val contentColor = if (active) Color(0xFF6B34DF) else Color(0xFF99939F)
    Box(
        modifier = Modifier.weight(1f).height(67.dp).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Icon(
                icon,
                contentDescription = label,
                modifier = Modifier.size(23.dp),
                tint = contentColor.copy(alpha = if (active) 1f else 0.78f),
            )
            Text(
                text = label,
                color = contentColor,
                fontSize = 9.sp,
                fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold,
                lineHeight = 10.sp,
            )
        }
    }
}