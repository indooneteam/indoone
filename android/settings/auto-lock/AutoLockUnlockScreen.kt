package com.indoone.settings.autolock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AutoLockUnlockScreen(
    biometricEnabled: Boolean,
    onUnlockWithPin: (String) -> Boolean,
    onUnlockWithBiometric: () -> Boolean,
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var biometricStarted by remember { mutableStateOf(false) }

    fun append(digit: String) {
        if (pin.length < 12) {
            pin += digit
            error = ""
        }
    }

    LaunchedEffect(biometricEnabled) {
        if (biometricEnabled && !biometricStarted) {
            biometricStarted = true
            onUnlockWithBiometric()
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("Indoone", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.size(12.dp))
            Text("Unlock Indoone", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                "Enter your App PIN to continue.${if (biometricEnabled) " You can also use biometric unlock." else ""}",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.size(20.dp))
            Text(
                "${"• ".repeat(pin.length)}${"○ ".repeat((4 - pin.length.coerceAtMost(4)).coerceAtLeast(0))}".trim(),
                style = MaterialTheme.typography.titleLarge,
            )
            if (error.isNotBlank()) {
                Text(error, modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.size(16.dp))
            val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "Clear", "0", "⌫")
            keys.chunked(3).forEach { row ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { key ->
                        OutlinedButton(
                            onClick = {
                                when (key) {
                                    "Clear" -> { pin = ""; error = "" }
                                    "⌫" -> { pin = pin.dropLast(1); error = "" }
                                    else -> append(key)
                                }
                            },
                            modifier = Modifier.weight(1f),
                        ) { Text(key) }
                    }
                }
                Spacer(Modifier.size(8.dp))
            }
            Button(
                onClick = {
                    if (pin.length !in 4..12) {
                        error = "Enter a 4–12 digit PIN."
                    } else if (onUnlockWithPin(pin)) {
                        error = ""
                    } else {
                        error = "Incorrect PIN"
                        pin = ""
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) { Text("Unlock with App PIN") }
            if (biometricEnabled) {
                IconButton(onClick = { biometricStarted = false; onUnlockWithBiometric() }, modifier = Modifier.padding(top = 8.dp)) {
                    Text("Fingerprint", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
