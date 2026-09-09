package com.indoone.menu.termsofuse

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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

data class TermsSection(
    val title: String,
    val body: String,
)

private val TERMS_SECTIONS = listOf(
    TermsSection("1. Account Creation", "When creating an Indoone account, users may provide an email address, mobile number, password, and OTP. The OTP is verified through the IndoVerification system. After successful OTP verification, the Indoone account is created in Firebase."),
    TermsSection("2. Login", "Login uses an existing Indoone account. Users may sign in with their registered email address or mobile number and password. OTP verification may also be required to complete login. Login does not create a new account."),
    TermsSection("3. Account Information", "Users may change their registered email address and mobile number through available account settings. Password change is currently not available. Forgot Password and separate account recovery functionality are currently not fully available."),
    TermsSection("4. Authenticator Accounts", "Indoone allows users to add and manage supported authenticator accounts. Supported setup methods may include QR setup, manual setup, and supported import methods. Indoone may generate and display TOTP verification codes. Users are responsible for keeping authenticator information secure."),
    TermsSection("5. Account Management", "Users may view, search, sort, favorite, and manage their authenticator accounts using available features. Users should review an action before deleting or moving an account."),
    TermsSection("6. Trash and Restore", "When an authenticator account is deleted, it may remain in Trash for up to 30 days. During this period, the user may restore the account from Trash using the available restore functionality. After the retention period, the account may be permanently removed. Trash restoration is not a password recovery or Indoone account recovery service."),
    TermsSection("7. Backup", "Available backup features may help users preserve authenticator-related information. Users are responsible for keeping backup and recovery information secure. Backup functionality does not guarantee complete recovery in every situation."),
    TermsSection("8. Security Features", "Indoone may provide App Lock, PIN protection, biometric unlock, Auto-Lock, and screenshot protection for supported sensitive screens. These features are intended to improve security but do not guarantee complete protection against every security risk."),
    TermsSection("9. Firebase and Other Services", "Indoone uses Firebase and related services for authentication and certain account-related data. Indoone may also use IndoVerification and device or operating-system capabilities for supported features. Service interruptions, network problems, maintenance, or third-party service issues may affect availability."),
    TermsSection("10. Connect and Permissions", "Supported versions of Indoone may provide nearby device connection, device pairing, permissions, and data or file transfer features. Users are responsible for reviewing devices and permissions before granting access. Users must not access, copy, or transfer another person's data without authorization."),
    TermsSection("11. User Responsibilities", "Users must protect their password, OTP, authenticator information, recovery information, and device. Indoone must not be used for unauthorized access, fraud, misuse of another person's account, illegal activity, or attempts to bypass security controls."),
    TermsSection("12. Service Availability", "Indoone is provided with the goal of reasonable reliability. However, continuous, uninterrupted, or error-free operation cannot be guaranteed. Features may temporarily become unavailable because of technical issues, network problems, maintenance, device limitations, or third-party services."),
    TermsSection("13. Data and Account Responsibility", "Users are responsible for their own account credentials, authenticator information, backups, and recovery information. Loss of a device, credentials, deleted data, failed synchronization, or unavailable third-party services may result in loss of access or data."),
    TermsSection("14. Privacy", "Indoone may process account, authentication, and technical information as necessary to provide its features. Users should review the Indoone Privacy Policy for information about data collection, use, storage, and deletion."),
    TermsSection("15. Changes to These Terms", "Indoone may update these Terms when its features, services, or legal requirements change. Updated Terms may be published within the application or through an official Indoone channel."),
    TermsSection("16. Contact", "Email: indoone@zohomail.in"),
    TermsSection("17. Acceptance", "By creating an account, logging in, or using Indoone, you acknowledge that you have read and accepted these Terms of Use. If you do not agree with these Terms, please stop using Indoone."),
)

@Composable
fun TermsOfUseScreen(
    onBack: () -> Unit,
    onAccountsClick: () -> Unit,
    onLobbyClick: () -> Unit,
    onConnectClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x8819141F)),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f),
            shape = RoundedCornerShape(25.dp),
            color = Color.White,
            shadowElevation = 14.dp,
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(23.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Terms of Use",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C2733),
                    )
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(35.dp)
                            .background(Color(0xFFF5F2F8), RoundedCornerShape(11.dp)),
                    ) {
                        Text("×", fontSize = 21.sp, color = Color(0xFF2C2733))
                    }
                }

                Text(
                    text = "Last Updated: 08/09/2026",
                    modifier = Modifier.padding(top = 1.dp, bottom = 14.dp),
                    color = Color(0xFF8A8492),
                    fontSize = 11.sp,
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(TERMS_SECTIONS) { section ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = section.title,
                                modifier = Modifier.padding(top = 4.dp),
                                fontSize = 14.sp,
                                lineHeight = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF211D27),
                            )
                            Text(
                                text = section.body,
                                modifier = Modifier.padding(top = 2.dp),
                                fontSize = 12.sp,
                                lineHeight = 19.sp,
                                color = Color(0xFF8A8492),
                            )
                        }
                    }
                }
            }
        }
    }
}