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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indoone.menu.AppBottomNav
import com.indoone.menu.AppTab

@Composable
fun ConnectScreen(
    state: ConnectState = ConnectState(),
    onMenuClick: () -> Unit = {},
    onAccountsClick: () -> Unit = {},
    onLobbyClick: () -> Unit = {},
    onConnectClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
) {
    var selectedIntegration by remember { mutableStateOf<String?>(null) }

    selectedIntegration?.let { integration ->
        AlertDialog(
            onDismissRequest = { selectedIntegration = null },
            title = { Text(integration) },
            text = {
                Text(
                    "Connect this service to Indoone AI to receive requests, fetch permitted data and send replies back to the same channel.",
                )
            },
            confirmButton = {
                TextButton(onClick = { selectedIntegration = null }) {
                    Text("SETUP")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedIntegration = null }) {
                    Text("CANCEL")
                }
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
                    Column(modifier = Modifier.padding(top = 6.dp, bottom = 10.dp)) {
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
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        SummaryCard(
                            title = "Balance",
                            value = state.balance,
                            modifier = Modifier.weight(1f),
                        )
                        SummaryCard(
                            title = "Usage",
                            value = "${state.usage} / ${state.usageLimit}",
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                item {
                    Text(
                        text = "BUSINESS CONNECTIONS",
                        modifier = Modifier.padding(top = 10.dp, bottom = 2.dp),
                        color = Color(0xFF6F6876),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(11.dp),
                    ) {
                        IntegrationCard(
                            title = "WhatsApp",
                            description = "Customer messages & automated replies",
                            icon = "WA",
                            modifier = Modifier.weight(1f),
                            onClick = { selectedIntegration = "WhatsApp" },
                        )
                        IntegrationCard(
                            title = "Instagram",
                            description = "DM automation & support",
                            icon = "IG",
                            modifier = Modifier.weight(1f),
                            onClick = { selectedIntegration = "Instagram" },
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(11.dp),
                    ) {
                        IntegrationCard(
                            title = "App",
                            description = "Connect your app backend",
                            icon = "AP",
                            modifier = Modifier.weight(1f),
                            onClick = { selectedIntegration = "App" },
                        )
                        IntegrationCard(
                            title = "Website",
                            description = "AI support for your website",
                            icon = "WEB",
                            modifier = Modifier.weight(1f),
                            onClick = { selectedIntegration = "Website" },
                        )
                    }
                }

                item {
                    IntegrationCard(
                        title = "Telegram",
                        description = "Connect Telegram support and automation",
                        icon = "TG",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { selectedIntegration = "Telegram" },
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
private fun SummaryCard(
    title: String,
    value: String,
    modifier: Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFBFAFD),
        border = BorderStroke(1.dp, Color(0xFFE8E5EE)),
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp)) {
            Text(
                text = title,
                color = Color(0xFF77717E),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = value,
                modifier = Modifier.padding(top = 4.dp),
                color = Color(0xFF2C2830),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun IntegrationCard(
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
        shadowElevation = 4.dp,
        onClick = onClick,
    ) {
        Column(modifier = Modifier.padding(horizontal = 13.dp, vertical = 14.dp)) {
            Box(
                modifier = Modifier.size(34.dp),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF2EFF9),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = icon,
                            color = Color(0xFF5E2DD2),
                            fontSize = if (icon.length > 2) 8.sp else 10.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
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
                text = "CONNECT",
                fontSize = 9.sp,
                lineHeight = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.35.sp,
                color = Color(0xFF6B34DF),
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
                Text(
                    text = "☰",
                    color = Color(0xFF242129),
                    fontSize = 21.sp,
                )
            }

            Text(
                text = "Indoone",
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xFF5E2DD2),
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
            )

            TextButton(
                onClick = {},
                modifier = Modifier.align(Alignment.CenterEnd),
                contentPadding = PaddingValues(8.dp),
            ) {
                Text(
                    text = "⌕",
                    color = Color(0xFF242129),
                    fontSize = 24.sp,
                )
            }
        }
        HorizontalDivider(color = Color(0xFFF0EEF5))
    }
}
