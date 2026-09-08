package com.indoone.settings.profile.change_mobile_number

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
import androidx.compose.ui.unit.dp
import com.indoone.menu.AppBottomNav
import com.indoone.menu.AppTab
import com.indoone.menu.AppTopBar
import com.indoone.settings.profile.ProfileState

@Composable
fun ChangeMobileNumberScreen(
    state: ProfileState,
    onMenuClick: () -> Unit = {},
    onBack: () -> Unit,
    onSave: (String) -> Unit,
    onAccountsClick: () -> Unit,
    onLobbyClick: () -> Unit,
    onConnectClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    var mobile by remember(state.mobile) { mutableStateOf(state.mobile.takeUnless { it.contains("not set", true) }.orEmpty()) }
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize()) {
            AppTopBar(onMenuClick = onMenuClick)
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).imePadding().padding(18.dp)) {
                Text("Change mobile number", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Update the mobile number saved to your Indoone account.", modifier = Modifier.padding(top = 5.dp, bottom = 18.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(mobile, { mobile = it }, Modifier.fillMaxWidth(), label = { Text("Mobile number") }, placeholder = { Text("+91 98765 43210") }, singleLine = true)
                Spacer(Modifier.padding(top = 16.dp))
                Button(onClick = { onSave(mobile) }, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) { Text(if (state.busy) "Updating…" else "Update mobile number") }
                state.error?.let { Text(it, Modifier.padding(top = 12.dp), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                state.message?.let { Text(it, Modifier.padding(top = 12.dp), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall) }
                Spacer(Modifier.padding(top = 12.dp))
                Text("← Back to Profile", Modifier.fillMaxWidth().padding(vertical = 10.dp), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Surface(onClick = onBack, color = MaterialTheme.colorScheme.surfaceVariant) { Text("Back to Profile", Modifier.fillMaxWidth().padding(13.dp)) }
            }
            AppBottomNav(AppTab.SETTINGS, onAccountsClick, onLobbyClick, onConnectClick, onSettingsClick)
        }
    }
}
