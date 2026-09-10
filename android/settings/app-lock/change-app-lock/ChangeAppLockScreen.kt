package com.indoone.settings.applock.changeapplock

import androidx.compose.runtime.Composable
import com.indoone.settings.applock.setapplock.PinPadScaffold

@Composable
fun ChangeAppLockScreen(
    stepTitle: String,
    description: String,
    pin: String,
    error: String,
    actionLabel: String,
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onAction: () -> Unit,
    onCancel: () -> Unit,
) {
    PinPadScaffold(
        title = stepTitle,
        description = description,
        pin = pin,
        error = error,
        actionLabel = actionLabel,
        actionEnabled = pin.length in 4..12,
        onDigit = onDigit,
        onBackspace = onBackspace,
        onClear = onClear,
        onAction = onAction,
        onCancel = onCancel,
    )
}
