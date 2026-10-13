package com.indoone.settings

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indoone.authentication.AuthActivity
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
import com.indoone.settings.dangerzone.DangerZoneScreen
import com.indoone.settings.logout.LogoutScreen

private val ProfileIcon = Icons.Outlined.Person
private val LockIcon = Icons.Outlined.Lock
private val BiometricIcon = Icons.Outlined.Fingerprint
private val TimerIcon = Icons.Outlined.Timer
private val InfoIcon = Icons.Outlined.Info
private val NotificationsIcon = Icons.Outlined.NotificationsNone
private val DangerZoneIcon = Icons.Outlined.WarningAmber
private val LogoutIcon = Icons.Outlined.Logout
private val ChevronIcon = Icons.Outlined.ChevronRight

private val ItemTitleColor = Color(0xFF2C2733)
private val ItemSubtitleColor = Color(0xFF8A8392)
private val SectionLabelColor = Color(0xFF8592A8)

@Composable
fun SettingsScreen(
    onMenuClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onAccountsClick: () -> Unit = {},
    onLobbyClick: () -> Unit = {},
    onConnectClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onCheckForUpdates: () -> Unit = {},
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val appLockStore = remember { AppLockStore(context) }
    val biometricStore = remember { BiometricUnlockStore(context) }
    val autoLockStore = remember { AutoLockStore(context) }
    val biometricAuthenticator = remember { activity?.let(::BiometricAuthenticator) }
    val flow = remember { AppLockViewModel() }

    var appLockPage by remember { mutableStateOf(false) }
    var autoLockPage by remember { mutableStateOf(false) }
    var aboutPage by remember { mutableStateOf(false) }
    var dangerZonePage by remember { mutableStateOf(false) }
    var logoutPage by remember { mutableStateOf(false) }
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

    Surface(Modifier.fillMaxSize(), color = Color.White) {
        Column(Modifier.fillMaxSize()) {
            AppTopBar(onMenuClick = onMenuClick)
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 18.dp),
            ) {
                Column(Modifier.padding(top = 6.dp, bottom = 4.dp)) {
                    Text("Settings", Modifier.padding(top = 3.dp), color = Color(0xFF1F1B24), fontSize = 26.sp, lineHeight = 29.sp, fontWeight = FontWeight.Bold)
                    Text("Manage your account, security and app preferences.", Modifier.padding(top = 7.dp), color = Color(0xFF77717F), fontSize = 12.sp, lineHeight = 19.sp)
                }

                SettingsSectionLabel("ACCOUNT")
                SettingsMenuItem(ProfileIcon, "Profile", "Email & mobile number", onProfileClick)

                SettingsSectionLabel("SECURITY")
                SettingsMenuItem(
                    LockIcon,
                    "App Lock",
                    if (appLockStore.isEnabled()) "PIN is set" else "Not set",
                ) {
                    flow.sync(appLockStore.isEnabled())
                    appLockPage = true
                }
                SettingsMenuToggle(
                    BiometricIcon,
                    "Biometric Unlock",
                    "Fingerprint / device credential",
                    biometricOn,
                ) { enabled ->
                    if (enabled) {
                        requestBiometricEnable()
                    } else {
                        biometricStore.setEnabled(false)
                        biometricOn = false
                        biometricError = null
                    }
                }
                SettingsMenuItem(
                    TimerIcon,
                    "Auto-Lock",
                    if (autoLockStore.minutes() > 0) {
                        "After ${autoLockStore.minutes()} minute${if (autoLockStore.minutes() == 1) "" else "s"}"
                    } else {
                        "Never"
                    },
                ) { autoLockPage = true }

                SettingsSectionLabel("APP")
                SettingsMenuItem(NotificationsIcon, "Notifications", "Security, AI, Vibe and other alerts", onNotificationsClick)
                SettingsMenuItem(InfoIcon, "About Indoone", "Version 0.1.0 · Updates") { aboutPage = true }

                SettingsSectionLabel("DANGER ZONE")
                SettingsMenuItem(DangerZoneIcon, "Danger Zone", "Delete local data or your Indoone account") { dangerZonePage = true }
                SettingsMenuItem(LogoutIcon, "Log out", "Sign out from this device") { logoutPage = true }

                Spacer(Modifier.height(8.dp))
            }
            AppBottomNav(AppTab.SETTINGS, onAccountsClick, onLobbyClick, onConnectClick, onSettingsClick)
        }
    }

    if (appLockPage) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x5519141F)),
            contentAlignment = Alignment.BottomCenter,
        ) {
            val state by flow.state.collectAsState()
            when (state.step) {
                AppLockViewModel.Step.CREATE -> SetAppLockScreen(state.pin, state.error, flow::appendDigit, flow::backspace, flow::clear, {
                    if (state.pin.length !in 4..12) flow.error("PIN must contain 4–12 digits.")
                    else runCatching { appLockStore.setPin(state.pin) }
                        .onSuccess { resetToSettings() }
                        .onFailure { flow.error("Could not create App PIN.") }
                }, ::resetToSettings)
                AppLockViewModel.Step.CURRENT -> ChangeAppLockScreen(
                    "Verify current PIN",
                    "Enter your current App PIN to continue.",
                    state.pin,
                    state.error,
                    "Continue",
                    flow::appendDigit,
                    flow::backspace,
                    flow::clear,
                    {
                        if (appLockStore.verifyPin(state.pin)) {
                            flow.setStep(AppLockViewModel.Step.NEW)
                            flow.resetInput()
                        } else flow.error("Incorrect current PIN")
                    },
                    ::resetToSettings,
                )
                AppLockViewModel.Step.NEW -> ChangeAppLockScreen(
                    "Create new PIN",
                    "Choose a new 4–12 digit App PIN.",
                    state.pin,
                    state.error,
                    "Continue",
                    flow::appendDigit,
                    flow::backspace,
                    flow::clear,
                    {
                        if (state.pin.length in 4..12) {
                            flow.setNewPin(state.pin)
                            flow.setStep(AppLockViewModel.Step.CONFIRM)
                            flow.resetInput()
                        } else flow.error("PIN must contain 4–12 digits.")
                    },
                    ::resetToSettings,
                )
                AppLockViewModel.Step.CONFIRM -> ChangeAppLockScreen(
                    "Confirm new PIN",
                    "Enter the new App PIN again to confirm it.",
                    state.pin,
                    state.error,
                    "Change PIN",
                    flow::appendDigit,
                    flow::backspace,
                    flow::clear,
                    {
                        if (state.pin != state.newPin) flow.error("New PINs do not match")
                        else runCatching { appLockStore.setPin(state.newPin) }
                            .onSuccess { resetToSettings() }
                            .onFailure { flow.error("Could not change App PIN.") }
                    },
                    ::resetToSettings,
                )
                AppLockViewModel.Step.DISABLE -> DisableAppLockScreen(
                    state.pin,
                    state.error,
                    flow::appendDigit,
                    flow::backspace,
                    flow::clear,
                    {
                        if (appLockStore.verifyPin(state.pin)) {
                            appLockStore.clear()
                            biometricStore.setEnabled(false)
                            biometricOn = false
                            resetToSettings()
                        } else flow.error("Incorrect current PIN")
                    },
                    ::resetToSettings,
                )
                else -> AppLockScreen(
                    appLockStore.isEnabled(),
                    { flow.startCreate(); appLockPage = true },
                    { flow.startChange(); appLockPage = true },
                    { flow.startDisable(); appLockPage = true },
                    ::resetToSettings,
                )
            }
        }
    }

    if (autoLockPage) {
        AutoLockScreen(
            autoLockStore.minutes(),
            appLockStore.isEnabled(),
            biometricStore.isEnabled(),
            { autoLockStore.setMinutes(it); autoLockPage = false },
            { autoLockPage = false },
        )
    }

    if (aboutPage) {
        AboutScreen(
            onBack = { aboutPage = false },
            onAccountsClick = onAccountsClick,
            onLobbyClick = onLobbyClick,
            onConnectClick = onConnectClick,
            onSettingsClick = onSettingsClick,
            onCheckForUpdates = onCheckForUpdates,
        )
    }

    if (dangerZonePage) {
        DangerZoneScreen(
            onBack = { dangerZonePage = false },
            onAccountsClick = onAccountsClick,
            onLobbyClick = onLobbyClick,
            onConnectClick = onConnectClick,
            onSettingsClick = onSettingsClick,
        )
    }

    if (logoutPage) {
        LogoutScreen(
            onDismiss = { logoutPage = false },
            onLoggedOut = {
                logoutPage = false
                context.startActivity(
                    Intent(context, AuthActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    },
                )
                activity?.finish()
            },
        )
    }
}

/** Small uppercase heading, matching the main menu's section labels. */
@Composable
private fun SettingsSectionLabel(label: String) {
    Text(
        text = label,
        modifier = Modifier.padding(start = 9.dp, top = 16.dp, bottom = 5.dp),
        color = SectionLabelColor,
        fontSize = 10.sp,
        lineHeight = 13.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.0.sp,
    )
}

/** The rounded icon tile the main menu uses for every entry. */
@Composable
private fun SettingsMenuIcon(icon: ImageVector) {
    Surface(
        modifier = Modifier.size(32.dp),
        shape = RoundedCornerShape(10.dp),
        color = Color.Transparent,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(listOf(Color.White, Color(0xFFF3EFFA)))),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, Modifier.size(18.dp), tint = Color(0xFF51627D))
        }
    }
}

@Composable
private fun SettingsMenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .heightIn(min = 56.dp)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsMenuIcon(icon)
        Column(Modifier.weight(1f).padding(start = 11.dp)) {
            Text(title, color = ItemTitleColor, fontSize = 13.sp, lineHeight = 17.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, Modifier.padding(top = 2.dp), color = ItemSubtitleColor, fontSize = 11.sp, lineHeight = 14.sp)
        }
        trailing?.invoke()
    }
}

@Composable
private fun SettingsMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    SettingsMenuRow(
        icon = icon,
        title = title,
        subtitle = subtitle,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Icon(ChevronIcon, "Open $title", Modifier.size(18.dp), tint = Color(0xFF8B9AB0))
    }
}

@Composable
private fun SettingsMenuToggle(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    SettingsMenuRow(icon = icon, title = title, subtitle = subtitle) {
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
