package com.indoone.settings

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

private fun settingsIcon(name: String, content: androidx.compose.ui.graphics.vector.PathBuilder.() -> Unit): ImageVector =
    ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.8f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
            strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round,
            pathBuilder = content,
        )
    }.build()

private val ProfileIcon = settingsIcon("SettingsProfile") {
    moveTo(12f, 4.9f); curveTo(10.287f, 4.9f, 8.9f, 6.287f, 8.9f, 8f); curveTo(8.9f, 9.713f, 10.287f, 11.1f, 12f, 11.1f); curveTo(13.713f, 11.1f, 15.1f, 9.713f, 15.1f, 8f); curveTo(15.1f, 6.287f, 13.713f, 4.9f, 12f, 4.9f)
    moveTo(5.5f, 19.5f); curveTo(6.3f, 16.4f, 8.6f, 14.7f, 12f, 14.7f); curveTo(15.4f, 14.7f, 17.7f, 16.4f, 18.5f, 19.5f)
}

private val LockIcon = settingsIcon("SettingsLock") {
    moveTo(5f, 10f); lineTo(19f, 10f); lineTo(19f, 20f); lineTo(5f, 20f); close()
    moveTo(8f, 10f); lineTo(8f, 7f); curveTo(8f, 4.791f, 9.791f, 3f, 12f, 3f); curveTo(14.209f, 3f, 16f, 4.791f, 16f, 7f); lineTo(16f, 10f)
}

private val BiometricIcon = settingsIcon("SettingsBiometric") {
    moveTo(8f, 7.5f); curveTo(8f, 5.015f, 10.015f, 3f, 12.5f, 3f); curveTo(14.985f, 3f, 17f, 5.015f, 17f, 7.5f)
    moveTo(6f, 10f); curveTo(6f, 6.686f, 8.686f, 4f, 12f, 4f); curveTo(15.314f, 4f, 18f, 6.686f, 18f, 10f)
    moveTo(8.5f, 12.5f); curveTo(8.5f, 10.567f, 10.067f, 9f, 12f, 9f); curveTo(13.933f, 9f, 15.5f, 10.567f, 15.5f, 12.5f)
    moveTo(10.5f, 15f); lineTo(10.5f, 16.5f); curveTo(10.5f, 17.328f, 9.828f, 18f, 9f, 18f); curveTo(8.172f, 18f, 7.5f, 17.328f, 7.5f, 16.5f); lineTo(7.5f, 15.3f)
    moveTo(13.5f, 15f); lineTo(13.5f, 18f); curveTo(13.5f, 19.105f, 12.605f, 20f, 11.5f, 20f); curveTo(10.395f, 20f, 9.5f, 19.105f, 9.5f, 18f); lineTo(9.5f, 16.5f)
    moveTo(16f, 13f); lineTo(16f, 17f); curveTo(16f, 18.105f, 15.105f, 19f, 14f, 19f); curveTo(12.895f, 19f, 12f, 18.105f, 12f, 17f); lineTo(12f, 16f)
}

private val TimerIcon = settingsIcon("SettingsTimer") {
    moveTo(12f, 6f); curveTo(8.134f, 6f, 5f, 9.134f, 5f, 13f); curveTo(5f, 16.866f, 8.134f, 20f, 12f, 20f); curveTo(15.866f, 20f, 19f, 16.866f, 19f, 13f); curveTo(19f, 9.134f, 15.866f, 6f, 12f, 6f)
    moveTo(12f, 13f); lineTo(12f, 9f)
    moveTo(9.5f, 3.5f); lineTo(14.5f, 3.5f)
    moveTo(12f, 6f); lineTo(12f, 3.5f)
    moveTo(18f, 7f); lineTo(19.5f, 5.5f)
}

private val InfoIcon = settingsIcon("SettingsInfo") {
    moveTo(12f, 4f); curveTo(16.418f, 4f, 20f, 7.582f, 20f, 12f); curveTo(20f, 16.418f, 16.418f, 20f, 12f, 20f); curveTo(7.582f, 20f, 4f, 16.418f, 4f, 12f); curveTo(4f, 7.582f, 7.582f, 4f, 12f, 4f)
    moveTo(12f, 10.5f); lineTo(12f, 15.5f)
    moveTo(12f, 7.5f); lineTo(12.01f, 7.5f)
}

private val ChevronIcon = settingsIcon("SettingsNext") {
    moveTo(9f, 5f); lineTo(16f, 12f); lineTo(9f, 19f)
}

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
            onSuccess = { biometricStore.setEnabled(true); biometricOn = true; biometricError = null },
            onError = { message -> biometricError = message },
        )
    }

    if (showAppLockRequired) {
        AlertDialog(
            onDismissRequest = { showAppLockRequired = false },
            title = { Text("App Lock required") },
            text = { Text("Set App Lock before enabling Biometric Unlock.") },
            confirmButton = { TextButton(onClick = { showAppLockRequired = false; flow.sync(appLockStore.isEnabled()); flow.startCreate(); appLockPage = true }) { Text("Set App Lock") } },
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
            AppLockViewModel.Step.CREATE -> SetAppLockScreen(state.pin, state.error, flow::appendDigit, flow::backspace, flow::clear, {
                if (state.pin.length !in 4..12) flow.error("PIN must contain 4–12 digits.")
                else runCatching { appLockStore.setPin(state.pin) }.onSuccess { resetToSettings() }.onFailure { flow.error("Could not create App PIN.") }
            }, ::resetToSettings)
            AppLockViewModel.Step.CURRENT -> ChangeAppLockScreen("Verify current PIN", "Enter your current App PIN to continue.", state.pin, state.error, "Continue", flow::appendDigit, flow::backspace, flow::clear, {
                if (appLockStore.verifyPin(state.pin)) { flow.setStep(AppLockViewModel.Step.NEW); flow.resetInput() } else flow.error("Incorrect current PIN")
            }, ::resetToSettings)
            AppLockViewModel.Step.NEW -> ChangeAppLockScreen("Create new PIN", "Choose a new 4–12 digit App PIN.", state.pin, state.error, "Continue", flow::appendDigit, flow::backspace, flow::clear, {
                if (state.pin.length in 4..12) { flow.setNewPin(state.pin); flow.setStep(AppLockViewModel.Step.CONFIRM); flow.resetInput() } else flow.error("PIN must contain 4–12 digits.")
            }, ::resetToSettings)
            AppLockViewModel.Step.CONFIRM -> ChangeAppLockScreen("Confirm new PIN", "Enter the new PIN again to confirm it.", state.pin, state.error, "Change PIN", flow::appendDigit, flow::backspace, flow::clear, {
                if (state.pin != state.newPin) flow.error("New PINs do not match") else runCatching { appLockStore.setPin(state.newPin) }.onSuccess { resetToSettings() }.onFailure { flow.error("Could not change App PIN.") }
            }, ::resetToSettings)
            AppLockViewModel.Step.DISABLE -> DisableAppLockScreen(state.pin, state.error, flow::appendDigit, flow::backspace, flow::clear, {
                if (appLockStore.verifyPin(state.pin)) { appLockStore.clear(); biometricStore.setEnabled(false); biometricOn = false; resetToSettings() } else flow.error("Incorrect current PIN")
            }, ::resetToSettings)
            else -> AppLockScreen(appLockStore.isEnabled(), { flow.startCreate(); appLockPage = true }, { flow.startChange(); appLockPage = true }, { flow.startDisable(); appLockPage = true }, ::resetToSettings)
        }
        return
    }

    if (autoLockPage) {
        AutoLockScreen(autoLockStore.minutes(), appLockStore.isEnabled(), biometricStore.isEnabled(), { autoLockStore.setMinutes(it); autoLockPage = false }, { autoLockPage = false })
        return
    }

    Surface(Modifier.fillMaxSize(), color = Color.White) {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            AppTopBar(onMenuClick = onMenuClick)
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 18.dp),
            ) {
                Column(Modifier.padding(top = 6.dp, bottom = 16.dp)) {
                    Text("PREFERENCES & SECURITY", color = Color(0xFF2877E8), fontSize = 9.sp, lineHeight = 11.sp, fontWeight = FontWeight.Bold)
                    Text("Settings", Modifier.padding(top = 3.dp), color = Color(0xFF1F1B24), fontSize = 26.sp, lineHeight = 29.sp, fontWeight = FontWeight.Bold)
                    Text("Manage your account, security and app preferences.", Modifier.padding(top = 7.dp), color = Color(0xFF77717F), fontSize = 12.sp, lineHeight = 19.sp)
                }
                SettingsSectionLabel("Account", true)
                SettingsActionRow("Profile", "Email & mobile number", ProfileIcon, onProfileClick)
                SettingsSectionLabel("Security")
                SettingsActionRow("App Lock", "PIN", LockIcon) { flow.sync(appLockStore.isEnabled()); appLockPage = true }
                SettingsToggleRow("Biometric Unlock", "Fingerprint / device credential", BiometricIcon, biometricOn) { enabled -> if (enabled) requestBiometricEnable() else { biometricStore.setEnabled(false); biometricOn = false; biometricError = null } }
                SettingsActionRow("Auto-Lock", if (autoLockStore.minutes() > 0) "After ${autoLockStore.minutes()} minute${if (autoLockStore.minutes() == 1) "" else "s"}" else "Never", TimerIcon) { autoLockPage = true }
                SettingsSectionLabel("App")
                SettingsActionRow("About Indoone", "Version 0.1.0 · Updates", InfoIcon) { aboutPage = true }
            }
            AppBottomNav(AppTab.SETTINGS, onAccountsClick, onLobbyClick, onConnectClick, onSettingsClick)
        }
    }

    if (aboutPage) {
        AboutScreen(
            onBack = { aboutPage = false },
            onAccountsClick = onAccountsClick,
            onLobbyClick = onLobbyClick,
            onConnectClick = onConnectClick,
            onSettingsClick = onSettingsClick,
        )
    }
}

@Composable
private fun SettingsSectionLabel(label: String, first: Boolean = false) {
    Text(label, Modifier.padding(top = if (first) 0.dp else 18.dp, bottom = 7.dp), color = Color(0xFF8A8392), fontSize = 11.sp, lineHeight = 13.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun SettingsActionRow(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(icon, null, Modifier.size(22.dp), tint = Color(0xFF756D80))
            Column(Modifier.weight(1f)) {
                Text(title, color = Color(0xFF2C2733), fontSize = 13.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, Modifier.padding(top = 3.dp), color = Color(0xFF8A8392), fontSize = 11.sp, lineHeight = 15.sp)
            }
            Icon(ChevronIcon, null, Modifier.size(18.dp), tint = Color(0xFFAAA3B0))
        }
        HorizontalDivider(color = Color(0xFFEEE8F4), thickness = 1.dp)
    }
}

@Composable
private fun SettingsToggleRow(title: String, subtitle: String, icon: ImageVector, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(icon, null, Modifier.size(22.dp), tint = Color(0xFF756D80))
            Column(Modifier.weight(1f)) {
                Text(title, color = Color(0xFF2C2733), fontSize = 13.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, Modifier.padding(top = 3.dp), color = Color(0xFF8A8392), fontSize = 11.sp, lineHeight = 15.dp)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
        HorizontalDivider(color = Color(0xFFEEE8F4), thickness = 1.dp)
    }
}
