package com.indoone.settings.profile.change_email

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.indoone.menu.AppBottomNav
import com.indoone.menu.AppTab
import com.indoone.menu.AppTopBar
import com.indoone.settings.profile.ProfileState

@Composable
fun ChangeEmailScreen(
    state: ProfileState,
    onMenuClick: () -> Unit = {},
    onBack: () -> Unit,
    onSave: (String, String) -> Unit,
    onAccountsClick: () -> Unit,
    onLobbyClick: () -> Unit,
    onConnectClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    var email by remember(state.email) { mutableStateOf(state.email.takeUnless { it.contains("not available", true) }.orEmpty()) }
    var password by remember { mutableStateOf("") }
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize()) {
            AppTopBar(onMenuClick = onMenuClick)
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).imePadding().padding(18.dp)) {
                Text(
                    "← Back to Profile",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 18.dp)
                        .clickable(onClick = onBack),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text("Change email", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Change the email used for your Indoone account. Enter your current password to confirm.", modifier = Modifier.padding(top = 5.dp, bottom = 18.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), label = { Text("Email address") }, placeholder = { Text("you@example.com") }, singleLine = true)
                Spacer(Modifier.padding(top = 12.dp))
                OutlinedTextField(password, { password = it }, Modifier.fillMaxWidth(), label = { Text("Current password") }, placeholder = { Text("Enter your current password") }, visualTransformation = PasswordVisualTransformation(), singleLine = true)
                Spacer(Modifier.padding(top = 16.dp))
                Button(onClick = { onSave(email, password) }, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) { Text(if (state.busy) "Changing…" else "Change email") }
                state.error?.let { Text(it, Modifier.padding(top = 12.dp), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                state.message?.let { Text(it, Modifier.padding(top = 12.dp), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall) }
            }
            AppBottomNav(AppTab.SETTINGS, onAccountsClick, onLobbyClick, onConnectClick, onSettingsClick)
        }
    }
}
