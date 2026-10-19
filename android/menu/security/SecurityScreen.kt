package com.indoone.menu.security

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val SecurityPurple = Color(0xFF6330DB)
private val SecurityInk = Color(0xFF211D27)
private val SecurityMuted = Color(0xFF756F7F)
private val SecurityBorder = Color(0xFFECE7F8)
private val SecurityChip = Color(0xFFF3EDFF)

private data class SecurityPoint(val icon: ImageVector, val title: String, val body: String)

@Composable
fun SecurityScreen(
    onBack: () -> Unit,
    onAccountsClick: () -> Unit,
    onLobbyClick: () -> Unit,
    onVibeClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val points = listOf(
        SecurityPoint(
            Icons.Outlined.PersonOutline,
            "Creating an account",
            "You provide an email address or mobile number, a password, and an OTP. The OTP is verified through the IndoVerification system, and the account is then created securely in Firebase.",
        ),
        SecurityPoint(
            Icons.Outlined.Lock,
            "Signing in",
            "Your email or mobile number, your password, and an OTP are entered again. Signing in only opens an account that already exists.",
        ),
        SecurityPoint(
            Icons.Outlined.Security,
            "Protecting the app",
            "Indoone has Auto-Lock, App Lock, and Biometric Unlock. Once you enable them, the app locks itself when you leave it and asks for your PIN or fingerprint before it opens again.",
        ),
        SecurityPoint(
            Icons.Outlined.Visibility,
            "Screenshots",
            "The accounts page prevents screenshots, so your codes cannot be captured by another app.",
        ),
    )

    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                    )
                }
                Text(
                    text = "Security",
                    modifier = Modifier.padding(start = 4.dp),
                    color = Color(0xFF2C2733),
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                points.forEach { point ->
                    SecurityPointCard(point)
                }
            }
        }
    }

    @Suppress("UNUSED_VARIABLE")
    val keepNavigationContract = onAccountsClick to onLobbyClick to onVibeClick to onSettingsClick
}

@Composable
private fun SecurityPointCard(point: SecurityPoint) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, SecurityBorder),
        shadowElevation = 1.dp,
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(SecurityChip, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = point.icon,
                    contentDescription = null,
                    tint = SecurityPurple,
                    modifier = Modifier.size(19.dp),
                )
            }
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(
                    text = point.title,
                    color = SecurityInk,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = point.body,
                    modifier = Modifier.padding(top = 4.dp),
                    color = SecurityMuted,
                    fontSize = 12.sp,
                    lineHeight = 19.sp,
                )
            }
        }
    }
}
