package com.indoone.settings.applock.disableapplock

import androidx.compose.runtime.Composable
import com.indoone.settings.applock.setapplock.PinPadScaffold

@Composable
fun DisableAppLockScreen(
    pin: String,
    error: String,
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onDisable: () -> Unit,
    onCancel: () -> Unit,
) {
    PinPadScaffold(
        title = "Disable App Lock",
        description = "Enter your current App PIN to disable App Lock.",
        pin = pin,
        error = error,
        actionLabel = "Disable App Lock",
        actionEnabled = pin.length in 4..12,
        onDigit = onDigit,
        onBackspace = onBackspace,
        onClear = onClear,
        onAction = onDisable,
        onCancel = onCancel,
    )
}
