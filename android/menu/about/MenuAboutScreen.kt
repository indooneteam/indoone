package com.indoone.menu.about

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class MenuAboutSection(val title: String, val description: String)

@Composable
fun MenuAboutScreen(
    onBack: () -> Unit,
    onAccountsClick: () -> Unit,
    onLobbyClick: () -> Unit,
    onConnectClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    val sections = listOf(
        MenuAboutSection("About Indoone", "Securely generate and manage time-based one-time passwords for your accounts."),
        MenuAboutSection("Sync", "Secure cloud sync across your signed-in devices"),
        MenuAboutSection("OTP Standard", "TOTP • 6/8 digits • SHA-1 / SHA-256 / SHA-512"),
        MenuAboutSection("Account Storage", "Cloud synced with your Indoone account"),
        MenuAboutSection("Legal", "Privacy Policy • Terms of Service"),
        MenuAboutSection("Licenses", "Open Source Licenses"),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x5519141F))
            .clickable(onClick = onBack),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp)
                .clickable(onClick = {}),
            color = Color.White,
            shadowElevation = 14.dp,
            shape = RoundedCornerShape(25.dp),
        ) {
            Column(Modifier.padding(horizontal = 23.dp, vertical = 21.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "About Indoone",
                        modifier = Modifier.weight(1f),
                        color = Color(0xFF201C25),
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFF5F2F8), RoundedCornerShape(11.dp))
                            .clickable(onClick = onBack)
                            .padding(horizontal = 11.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("×", color = Color(0xFF5D5666), fontSize = 22.sp, lineHeight = 22.sp)
                    }
                }
                Spacer(Modifier.height(10.dp))
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    sections.forEach { section ->
                        Column(Modifier.fillMaxWidth()) {
                            HorizontalDivider(color = Color(0xFFEEE8F4))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(section.title, color = Color(0xFF24202A), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        section.description,
                                        modifier = Modifier.padding(top = 3.dp),
                                        color = Color(0xFF756F7F),
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Suppress("UNUSED_VARIABLE")
    val keepNavigationContract = onAccountsClick to onLobbyClick to onConnectClick to onSettingsClick
}
