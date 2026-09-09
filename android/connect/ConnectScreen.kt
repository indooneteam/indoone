package com.indoone.connect

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indoone.menu.AppBottomNav
import com.indoone.menu.AppTab
import com.indoone.menu.AppTopBar

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
        color = Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
        ) {
            AppTopBar(onMenuClick = onMenuClick)

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
                            onClick = onPair,
                        )
                        ConnectActionCard(
                            title = "Connect",
                            description = "Choose a nearby device",
                            icon = "↔",
                            modifier = Modifier.weight(1f),
                            onClick = onConnect,
                        )
                    }
                }

                item {
                    ConnectActionCard(
                        title = "Devices",
                        description = "Manage connected devices",
                        icon = "▣",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onDevices,
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
private fun ConnectActionCard(
    title: String,
    description: String,
    icon: String,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier.alpha(0.62f),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE8E5EE)),
        shadowElevation = 5.dp,
        onClick = {},
        enabled = false,
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
                text = "COMING SOON",
                fontSize = 9.sp,
                lineHeight = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.35.sp,
                color = Color(0xFF7C7485),
            )
        }
    }
}