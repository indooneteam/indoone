package com.indoone.settings.applock.setapplock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SetAppLockScreen(
    pin: String,
    error: String,
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onCreate: () -> Unit,
    onCancel: () -> Unit,
) {
    PinPadScaffold(
        title = "Create App PIN",
        description = "Create a 4–12 digit PIN to protect Indoone.",
        pin = pin,
        error = error,
        actionLabel = "Create App PIN",
        actionEnabled = pin.length in 4..12,
        onDigit = onDigit,
        onBackspace = onBackspace,
        onClear = onClear,
        onAction = onCreate,
        onCancel = onCancel,
    )
}

@Composable
internal fun PinPadScaffold(
    title: String,
    description: String,
    pin: String,
    error: String,
    actionLabel: String,
    actionEnabled: Boolean,
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onAction: () -> Unit,
    onCancel: (() -> Unit)? = null,
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("INDOONE", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(title, modifier = Modifier.padding(top = 10.dp), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(description, modifier = Modifier.padding(top = 6.dp), style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = if (pin.isEmpty()) "Enter PIN" else "• ".repeat(pin.length).trim(),
                    modifier = Modifier.padding(top = 22.dp),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(error, modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                Button(
                    onClick = onAction,
                    enabled = actionEnabled,
                    modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                ) { Text(actionLabel) }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                val keys = listOf('1','2','3','4','5','6','7','8','9','C','0','⌫')
                keys.chunked(3).forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        row.forEach { key ->
                            val label = key.toString()
                            OutlinedButton(
                                onClick = when (key) {
                                    'C' -> onClear
                                    '⌫' -> onBackspace
                                    else -> ({ onDigit(key) })
                                },
                                modifier = Modifier.weight(1f).padding(4.dp).height(54.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(),
                            ) { Text(label) }
                        }
                    }
                }
                if (onCancel != null) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
                }
            }
        }
    }
}
