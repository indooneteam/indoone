package com.indoone.settings

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.indoone.menu.AppBottomNav
import com.indoone.menu.AppTab
import com.indoone.menu.AppTopBar
import com.indoone.settings.about.AboutScreen
import com.indoone.settings.applock.AppLockScreen
import com.indoone.settings.applock.AppLockStore
import com.indoone.settings.applock.AppLockViewModel
import com.indoone.settings.applock.changeapplock.ChangeAppLockScreen
import com.indoone.settings.applock.disableapplock.DisableAppLockScreen
import com.indoone.settings.applock.setapplock.SetAppLockScreen
import com.indoone.settings.autolock.AutoLockScreen
import com.indoone.settings.autolock.AutoLockStore
import com.indoone.settings.biometric.BiometricAuthenticator
import com.indoone.settings.biometric.BiometricUnlockStore

@Composable
fun SettingsScreen(
    onMenuClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onAccountsClick: () -> Unit = {},
    onLobbyClick: () -> Unit = {},
    onConnectClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val appLockStore = remember { AppLockStore(context) }
    val biometricStore = remember { BiometricUnlockStore(context) }
    val autoLockStore = remember { AutoLockStore(context) }
    val biometricAuthenticator = remember { (context as? Activity)?.let(::BiometricAuthenticator) }
    val flow = remember { AppLockViewModel() }

    var appLockPage by remember { mutableStateOf(false) }
    var autoLockPage by remember { mutableStateOf(false) }
    var aboutPage by remember { mutableStateOf(false) }
    var biometricOn by remember { mutableStateOf(biometricStore.isEnabled()) }
    var showAppLockRequired by remember { mutableStateOf(false) }
    var showBiometricUnavailable by remember { mutableStateOf(false) }
    var biometricError by remember { mutableStateOf<String?>(null) }

    fun resetToSettings() {
        flow.sync(appLockStore.isEnabled())
        flow.setStep(AppLockViewModel.Step.NONE)
        appLockPage = false
    }

    fun requestBiometricEnable() {
        if (!appLockStore.isEnabled()) {
            showAppLockRequired = true
            return
        }
        val authenticator = biometricAuthenticator
        if (authenticator == null || !authenticator.canAuthenticate()) {
            showBiometricUnavailable = true
            return
        }
        authenticator.authenticate(
            onSuccess = {
                biometricStore.setEnabled(true)
                biometricOn = true
                biometricError = null
            },
            onError = { message -> biometricError = message },
        )
    }

    if (aboutPage) {
        AboutScreen(
            onBack = { aboutPage = false },
            onAccountsClick = onAccountsClick,
            onLobbyClick = onLobbyClick,
            onConnectClick = onConnectClick,
            onSettingsClick = onSettingsClick,
        )
        return
    }

    if (showAppLockRequired) {
        AlertDialog(
            onDismissRequest = { showAppLockRequired = false },
            title = { Text("App Lock required") },
            text = { Text("Set App Lock before enabling Biometric Unlock.") },
            confirmButton = {
                TextButton(onClick = {
                    showAppLockRequired = false
                    flow.sync(appLockStore.isEnabled())
                    flow.startCreate()
                    appLockPage = true
                }) { Text("Set App Lock") }
            },
            dismissButton = { TextButton(onClick = { showAppLockRequired = false }) { Text("Cancel") } },
        )
    }

    if (showBiometricUnavailable) {
        AlertDialog(
            onDismissRequest = { showBiometricUnavailable = false },
            title = { Text("Biometric unavailable") },
            text = { Text("Set up a supported fingerprint or device biometric on this Android device first.") },
            confirmButton = { TextButton(onClick = { showBiometricUnavailable = false }) { Text("OK") } },
        )
    }

    biometricError?.let { message ->
        AlertDialog(
            onDismissRequest = { biometricError = null },
            title = { Text("Biometric Unlock") },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = { biometricError = null }) { Text("OK") } },
        )
    }

    if (appLockPage) {
        val state = flow.state.value
        when (state.step) {
            AppLockViewModel.Step.CREATE -> SetAppLockScreen(
                pin = state.pin,
                error = state.error,
                onDigit = flow::appendDigit,
                onBackspace = flow::backspace,
                onClear = flow::clear,
                onCreate = {
                    if (state.pin.length !in 4..12) flow.error("PIN must contain 4–12 digits.")
                    else runCatching { appLockStore.setPin(state.pin) }.onSuccess { resetToSettings() }.onFailure { flow.error("Could not create App PIN.") }
                },
                onCancel = ::resetToSettings,
            )
            AppLockViewModel.Step.CURRENT -> ChangeAppLockScreen(
                stepTitle = "Verify current PIN",
                description = "Enter your current App PIN to continue.",
                pin = state.pin,
                error = state.error,
                actionLabel = "Continue",
                onDigit = flow::appendDigit,
                onBackspace = flow::backspace,
                onClear = flow::clear,
                onAction = { if (appLockStore.verifyPin(state.pin)) { flow.setStep(AppLockViewModel.Step.NEW); flow.resetInput() } else flow.error("Incorrect current PIN") },
                onCancel = ::resetToSettings,
            )
            AppLockViewModel.Step.NEW -> ChangeAppLockScreen(
                stepTitle = "Create new PIN",
                description = "Choose a new 4–12 digit App PIN.",
                pin = state.pin,
                error = state.error,
                actionLabel = "Continue",
                onDigit = flow::appendDigit,
                onBackspace = flow::backspace,
                onClear = flow::clear,
                onAction = { if (state.pin.length in 4..12) { flow.setNewPin(state.pin); flow.setStep(AppLockViewModel.Step.CONFIRM); flow.resetInput() } else flow.error("PIN must contain 4–12 digits.") },
                onCancel = ::resetToSettings,
            )
            AppLockViewModel.Step.CONFIRM -> ChangeAppLockScreen(
                stepTitle = "Confirm new PIN",
                description = "Enter the new PIN again to confirm it.",
                pin = state.pin,
                error = state.error,
                actionLabel = "Change PIN",
                onDigit = flow::appendDigit,
                onBackspace = flow::backspace,
                onClear = flow::clear,
                onAction = { if (state.pin != state.newPin) flow.error("New PINs do not match") else runCatching { appLockStore.setPin(state.newPin) }.onSuccess { resetToSettings() }.onFailure { flow.error("Could not change App PIN.") } },
                onCancel = ::resetToSettings,
            )
            AppLockViewModel.Step.DISABLE -> DisableAppLockScreen(
                pin = state.pin,
                error = state.error,
                onDigit = flow::appendDigit,
                onBackspace = flow::backspace,
                onClear = flow::clear,
                onDisable = { if (appLockStore.verifyPin(state.pin)) { appLockStore.clear(); biometricStore.setEnabled(false); biometricOn = false; resetToSettings() } else flow.error("Incorrect current PIN") },
                onCancel = ::resetToSettings,
            )
            else -> AppLockScreen(appLockStore.isEnabled(), { flow.startCreate(); appLockPage = true }, { flow.startChange(); appLockPage = true }, { flow.startDisable(); appLockPage = true }, ::resetToSettings)
        }
        return
    }

    if (autoLockPage) {
        AutoLockScreen(
            currentMinutes = autoLockStore.minutes(),
            appLockEnabled = appLockStore.isEnabled(),
            biometricEnabled = biometricStore.isEnabled(),
            onSelectMinutes = { autoLockStore.setMinutes(it); autoLockPage = false },
            onBack = { autoLockPage = false },
        )
        return
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
            AppTopBar(onMenuClick = onMenuClick)
            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 18.dp)) {
                Text("SETTINGS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text("Settings", modifier = Modifier.padding(top = 3.dp), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.padding(top = 18.dp))
                SettingsCard("Profile", "Manage your email and mobile number", onProfileClick)
                Spacer(Modifier.padding(top = 12.dp))
                SettingsCard("App Lock", if (appLockStore.isEnabled()) "PIN enabled" else "Protect Indoone with a PIN") { flow.sync(appLockStore.isEnabled()); appLockPage = true }
                Spacer(Modifier.padding(top = 12.dp))
                BiometricCard(biometricOn) { enabled -> if (enabled) requestBiometricEnable() else { biometricStore.setEnabled(false); biometricOn = false; biometricError = null } }
                Spacer(Modifier.padding(top = 12.dp))
                SettingsCard("Auto-Lock", if (appLockStore.isEnabled() || biometricStore.isEnabled()) "After ${autoLockStore.minutes()} minute${if (autoLockStore.minutes() == 1) "" else "s"}" else "Requires App Lock or Biometric Unlock") { autoLockPage = true }
                Spacer(Modifier.padding(top = 12.dp))
                SettingsCard("About Indoone", "Version 0.1.0 · Updates", { aboutPage = true })
            }
            AppBottomNav(AppTab.SETTINGS, onAccountsClick, onLobbyClick, onConnectClick, onSettingsClick)
        }
    }
}

@Composable
private fun SettingsCard(title: String, subtitle: String, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, modifier = Modifier.padding(top = 4.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun BiometricCard(enabled: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Biometric Unlock", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Fingerprint / device biometric", modifier = Modifier.padding(top = 4.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = enabled, onCheckedChange = onCheckedChange)
        }
    }
}
