package com.indoone.settings.about

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class AboutSection(val title: String, val description: String)

@Composable
fun AboutScreen(
    onBack: () -> Unit,
    onAccountsClick: () -> Unit,
    onLobbyClick: () -> Unit,
    onConnectClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    val sections = listOf(
        AboutSection("About Indoone", "Securely generate and manage time-based one-time passwords for your accounts."),
        AboutSection("Sync", "Secure cloud sync across your signed-in devices"),
        AboutSection("OTP Standard", "TOTP • 6/8 digits • SHA-1 / SHA-256 / SHA-512"),
        AboutSection("Account Storage", "Cloud synced with your Indoone account"),
        AboutSection("Legal", "Privacy Policy • Terms of Service"),
        AboutSection("Licenses", "Open Source Licenses"),
    )

    Box(Modifier.fillMaxSize().background(Color(0x8819141F)), contentAlignment = Alignment.BottomCenter) {
        Surface(
            Modifier.fillMaxWidth().fillMaxHeight(0.88f),
            color = Color.White,
            shadowElevation = 14.dp,
            shape = RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp),
        ) {
            Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 16.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("About Indoone", Modifier.weight(1f), fontSize = 21.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1A22))
                    Box(
                        Modifier.size(35.dp).background(Color(0xFFF5F2F8), RoundedCornerShape(11.dp)).clickable(onClick = onBack),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("×", fontSize = 23.sp, lineHeight = 23.sp, color = Color(0xFF5D5666))
                    }
                }
                Spacer(Modifier.height(14.dp))
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    sections.forEach { section ->
                        Surface(
                            Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFE9E5EF)),
                            shadowElevation = 1.dp,
                        ) {
                            Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                                Text(section.title, fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1A22))
                                Text(section.description, Modifier.padding(top = 3.dp), fontSize = 10.sp, lineHeight = 15.sp, color = Color(0xFF8A8492))
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
