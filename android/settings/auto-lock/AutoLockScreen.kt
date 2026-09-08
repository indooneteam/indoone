package com.indoone.settings.autolock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AutoLockScreen(
    currentMinutes: Int,
    appLockEnabled: Boolean,
    biometricEnabled: Boolean,
    onSelectMinutes: (Int) -> Unit,
    onBack: () -> Unit,
) {
    Surface(modifier = Modifier.safeDrawingPadding()) {
        Column(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Auto-Lock", fontWeight = FontWeight.Bold)
                OutlinedButton(onClick = onBack) { Text("Back") }
            }
            Text("Automatically lock Indoone after inactivity.", style = MaterialTheme.typography.bodyMedium)
            Text(
                if (appLockEnabled || biometricEnabled) "Security lock is active. Unlock with your App PIN or enabled biometric."
                else "Auto-Lock becomes available when App Lock or Biometric Unlock is enabled.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(6.dp))
            listOf(1, 5, 15).forEach { minutes ->
                val selected = currentMinutes == minutes
                Surface(
                    onClick = { onSelectMinutes(minutes) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                ) {
                    Text(
                        "After $minutes minute${if (minutes == 1) "" else "s"}${if (selected) "  ✓" else ""}",
                        modifier = Modifier.padding(16.dp),
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}
