package com.indoone.menu.privacypolicy

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indoone.menu.AppBottomNav
import com.indoone.menu.AppTab
import com.indoone.menu.AppTopBar

data class PrivacySection(val title: String, val body: String)

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

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
            AppTopBar(onMenuClick = onBack)
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Text("PRIVACY POLICY", color = Color(0xFF7650D8), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.3.sp)
                    Text("Privacy Policy", modifier = Modifier.padding(top = 3.dp), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Last Updated: 08/09/2026", modifier = Modifier.padding(top = 6.dp), color = Color(0xFF8A8492), fontSize = 11.sp)
                }
                items(sections) { section ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(section.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(section.body, modifier = Modifier.padding(top = 5.dp), style = MaterialTheme.typography.bodySmall, color = Color(0xFF8A8492), lineHeight = MaterialTheme.typography.bodySmall.lineHeight)
                    }
                }
                item {
                    Surface(shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, Color(0xFFECE8F1)), color = Color(0xFFFAF9FD), modifier = Modifier.fillMaxWidth()) {
                        Text("This Privacy Policy may change as Indoone features and services evolve.", modifier = Modifier.padding(14.dp), style = MaterialTheme.typography.bodySmall, color = Color(0xFF6E6878))
                    }
                }
            }
            AppBottomNav(AppTab.SETTINGS, onAccountsClick, onLobbyClick, onConnectClick, onSettingsClick)
        }
    }
}
