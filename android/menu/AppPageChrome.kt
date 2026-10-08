package com.indoone.menu

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
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

private val AccountsNavIcon = iconBuilder("HomeNav") {
    moveTo(4f, 10.5f); lineTo(12f, 4f); lineTo(20f, 10.5f)
    moveTo(6f, 9f); lineTo(6f, 19f); lineTo(18f, 19f); lineTo(18f, 9f)
    moveTo(9f, 19f); lineTo(9f, 14f); lineTo(15f, 14f); lineTo(15f, 19f)
    moveTo(8f, 7.5f); lineTo(10f, 7.5f); moveTo(14f, 7.5f); lineTo(16f, 7.5f)
}
private val LobbyNavIcon = iconBuilder("LobbyNav") {
    moveTo(12f, 4f); lineTo(15.8f, 8.2f); lineTo(15.2f, 14f); lineTo(12f, 19f); lineTo(8.8f, 14f); lineTo(8.2f, 8.2f); close()
    moveTo(12f, 9f); curveTo(10.895f, 9f, 10f, 9.895f, 10f, 11f); curveTo(10f, 12.105f, 10.895f, 13f, 12f, 13f); curveTo(13.105f, 13f, 14f, 12.105f, 14f, 11f); curveTo(14f, 9.895f, 13.105f, 9f, 12f, 9f)
    moveTo(12f, 13f); lineTo(12f, 15f)
}
private val ConnectNavIcon = iconBuilder("ConnectNav") {
    moveTo(8.2f, 6.2f); lineTo(5f, 9.4f); curveTo(3.343f, 11.057f, 3.343f, 13.743f, 5f, 15.4f); lineTo(6.4f, 16.8f); curveTo(8.057f, 18.457f, 10.743f, 18.457f, 12.4f, 16.8f); lineTo(15.6f, 13.6f)
    moveTo(15.8f, 17.8f); lineTo(19f, 14.6f); curveTo(20.657f, 12.943f, 20.657f, 10.257f, 19f, 8.6f); lineTo(17.6f, 7.2f); curveTo(15.943f, 5.543f, 13.257f, 5.543f, 11.6f, 7.2f); lineTo(8.4f, 10.4f)
    moveTo(9.5f, 14.5f); lineTo(14.5f, 9.5f)
}
private val SettingsNavIcon = iconBuilder("SettingsNav") {
    moveTo(5f, 5f); lineTo(10f, 5f); lineTo(10f, 10f); lineTo(5f, 10f); close()
    moveTo(14f, 5f); lineTo(19f, 5f); lineTo(19f, 10f); lineTo(14f, 10f); close()
    moveTo(5f, 14f); lineTo(10f, 14f); lineTo(10f, 19f); lineTo(5f, 19f); close()
    moveTo(14f, 15.5f); lineTo(19f, 15.5f); moveTo(14f, 18.5f); lineTo(19f, 18.5f)
}
val AppSearchIcon = iconBuilder("Search") {
    moveTo(11f, 17.5f); curveTo(7.41f, 17.5f, 4.5f, 14.59f, 4.5f, 11f); curveTo(4.5f, 7.41f, 7.41f, 4.5f, 11f, 4.5f); curveTo(14.59f, 4.5f, 17.5f, 7.41f, 17.5f, 11f); curveTo(17.5f, 14.59f, 14.59f, 17.5f, 11f, 17.5f)
    moveTo(16f, 16f); lineTo(20f, 20f)
}
private val MenuIcon = iconBuilder("Menu") {
    moveTo(4f, 7f); lineTo(20f, 7f); moveTo(4f, 12f); lineTo(20f, 12f); moveTo(4f, 17f); lineTo(20f, 17f)
}

@Composable
private fun IndooneTopBarLogo(modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val scaleFactor = size.minDimension / 48f
        scale(scaleFactor) {
            rotate(
                45f,
                pivot = androidx.compose.ui.geometry.Offset(24f, 24f),
            ) {
                drawRoundRect(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(
                            androidx.compose.ui.graphics.Color(0xFFC15CFF),
                            androidx.compose.ui.graphics.Color(0xFF7C3AED),
                            androidx.compose.ui.graphics.Color(0xFF22C7FF),
                        ),
                        start = androidx.compose.ui.geometry.Offset(11f, 11f),
                        end = androidx.compose.ui.geometry.Offset(37f, 37f),
                    ),
                    topLeft = androidx.compose.ui.geometry.Offset(11f, 11f),
                    size = androidx.compose.ui.geometry.Size(26f, 26f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
                )
            }
            val outer = androidx.compose.ui.graphics.Path().apply {
                moveTo(24f, 14f); lineTo(27.2f, 20.8f); lineTo(34f, 24f); lineTo(27.2f, 27.2f)
                lineTo(24f, 34f); lineTo(20.8f, 27.2f); lineTo(14f, 24f); lineTo(20.8f, 20.8f); close()
            }
            drawPath(outer, color = androidx.compose.ui.graphics.Color(0xFF0A0A18))
            val inner = androidx.compose.ui.graphics.Path().apply {
                moveTo(24f, 20.8f); lineTo(25.2f, 22.8f); lineTo(27.2f, 24f); lineTo(25.2f, 25.2f)
                lineTo(24f, 27.2f); lineTo(22.8f, 25.2f); lineTo(20.8f, 24f); lineTo(22.8f, 22.8f); close()
            }
            drawPath(inner, color = androidx.compose.ui.graphics.Color(0xFF60A5FA))
        }
    }
}

@Composable
fun AppTopBar(
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit = {},
    trailingIcon: String? = null,
    onTrailingClick: () -> Unit = {},
    hasUnreadNotifications: Boolean = false,
    onNotificationsClick: (() -> Unit)? = null,
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
                IndooneTopBarLogo(
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

            if (onNotificationsClick != null) {
                IconButton(
                    onClick = onNotificationsClick,
                    modifier = Modifier.align(Alignment.CenterEnd),
                ) {
                    Icon(
                        imageVector = if (hasUnreadNotifications) {
                            Icons.Filled.Notifications
                        } else {
                            Icons.Outlined.NotificationsNone
                        },
                        contentDescription = "Notifications",
                        modifier = Modifier.size(22.dp),
                        tint = Color(0xFF242129),
                    )
                }
            } else {
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
            AppBottomNavItem(AccountsNavIcon, "Home", activeTab == AppTab.HOME || activeTab == AppTab.ACCOUNTS, onAccountsClick)
            AppBottomNavItem(LobbyNavIcon, "Lobby", activeTab == AppTab.LOBBY, onLobbyClick)
            AppBottomNavItem(ConnectNavIcon, "Connect", activeTab == AppTab.CONNECT, onConnectClick)
            AppBottomNavItem(SettingsNavIcon, "Settings", activeTab == AppTab.SETTINGS, onSettingsClick)
        }
    }
}

enum class AppTab { HOME, ACCOUNTS, LOBBY, CONNECT, SETTINGS }

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