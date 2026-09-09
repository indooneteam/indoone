package com.indoone.accounts.addaccount

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indoone.menu.AppBottomNav
import com.indoone.menu.AppTab
import com.indoone.menu.AppTopBar

@Composable
fun AddAccountScreen(
    state: AddAccountState,
    onOtpUriChanged: (String) -> Unit,
    onBack: () -> Unit,
    onScanQr: () -> Unit,
    onEnterSetupKey: () -> Unit,
    onImportOtpUri: (String) -> Unit,
    onMenuClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onAccountsClick: () -> Unit = onBack,
    onLobbyClick: () -> Unit = {},
    onConnectClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        AppTopBar(onMenuClick = onMenuClick, onSearchClick = onSearchClick)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 8.dp),
        ) {
            Row(
                modifier = Modifier
                    .clickable(onClick = onBack)
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("‹", color = Color(0xFF242129), fontSize = 20.sp)
                Spacer(Modifier.width(8.dp))
                Text("Back", color = Color(0xFF242129), fontWeight = FontWeight.Bold)
            }

            Column(modifier = Modifier.padding(top = 14.dp)) {
                Text("NEW AUTHENTICATOR", color = Color(0xFF7650D8), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.3.sp)
                Text("Add Account", modifier = Modifier.padding(top = 2.dp), color = Color(0xFF17151D), fontSize = 25.sp, fontWeight = FontWeight.Bold)
                Text("Choose how you want to add your TOTP account.", modifier = Modifier.padding(top = 8.dp), color = Color(0xFF2E2A33), fontSize = 14.sp, lineHeight = 20.sp)
            }

            Spacer(Modifier.height(22.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                AddAccountOption(Modifier.weight(1f), "▦", "Scan QR Code", "Use your camera to scan a TOTP QR code.", onScanQr)
                AddAccountOption(Modifier.weight(1f), "⌨", "Enter Setup Key", "Enter the secret key and account details manually.", onEnterSetupKey)
            }

            Spacer(Modifier.height(18.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFEEE9F1), RoundedCornerShape(18.dp))
                    .background(Color(0xFFFAF9FC), RoundedCornerShape(18.dp))
                    .padding(18.dp),
            ) {
                Text("PASTE OTPAUTH URI", color = Color(0xFF66606E), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.8.sp)
                BasicTextField(
                    value = state.otpUri,
                    onValueChange = onOtpUriChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .height(48.dp)
                        .border(1.dp, Color(0xFFE6E2ED), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 13.dp),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color(0xFF17151D), fontSize = 14.sp),
                    decorationBox = { inner ->
                        if (state.otpUri.isBlank()) Text("otpauth://totp/...", color = Color(0xFF9B95A1), fontSize = 14.sp)
                        inner()
                    },
                )
                OutlinedButton(
                    onClick = { onImportOtpUri(state.otpUri) },
                    enabled = state.isImportEnabled,
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFE3DFE8)),
                    contentPadding = PaddingValues(vertical = 11.dp),
                ) {
                    Text("Import OTP URI", color = Color(0xFF5F5966), fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Spacer(Modifier.height(10.dp))
        }

        AppBottomNav(
            activeTab = AppTab.ACCOUNTS,
            onAccountsClick = onAccountsClick,
            onLobbyClick = onLobbyClick,
            onConnectClick = onConnectClick,
            onSettingsClick = onSettingsClick,
        )
    }
}

@Composable
private fun AddAccountOption(
    modifier: Modifier,
    icon: String,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .height(150.dp)
            .background(Color.White, RoundedCornerShape(18.dp))
            .border(1.dp, Color(0xFFE8E3EC), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(icon, color = Color(0xFF302A38), fontSize = 28.sp, lineHeight = 28.sp)
        Text(title, color = Color(0xFF17141B), fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Text(description, color = Color(0xFF77717F), fontSize = 12.sp, lineHeight = 18.sp)
    }
}
