package com.indoone.settings.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.indoone.menu.AppBottomNav
import com.indoone.menu.AppTab
import com.indoone.menu.AppTopBar

@Composable
fun AboutScreen(
    onBack: () -> Unit,
    onAccountsClick: () -> Unit,
    onLobbyClick: () -> Unit,
    onConnectClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
            AppTopBar(onMenuClick = onBack)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("ABOUT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text("About Indoone", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text("Indoone Authenticator", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            "Private authenticator with cloud sync and secure device pairing. Updates are checked automatically when the app opens.",
                            modifier = Modifier.padding(top = 8.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                InfoRow("Version", "0.1.0")
                InfoRow("Updates", "Automatic")

                OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text("Back to Settings")
                }
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
private fun InfoRow(label: String, value: String) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, modifier = Modifier.padding(top = 2.dp), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}
