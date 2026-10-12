package com.indoone.connect

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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
    onAccountsClick: () -> Unit = {},
    onLobbyClick: () -> Unit = {},
    onConnectClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onWhatsAppClick: () -> Unit = {},
    onInstagramClick: () -> Unit = {},
) {
    var selectedIntegration by remember { mutableStateOf<String?>(null) }
    var showCreditsDialog by remember { mutableStateOf(false) }

    selectedIntegration?.let { integration ->
        AlertDialog(
            onDismissRequest = { selectedIntegration = null },
            title = { Text(integration) },
            text = {
                Text(
                    "Connect this service to Indoone AI to automate customer conversations and keep replies on the same channel.",
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

    if (showCreditsDialog) {
        AlertDialog(
            onDismissRequest = { showCreditsDialog = false },
            title = {
                Text(
                    "Add Indoone Credits",
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Prepaid balance for automation and AI usage. Choose a package to continue.",
                        color = Color(0xFF77717E),
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                    )
                    CreditOption("₹99  •  1,000 credits")
                    CreditOption("₹499 •  5,500 credits", highlighted = true)
                    CreditOption("₹999 •  12,000 credits")
                }
            },
            confirmButton = {
                Button(
                    onClick = { showCreditsDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6D3DE4),
                    ),
                ) {
                    Text("CONTINUE TO PAYMENT")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreditsDialog = false }) {
                    Text("CANCEL")
                }
            },
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFCFBFE),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppTopBar(onMenuClick = onMenuClick)

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    start = 18.dp,
                    end = 18.dp,
                    top = 12.dp,
                    bottom = 22.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Column(modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)) {
                        Text(
                            text = "BUSINESS INTEGRATIONS",
                            color = Color(0xFF6D3DE4),
                            fontSize = 9.sp,
                            lineHeight = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.7.sp,
                        )
                        Text(
                            text = "Connect a Platform",
                            modifier = Modifier.padding(top = 4.dp),
                            color = Color(0xFF221D2B),
                            fontSize = 26.sp,
                            lineHeight = 30.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Bring your customer conversations to Indoone. Connect once and automate replies across your business channels.",
                            modifier = Modifier.padding(top = 7.dp),
                            color = Color(0xFF77717E),
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                        )
                    }
                }

                item {
                    BalanceCard(
                        balance = state.balance,
                        usage = state.usage,
                        usageLimit = state.usageLimit,
                        onAddCredits = { showCreditsDialog = true },
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(11.dp),
                    ) {
                        IntegrationCard(
                            title = "WhatsApp",
                            description = "Connect your WhatsApp Business",
                            icon = "WA",
                            accent = Color(0xFF25D366),
                            modifier = Modifier.weight(1f),
                            onClick = { onWhatsAppClick() },
                        )
                        IntegrationCard(
                            title = "Instagram",
                            description = "Automate comments & DMs",
                            icon = "IG",
                            accent = Color(0xFFE1306C),
                            modifier = Modifier.weight(1f),
                            onClick = { onInstagramClick() },
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
                            description = "Connect your own app backend",
                            icon = "AP",
                            accent = Color(0xFF6D3DE4),
                            modifier = Modifier.weight(1f),
                            onClick = { selectedIntegration = "App" },
                        )
                        IntegrationCard(
                            title = "Website",
                            description = "AI support for your website",
                            icon = "WEB",
                            accent = Color(0xFF2F80ED),
                            modifier = Modifier.weight(1f),
                            onClick = { selectedIntegration = "Website" },
                        )
                    }
                }

                item {
                    IntegrationCard(
                        title = "Telegram",
                        description = "Connect your Telegram bot and automate support.",
                        icon = "TG",
                        accent = Color(0xFF229ED9),
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
private fun BalanceCard(
    balance: String,
    usage: String,
    usageLimit: String,
    onAddCredits: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        shadowElevation = 5.dp,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF6D3DE4),
                        Color(0xFF5030D7),
                    ),
                ),
                shape = RoundedCornerShape(20.dp),
            ),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 17.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            "INDOONE BALANCE",
                            color = Color(0xFFDCCEFF),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.7.sp,
                        )
                        Text(
                            balance,
                            modifier = Modifier.padding(top = 4.dp),
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "Prepaid balance for your Indoone usage",
                            modifier = Modifier.padding(top = 2.dp),
                            color = Color(0xFFEDE8FF),
                            fontSize = 10.sp,
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0x33FFFFFF),
                    ) {
                        Text(
                            "CREDITS",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            "Usage",
                            color = Color(0xFFDCCEFF),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            "$usage / $usageLimit",
                            modifier = Modifier.padding(top = 2.dp),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Button(
                        onClick = onAddCredits,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF5E35D4),
                        ),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(
                            "ADD CREDITS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CreditOption(
    text: String,
    highlighted: Boolean = false,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (highlighted) Color(0xFFF2EDFF) else Color.White,
        border = BorderStroke(
            1.dp,
            if (highlighted) Color(0xFF8D68EA) else Color(0xFFE7E2ED),
        ),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            color = Color(0xFF2F2A36),
            fontSize = 12.sp,
            fontWeight = if (highlighted) FontWeight.Bold else FontWeight.Medium,
        )
    }
}

@Composable
private fun IntegrationCard(
    title: String,
    description: String,
    icon: String,
    accent: Color,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE7E2ED)),
        shadowElevation = 3.dp,
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 15.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = accent.copy(alpha = 0.12f),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = icon,
                            color = accent,
                            fontSize = if (icon.length > 2) 8.sp else 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }
                }

                Text(
                    "›",
                    color = Color(0xFFB2ACB8),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Light,
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = title,
                fontSize = 14.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C2733),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 10.sp,
                lineHeight = 14.sp,
                color = Color(0xFF89838F),
            )
            Spacer(Modifier.height(9.dp))
            Text(
                "CONNECT",
                fontSize = 9.sp,
                lineHeight = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.35.sp,
                color = accent,
            )
        }
    }
}
