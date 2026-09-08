package com.indoone.menu.security

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.indoone.menu.AppBottomNav
import com.indoone.menu.AppTab
import com.indoone.menu.AppTopBar

@Composable
fun SecurityScreen(
    onBack: () -> Unit,
    onAccountsClick: () -> Unit,
    onLobbyClick: () -> Unit,
    onConnectClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppTopBar(onMenuClick = onBack)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 18.dp),
            ) {
                Text(
                    text = "SECURITY",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Security",
                    modifier = Modifier.padding(top = 3.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                SecurityCard(
                    "Account creation",
                    "When creating an account, the user provides their email or mobile number, password, and OTP. The OTP is securely verified through the Indoverification system. After successful verification, the account is created securely in Firebase.",
                )
                SecurityCard(
                    "Login",
                    "During login, the user enters their registered email or mobile number, password, and OTP to authenticate their existing account.",
                )
                SecurityCard(
                    "App protection",
                    "The app features auto-lock, app lock, and biometric authentication. Once enabled, these security features help protect the app.",
                )
                SecurityCard(
                    "Screenshot protection",
                    "The Accounts page prevents screenshots to help protect account information.",
                )
            }
            AppBottomNav(
                activeTab = AppTab.SETTINGS,
                onAccountsClick = onAccountsClick,
                onLobbyClick = onLobbyClick,
                onConnectClick = onConnectClick,
                onSettingsClick = onSettingsClick,
            )
        }
    }
}

@Composable
private fun SecurityCard(title: String, body: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                body,
                modifier = Modifier.padding(top = 7.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
