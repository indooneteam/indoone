package com.indoone.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalContext
import com.indoone.menu.AppBottomNav
import com.indoone.menu.AppTab
import com.indoone.menu.AppTopBar
import com.indoone.settings.applock.AppLockScreen
import com.indoone.settings.applock.AppLockStore
import com.indoone.settings.applock.AppLockViewModel
import com.indoone.settings.applock.changeapplock.ChangeAppLockScreen
import com.indoone.settings.applock.disableapplock.DisableAppLockScreen
import com.indoone.settings.applock.setapplock.SetAppLockScreen

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
    val store = remember { AppLockStore(context) }
    val flow = remember { AppLockViewModel() }
    var appLockPage by remember { mutableStateOf(false) }

    fun resetToSettings() {
        flow.sync(store.isEnabled())
        appLockPage = false
    }

    if (appLockPage) {
        when (flow.state.value.step) {
            AppLockViewModel.Step.CREATE -> SetAppLockScreen(
                pin = flow.state.value.pin,
                error = flow.state.value.error,
                onDigit = flow::appendDigit,
                onBackspace = flow::backspace,
                onClear = flow::clear,
                onCreate = {
                    runCatching { store.setPin(flow.state.value.pin) }
                        .onSuccess { flow.markCompleted(); resetToSettings() }
                        .onFailure { flow.error("PIN must contain 4–12 digits.") }
                },
                onCancel = ::resetToSettings,
            )
            AppLockViewModel.Step.CURRENT -> {
                val state = flow.state.value
                if (state.newPin.isEmpty()) {
                    ChangeAppLockScreen(
                        stepTitle = "Verify current PIN",
                        description = "Enter your current App PIN to continue.",
                        pin = state.pin,
                        error = state.error,
                        actionLabel = "Continue",
                        onDigit = flow::appendDigit,
                        onBackspace = flow::backspace,
                        onClear = flow::clear,
                        onAction = {
                            if (store.verifyPin(state.pin)) {
                                flow.resetInput()
                                flow.setNewPin("pending")
                            } else {
                                flow.error("Incorrect current PIN")
                            }
                        },
                        onCancel = ::resetToSettings,
                    )
                } else {
                    ChangeAppLockScreen(
                        stepTitle = "Create new PIN",
                        description = "Choose a new 4–12 digit App PIN.",
                        pin = state.pin,
                        error = state.error,
                        actionLabel = "Continue",
                        onDigit = flow::appendDigit,
                        onBackspace = flow::backspace,
                        onClear = flow::clear,
                        onAction = {
                            flow.setNewPin(state.pin)
                            flow.resetInput()
                            flow.sync(store.isEnabled())
                            flow.setNewPin(state.newPin)
                        },
                        onCancel = ::resetToSettings,
                    )
                }
            }
            AppLockViewModel.Step.DISABLE -> DisableAppLockScreen(
                pin = flow.state.value.pin,
                error = flow.state.value.error,
                onDigit = flow::appendDigit,
                onBackspace = flow::backspace,
                onClear = flow::clear,
                onDisable = {
                    if (store.verifyPin(flow.state.value.pin)) {
                        store.clear()
                        resetToSettings()
                    } else {
                        flow.error("Incorrect current PIN")
                    }
                },
                onCancel = ::resetToSettings,
            )
            else -> AppLockScreen(
                hasPin = store.isEnabled(),
                onSet = { flow.startCreate(); appLockPage = true },
                onChange = { flow.startChange(); appLockPage = true },
                onDisable = {
                    flow.startDisable()
                    flow.sync(store.isEnabled())
                    flow.state.value.let { flow.clear() }
                    flow.setNewPin("disable")
                    appLockPage = true
                },
                onBack = ::resetToSettings,
            )
        }
        return
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
            AppTopBar(onMenuClick = onMenuClick)
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 18.dp),
            ) {
                Text("SETTINGS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text("Settings", modifier = Modifier.padding(top = 3.dp), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.padding(top = 18.dp))
                SettingsCard("Profile", "Manage your email and mobile number", onProfileClick)
                Spacer(Modifier.padding(top = 12.dp))
                SettingsCard("App Lock", if (store.isEnabled()) "PIN enabled" else "Protect Indoone with a PIN") {
                    flow.sync(store.isEnabled())
                    appLockPage = true
                }
            }
            AppBottomNav(AppTab.SETTINGS, onAccountsClick, onLobbyClick, onConnectClick, onSettingsClick)
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, modifier = Modifier.padding(top = 4.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
