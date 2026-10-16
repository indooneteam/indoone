package com.indoone.lobby

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.indoone.R
import com.indoone.menu.AppBottomNav
import com.indoone.menu.AppTab
import com.indoone.menu.AppTopBar
import com.indoone.menu.plugins.email.gmail.GmailPluginApi
import com.indoone.menu.plugins.github.GitHubPluginApi
import com.indoone.menu.plugins.zoho_mail.ZohoMailPluginApi

private val LobbyInk = Color(0xFF211D27)
private val LobbyMuted = Color(0xFF77707F)
private val LobbyPurple = Color(0xFF5E2DD2)
private val LobbyCardBorder = Color(0xFFECE7F8)

/**
 * One plugin as the Lobby shows it: the plugin's own logo, its name, and what it
 * brings. The key matches the plugin's connection record so the Lobby can tell
 * whether it is connected.
 */
private data class LobbyPlugin(
    val key: String,
    val title: String,
    val subtitle: String,
    val logoRes: Int,
)

private val LOBBY_PLUGINS = listOf(
    LobbyPlugin(
        key = "gmail",
        title = "Gmail",
        subtitle = "Email, drafts and sending",
        logoRes = R.drawable.gmail_logo,
    ),
    LobbyPlugin(
        key = "zoho_mail",
        title = "Zoho Mail",
        subtitle = "Mailbox, folders and sending",
        logoRes = R.drawable.zoho_mail_logo,
    ),
    LobbyPlugin(
        key = "github",
        title = "GitHub",
        subtitle = "Repositories, issues and pull requests",
        logoRes = R.drawable.github_logo,
    ),
)

@Composable
fun LobbyScreen(
    state: LobbyState = LobbyState(),
    onMenuClick: () -> Unit = {},
    onAccountsClick: () -> Unit = {},
    onLobbyClick: () -> Unit = {},
    onVibeClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onPluginsClick: () -> Unit = {},
    onPluginOpen: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val userKey = remember {
        FirebaseAuth.getInstance().currentUser?.uid?.takeIf { it.isNotBlank() } ?: ""
    }
    var connectedKeys by remember { mutableStateOf<Set<String>>(emptySet()) }
    var orderedConnected by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(userKey) {
        // Each plugin reports its own connection. A failure just means "not
        // connected" here, so one broken provider cannot empty the whole Lobby.
        val status = linkedMapOf(
            "gmail" to runCatching { GmailPluginApi.getConnectionStatus().connected }
                .getOrDefault(false),
            "zoho_mail" to runCatching { ZohoMailPluginApi.getConnectionStatus().connected }
                .getOrDefault(false),
            "github" to runCatching { GitHubPluginApi.getConnectionStatus().connected }
                .getOrDefault(false),
        )

        connectedKeys = status.filterValues { it }.keys
        orderedConnected = LobbyPluginOrderStore(context).reconcile(userKey, status)
    }

    val connectedPlugins = orderedConnected.mapNotNull { key ->
        LOBBY_PLUGINS.firstOrNull { it.key == key }
    }
    val unconnectedPlugins = LOBBY_PLUGINS.filter { it.key !in connectedKeys }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppTopBar(
                onMenuClick = onMenuClick,
                trailingIcon = "+",
                onTrailingClick = onPluginsClick,
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "PLUGINS",
                    color = LobbyPurple,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.3.sp,
                )
                Text(
                    text = "Your connected plugins",
                    color = LobbyInk,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )

                if (connectedPlugins.isNotEmpty()) {
                    PluginGrid(
                        plugins = connectedPlugins,
                        connected = true,
                        onOpen = onPluginOpen,
                    )
                }

                FlowerDivider()

                Text(
                    text = "Add another plugin",
                    color = LobbyInk,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )

                PluginGrid(
                    plugins = unconnectedPlugins,
                    connected = false,
                    onOpen = { onPluginsClick() },
                )

                if (connectedPlugins.isEmpty() && unconnectedPlugins.isEmpty()) {
                    Text(
                        text = state.message,
                        color = LobbyMuted,
                        fontSize = 13.sp,
                    )
                }
            }

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

/**
 * Two boxes to a line: the first plugin on the left, the second on the right. A
 * line with one plugin leaves the right half empty rather than stretching it.
 */
@Composable
private fun PluginGrid(
    plugins: List<LobbyPlugin>,
    connected: Boolean,
    onOpen: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        plugins.chunked(2).forEach { rowPlugins ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                rowPlugins.forEach { plugin ->
                    PluginBox(
                        plugin = plugin,
                        connected = connected,
                        modifier = Modifier.weight(1f),
                        onOpen = onOpen,
                    )
                }
                if (rowPlugins.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun PluginBox(
    plugin: LobbyPlugin,
    connected: Boolean,
    modifier: Modifier = Modifier,
    onOpen: (String) -> Unit,
) {
    Surface(
        modifier = modifier
            .height(116.dp)
            .clickable { onOpen(plugin.key) },
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, LobbyCardBorder),
        shadowElevation = if (connected) 10.dp else 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(plugin.logoRes),
                    contentDescription = plugin.title + " logo",
                    modifier = Modifier.size(30.dp),
                )
                if (connected) {
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .background(LobbyPurple, CircleShape),
                    )
                }
            }
            Column {
                Text(
                    text = plugin.title,
                    color = LobbyInk,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = if (connected) "Connected" else "Not connected",
                    modifier = Modifier.padding(top = 2.dp),
                    color = if (connected) LobbyPurple else LobbyMuted,
                    fontSize = 11.sp,
                )
            }
        }
    }
}

/**
 * The green line with a flower in the middle that separates the connected plugins
 * above from the ones still to connect below.
 */
@Composable
private fun FlowerDivider() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(2.dp)
                .background(LobbyPurple, RoundedCornerShape(1.dp)),
        )
        Canvas(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .size(22.dp),
        ) {
            drawFlower(LobbyPurple)
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .height(2.dp)
                .background(LobbyPurple, RoundedCornerShape(1.dp)),
        )
    }
}

private fun DrawScope.drawFlower(color: Color) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val petalRadius = size.minDimension * 0.16f
    val petalDistance = size.minDimension * 0.25f

    for (index in 0 until 5) {
        val angle = Math.toRadians((index * 72).toDouble())
        val petalCenter = Offset(
            center.x + (petalDistance * kotlin.math.cos(angle)).toFloat(),
            center.y + (petalDistance * kotlin.math.sin(angle)).toFloat(),
        )
        drawCircle(color = color, radius = petalRadius, center = petalCenter)
    }

    drawCircle(color = Color.White, radius = petalRadius * 0.85f, center = center)
}
