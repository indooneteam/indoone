package com.indoone.settings.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.indoone.menu.AppBottomNav
import com.indoone.menu.AppTab
import com.indoone.menu.AppTopBar

@Composable
fun ProfileScreen(
    state: ProfileState,
    onMenuClick: () -> Unit = {},
    onBack: () -> Unit,
    onMobileClick: () -> Unit,
    onEmailClick: () -> Unit,
    onAccountsClick: () -> Unit,
    onLobbyClick: () -> Unit,
    onConnectClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize()) {
            AppTopBar(onMenuClick = onMenuClick)
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 18.dp),
            ) {
                Text("Profile", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    "Manage your account details.",
                    modifier = Modifier.padding(top = 4.dp, bottom = 18.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                            Text(
                                state.email.firstOrNull()?.uppercase() ?: "A",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Column(Modifier.padding(start = 14.dp)) {
                            Text(state.email, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(state.mobile, modifier = Modifier.padding(top = 3.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Spacer(Modifier.padding(top = 16.dp))
                ProfileActionRow("Change mobile number", "Update your verified phone number", onMobileClick)
                Spacer(Modifier.padding(top = 10.dp))
                ProfileActionRow("Change email", "Update your account email address", onEmailClick)
                state.error?.let { Text(it, modifier = Modifier.padding(top = 14.dp), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                state.message?.let { Text(it, modifier = Modifier.padding(top = 14.dp), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall) }
                Spacer(Modifier.padding(top = 18.dp))
                Surface(shape = RoundedCornerShape(14.dp), onClick = onBack, color = MaterialTheme.colorScheme.surfaceVariant) {
                    Text("← Back to Settings", modifier = Modifier.fillMaxWidth().padding(14.dp), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
            }
            AppBottomNav(AppTab.SETTINGS, onAccountsClick, onLobbyClick, onConnectClick, onSettingsClick)
        }
    }
}

@Composable
private fun ProfileActionRow(title: String, description: String, onClick: () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), onClick = onClick, color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(Modifier.fillMaxWidth().padding(15.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text(description, modifier = Modifier.padding(top = 3.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("›", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
