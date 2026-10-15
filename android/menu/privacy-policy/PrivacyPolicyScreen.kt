package com.indoone.menu.privacypolicy

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class PrivacySection(val title: String, val body: String)

private val PrivacyPurple = Color(0xFF6330DB)
private val PrivacyInk = Color(0xFF211D27)
private val PrivacyMuted = Color(0xFF756F7F)
private val PrivacyBorder = Color(0xFFECE7F8)
private val PrivacyChip = Color(0xFFF3EDFF)

@Composable
fun PrivacyPolicyScreen(
    onBack: () -> Unit,
    onAccountsClick: () -> Unit,
    onLobbyClick: () -> Unit,
    onConnectClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    val sections = listOf(
        PrivacySection("1. Information We Use", "When you create an Indoone account, you may provide your email address, mobile number, and password. This information is used for account authentication and account management."),
        PrivacySection("2. OTP Verification", "Indoone uses OTP verification for account creation and login. Your email address and required verification information may be sent to the IndoVerification service to request and verify OTPs."),
        PrivacySection("3. Firebase", "Indoone uses Firebase services for authentication and certain account profile information. Your registered email address, mobile number, and account identifier may be stored in Firebase services as required for these features."),
        PrivacySection("4. Authenticator Accounts", "Authenticator accounts and related information added to Indoone are stored in the app's encrypted local vault. Backup and recovery features may use this information when you choose to create or restore a recovery backup."),
        PrivacySection("5. Account Changes", "Available account settings may allow you to change your registered email address and mobile number. Password change is currently not available. Forgot Password and separate account recovery functionality are currently not fully available."),
        PrivacySection("6. Trash and Deleted Accounts", "Deleted authenticator accounts may remain in Trash for up to 30 days and may be restored during that period. After the retention period, they may be permanently removed. Trash restoration is not Indoone account recovery."),
        PrivacySection("7. Backup and Recovery", "Indoone may provide encrypted recovery PDF and supported device backup features. You are responsible for keeping your recovery files, PINs, passwords, and other recovery information secure."),
        PrivacySection("8. Security", "Indoone may provide App Lock, PIN protection, biometric authentication, Auto-Lock, and screenshot protection for supported features. These measures are intended to improve security but cannot guarantee complete protection against every security risk."),
        PrivacySection("9. Third-Party Services", "Indoone may use third-party services such as Firebase and IndoVerification to provide authentication, OTP verification, and related functionality. These services may have their own privacy policies and terms."),
        PrivacySection("10. Data Sharing", "Indoone does not sell your personal information. Information may be processed by third-party services when necessary to provide authentication, OTP verification, or other supported technical functionality."),
        PrivacySection("11. User Responsibility", "You are responsible for protecting your password, PIN, authenticator information, recovery files, and device. Unauthorized access to your device or recovery information may affect the security of your data."),
        PrivacySection("12. Privacy Policy Changes", "Indoone may update this Privacy Policy when features, services, or legal requirements change. Updated information may be published within the app or through an official Indoone channel."),
        PrivacySection("13. Contact", "Email: indoone@zohomail.in"),
    )

    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 10.dp),
        ) {
            PolicyHeader(title = "Privacy Policy", onBack = onBack)

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(sections) { section ->
                    PolicySectionCard(section)
                }
            }
        }
    }

    @Suppress("UNUSED_VARIABLE")
    val keepNavigationContract = onAccountsClick to onLobbyClick to onConnectClick to onSettingsClick
}

@Composable
private fun PolicyHeader(title: String, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                )
            }
            Text(
                text = title,
                modifier = Modifier.padding(start = 4.dp),
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C2733),
            )
        }
        Surface(
            modifier = Modifier.padding(start = 6.dp),
            shape = RoundedCornerShape(9.dp),
            color = PrivacyChip,
        ) {
            Text(
                text = "Last updated 08/09/2026",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                color = PrivacyPurple,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun PolicySectionCard(section: PrivacySection) {
    val number = section.title.substringBefore(".", "").trim()
    val heading = section.title.substringAfter(". ", section.title)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, PrivacyBorder),
        shadowElevation = 1.dp,
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .background(PrivacyChip, RoundedCornerShape(9.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = number,
                    color = PrivacyPurple,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(
                    text = heading,
                    color = PrivacyInk,
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = section.body,
                    modifier = Modifier.padding(top = 4.dp),
                    color = PrivacyMuted,
                    fontSize = 12.sp,
                    lineHeight = 19.sp,
                )
            }
        }
    }
}
