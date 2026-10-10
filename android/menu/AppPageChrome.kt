package com.indoone.menu

import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.background
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

private fun filledIconBuilder(name: String, content: androidx.compose.ui.graphics.vector.PathBuilder.() -> Unit): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            stroke = null,
            pathBuilder = content,
        )
    }.build()

private fun mixedIconBuilder(name: String, content: androidx.compose.ui.graphics.vector.PathBuilder.() -> Unit): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.8f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
            strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round,
            pathBuilder = content,
        )
    }.build()

private val AccountsNavIcon = filledIconBuilder("HomeNav") {
    moveTo(3f, 10.5f); lineTo(12f, 3.5f); lineTo(21f, 10.5f)
    lineTo(19f, 10.5f); lineTo(19f, 20f); lineTo(14f, 20f)
    lineTo(14f, 14.5f); lineTo(10f, 14.5f); lineTo(10f, 20f)
    lineTo(5f, 20f); lineTo(5f, 10.5f); close()
}

private val LobbyNavIcon = filledIconBuilder("LobbyNav") {
    moveTo(12f, 2.8f); lineTo(14.5f, 9.5f); lineTo(21.2f, 12f)
    lineTo(14.5f, 14.5f); lineTo(12f, 21.2f); lineTo(9.5f, 14.5f)
    lineTo(2.8f, 12f); lineTo(9.5f, 9.5f); close()
}

private val ConnectNavIcon = mixedIconBuilder("ConnectNav") {
    moveTo(7f, 7f); lineTo(17f, 17f)
    moveTo(17f, 7f); lineTo(7f, 17f)
    moveTo(7f, 7f); curveTo(8.657f, 7f, 10f, 8.343f, 10f, 10f)
    curveTo(10f, 11.657f, 8.657f, 13f, 7f, 13f)
    curveTo(5.343f, 13f, 4f, 11.657f, 4f, 10f)
    curveTo(4f, 8.343f, 5.343f, 7f, 7f, 7f)
    moveTo(17f, 11f); curveTo(18.657f, 11f, 20f, 12.343f, 20f, 14f)
    curveTo(20f, 15.657f, 18.657f, 17f, 17f, 17f)
    curveTo(15.343f, 17f, 14f, 15.657f, 14f, 14f)
    curveTo(14f, 12.343f, 15.343f, 11f, 17f, 11f)
}

private val SettingsNavIcon = mixedIconBuilder("SettingsNav") {
    moveTo(5f, 6.5f); lineTo(19f, 6.5f)
    moveTo(5f, 12f); lineTo(19f, 12f)
    moveTo(5f, 17.5f); lineTo(19f, 17.5f)
    moveTo(9f, 6.5f); curveTo(9f, 7.605f, 8.105f, 8.5f, 7f, 8.5f)
    curveTo(5.895f, 8.5f, 5f, 7.605f, 5f, 6.5f)
    curveTo(5f, 5.395f, 5.895f, 4.5f, 7f, 4.5f)
    curveTo(8.105f, 4.5f, 9f, 5.395f, 9f, 6.5f)
    moveTo(16f, 12f); curveTo(16f, 13.105f, 15.105f, 14f, 14f, 14f)
    curveTo(12.895f, 14f, 12f, 13.105f, 12f, 12f)
    curveTo(12f, 10.895f, 12.895f, 10f, 14f, 10f)
    curveTo(15.105f, 10f, 16f, 10.895f, 16f, 12f)
    moveTo(19f, 17.5f); curveTo(19f, 18.605f, 18.105f, 19.5f, 17f, 19.5f)
    curveTo(15.895f, 19.5f, 15f, 18.605f, 15f, 17.5f)
    curveTo(15f, 16.395f, 15.895f, 15.5f, 17f, 15.5f)
    curveTo(18.105f, 15.5f, 19f, 16.395f, 19f, 17.5f)
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
    val shell = RoundedCornerShape(30.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 7.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 18.dp, shape = shell, clip = false)
                .clip(shell)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFF2F5FF),
                            Color(0xFFFFFCFF),
                            Color(0xFFEAF0FF),
                        ),
                    ),
                )
                .border(
                    BorderStroke(1.dp, Color.White.copy(alpha = 0.95f)),
                    shape = shell,
                )
                .padding(horizontal = 6.dp, vertical = 6.dp)
                .height(70.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FloatingNavItem(
                icon = Icons.Outlined.Groups,
                label = "Lobby",
                active = activeTab == AppTab.LOBBY,
                onClick = onLobbyClick,
            )
            FloatingNavItem(
                icon = Icons.Outlined.Link,
                label = "Connect",
                active = activeTab == AppTab.CONNECT,
                onClick = onConnectClick,
            )
            FloatingNavItem(
                icon = Icons.Filled.Home,
                label = "Home",
                active = activeTab == AppTab.HOME || activeTab == AppTab.ACCOUNTS,
                onClick = onAccountsClick,
                isHome = true,
            )
            FloatingNavItem(
                icon = Icons.Outlined.Settings,
                label = "Settings",
                active = activeTab == AppTab.SETTINGS,
                onClick = onSettingsClick,
            )
        }
    }
}

enum class AppTab { HOME, ACCOUNTS, LOBBY, CONNECT, SETTINGS }

@Composable
private fun RowScope.FloatingNavItem(
    icon: ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    isHome: Boolean = false,
) {
    val tileShape = RoundedCornerShape(if (isHome) 22.dp else 18.dp)
    val homeSelected = isHome && active
    val tileBrush = when {
        homeSelected -> Brush.linearGradient(
            colors = listOf(Color(0xFF4388FF), Color(0xFF2455F5)),
        )
        isHome -> Brush.linearGradient(
            colors = listOf(Color.White, Color(0xFFEAF0FF)),
        )
        active -> Brush.linearGradient(
            colors = listOf(Color.White, Color(0xFFF9F7FF)),
        )
        else -> Brush.linearGradient(
            colors = listOf(Color.Transparent, Color.Transparent),
        )
    }
    val contentColor = when {
        homeSelected -> Color.White
        active -> Color(0xFF6537D8)
        isHome -> Color(0xFF2E63ED)
        else -> Color(0xFF777D91)
    }

    Box(
        modifier = Modifier
            .weight(1f)
            .height(64.dp)
            .padding(horizontal = 2.dp)
            .shadow(
                elevation = when {
                    homeSelected -> 10.dp
                    isHome -> 4.dp
                    active -> 3.dp
                    else -> 0.dp
                },
                shape = tileShape,
                clip = false,
            )
            .clip(tileShape)
            .background(tileBrush)
            .then(
                when {
                    homeSelected -> Modifier.border(
                        BorderStroke(1.dp, Color.White.copy(alpha = 0.38f)),
                        tileShape,
                    )
                    active || isHome -> Modifier.border(
                        BorderStroke(1.dp, Color(0xFFDCE4FF)),
                        tileShape,
                    )
                    else -> Modifier
                },
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(if (isHome) 25.dp else 22.dp),
                tint = contentColor,
            )
            Text(
                text = label,
                color = contentColor,
                fontSize = 10.sp,
                fontWeight = if (active || isHome) FontWeight.Bold else FontWeight.SemiBold,
                lineHeight = 12.sp,
                maxLines = 1,
            )
        }
    }
}
