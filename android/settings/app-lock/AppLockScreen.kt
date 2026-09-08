package com.indoone.settings.applock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AppLockScreen(
    hasPin: Boolean,
    onSet: () -> Unit,
    onChange: () -> Unit,
    onDisable: () -> Unit,
    onBack: () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("App Lock", fontWeight = FontWeight.Bold)
                OutlinedButton(onClick = onBack) { Text("Back") }
            }
            Text(
                if (hasPin) "Manage your Indoone App PIN."
                else "Protect Indoone with a 4–12 digit App PIN.",
            )
            Spacer(Modifier.height(4.dp))
            if (hasPin) {
                Button(onClick = onChange, modifier = Modifier.fillMaxWidth()) { Text("Change App PIN") }
                OutlinedButton(onClick = onDisable, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors()) {
                    Text("Disable App Lock")
                }
            } else {
                Button(onClick = onSet, modifier = Modifier.fillMaxWidth()) { Text("Set App Lock") }
            }
        }
    }
}
