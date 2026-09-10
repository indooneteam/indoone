package com.indoone.settings.applock

import androidx.compose.runtime.Composable
import com.indoone.settings.applock.setapplock.PinPadScaffold

@Composable
fun UnlockAppScreen(
    pin: String,
    error: String,
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onUnlock: () -> Unit,
) {
    PinPadScaffold(
        title = "Unlock Indoone",
        description = "Enter your App PIN to unlock Indoone.",
        pin = pin,
        error = error,
        actionLabel = "Unlock App",
        actionEnabled = pin.length in 4..12,
        onDigit = onDigit,
        onBackspace = onBackspace,
        onClear = onClear,
        onAction = onUnlock,
    )
}
