package com.indoone.connect

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import com.indoone.menu.AppBottomNav
import com.indoone.menu.AppTab

private fun headerIconBuilder(name: String, content: androidx.compose.ui.graphics.vector.PathBuilder.() -> Unit): ImageVector =
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

private val ConnectMenuIcon = headerIconBuilder("ConnectMenu") {
    moveTo(4f, 7f); lineTo(20f, 7f); moveTo(4f, 12f); lineTo(20f, 12f); moveTo(4f, 17f); lineTo(20f, 17f)
}

private val ConnectSearchIcon = headerIconBuilder("ConnectSearch") {
    moveTo(11f, 17.5f); curveTo(7.41f, 17.5f, 4.5f, 14.59f, 4.5f, 11f); curveTo(4.5f, 7.41f, 7.41f, 4.5f, 11f, 4.5f); curveTo(14.59f, 4.5f, 17.5f, 7.41f, 17.5f, 11f); curveTo(17.5f, 14.59f, 14.59f, 17.5f, 11f, 17.5f)
    moveTo(16f, 16f); lineTo(20f, 20f)
}

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
    var action by remember { mutableStateOf<String?>(null) }

    action?.let { selectedAction ->
        AlertDialog(
            onDismissRequest = { action = null },
            title = { Text(selectedAction) },
            text = {
                Text(
                    when (selectedAction) {
                        "Pair" -> "Pair another Indoone device."
                        "Connect" -> "Choose a nearby Indoone device to connect."
                        else -> "Manage connected Indoone devices."
                    },
                )
            },
            confirmButton = {
                TextButton(onClick = { action = null }) { Text("OK") }
            },
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
        ) {
            ConnectTopBar(onMenuClick = onMenuClick)

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Column(modifier = Modifier.padding(top = 6.dp, bottom = 18.dp)) {
                        Text(
                            text = state.eyebrow,
                            color = Color(0xFF2877E8),
                            fontSize = 9.sp,
                            lineHeight = 11.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = state.title,
                            modifier = Modifier.padding(top = 3.dp),
                            color = Color(0xFF1F1B24),
                            fontSize = 26.sp,
                            lineHeight = 29.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = state.description,
                            modifier = Modifier.padding(top = 7.dp),
                            color = Color(0xFF77717E),
                            fontSize = 12.sp,
                            lineHeight = 19.sp,
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(11.dp),
                    ) {
                        ConnectActionCard(
                            title = "Pair",
                            description = "Pair another Indoone device",
                            icon = "⌁",
                            modifier = Modifier.weight(1f),
                            onClick = { onPair(); action = "Pair" },
                        )
                        ConnectActionCard(
                            title = "Connect",
                            description = "Choose a nearby device",
                            icon = "↔",
                            modifier = Modifier.weight(1f),
                            onClick = { onConnect(); action = "Connect" },
                        )
                    }
                }

                item {
                    ConnectActionCard(
                        title = "Devices",
                        description = "Manage connected devices",
                        icon = "▣",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onDevices(); action = "Devices" },
                    )
                }
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

@Composable
private fun ConnectTopBar(
    onMenuClick: () -> Unit,
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
                    ConnectMenuIcon,
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
                ConnectIndooneLogo(modifier = Modifier.size(34.dp))
                Text(
                    "Indoone",
                    color = Color(0xFF5E2DD2),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 18.sp,
                )
            }

            TextButton(
                onClick = {},
                modifier = Modifier.align(Alignment.CenterEnd),
                contentPadding = PaddingValues(8.dp),
            ) {
                Icon(
                    ConnectSearchIcon,
                    contentDescription = "Search accounts",
                    modifier = Modifier.size(21.dp),
                    tint = Color(0xFF242129),
                )
            }
        }
        HorizontalDivider(color = Color(0xFFF0EEF5))
    }
}

@Composable
private fun ConnectIndooneLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val scaleFactor = size.minDimension / 48f
        scale(scaleFactor) {
            rotate(45f, pivot = androidx.compose.ui.geometry.Offset(24f, 24f)) {
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFC15CFF), Color(0xFF7C3AED), Color(0xFF22C7FF)),
                        start = androidx.compose.ui.geometry.Offset(11f, 11f),
                        end = androidx.compose.ui.geometry.Offset(37f, 37f),
                    ),
                    topLeft = androidx.compose.ui.geometry.Offset(11f, 11f),
                    size = androidx.compose.ui.geometry.Size(26f, 26f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
                )
            }
            val outer = Path().apply {
                moveTo(24f, 14f); lineTo(27.2f, 20.8f); lineTo(34f, 24f); lineTo(27.2f, 27.2f)
                lineTo(24f, 34f); lineTo(20.8f, 27.2f); lineTo(14f, 24f); lineTo(20.8f, 20.8f); close()
            }
            drawPath(outer, color = Color(0xFF0A0A18))
            val inner = Path().apply {
                moveTo(24f, 20.8f); lineTo(25.2f, 22.8f); lineTo(27.2f, 24f); lineTo(25.2f, 25.2f)
                lineTo(24f, 27.2f); lineTo(22.8f, 25.2f); lineTo(20.8f, 24f); lineTo(22.8f, 22.8f); close()
            }
            drawPath(inner, color = Color(0xFF60A5FA))
        }
    }
}

@Composable
private fun ConnectActionCard(
    title: String,
    description: String,
    icon: String,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE8E5EE)),
        shadowElevation = 5.dp,
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 16.dp),
        ) {
            Box(
                modifier = Modifier.size(23.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = icon,
                    fontSize = 20.sp,
                    color = Color(0xFF413C48),
                )
            }
            Spacer(Modifier.height(11.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C2830),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 10.sp,
                lineHeight = 14.sp,
                color = Color(0xFF89838F),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "OPEN",
                fontSize = 9.sp,
                lineHeight = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.35.sp,
                color = Color(0xFF7C7485),
            )
        }
    }
}