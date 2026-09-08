package com.indoone.menu.logout

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.indoone.authentication.AuthSessionStore

@Composable
fun LogoutScreen(
    onDismiss: () -> Unit,
    onLoggedOut: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }
    var working by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!working) onDismiss() },
        title = { Text("Log out on this device") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(androidx.compose.ui.unit.dp(10))) {
                Text("This signs you out only from this device.")
                error?.let { Text(it) }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (working) return@Button
                    working = true
                    error = null
                    scope.launch {
                        runCatching {
                            clearLocalSession(context)
                            auth.signOut()
                        }.onSuccess {
                            onLoggedOut()
                        }.onFailure {
                            working = false
                            error = it.message ?: "Could not log out"
                        }
                    }
                },
                enabled = !working,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Log out on this device")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !working) {
                Text("Cancel")
            }
        },
    )
}

private fun clearLocalSession(context: Context) {
    context.filesDir.resolve("accounts.enc").delete()
    AuthSessionStore(context).clear()
    context.getSharedPreferences("indoone_app_lock", Context.MODE_PRIVATE).edit().clear().apply()
    context.getSharedPreferences("indoone_biometric_unlock", Context.MODE_PRIVATE).edit().clear().apply()
    context.getSharedPreferences("indoone_auto_lock", Context.MODE_PRIVATE).edit().clear().apply()
}
