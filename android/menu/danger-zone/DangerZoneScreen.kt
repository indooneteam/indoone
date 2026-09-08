package com.indoone.menu.dangerzone

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

@Composable
fun DangerZoneScreen(
    onBack: () -> Unit,
    onAccountsClick: () -> Unit,
    onLobbyClick: () -> Unit,
    onConnectClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    var showLocalDelete by remember { mutableStateOf(false) }
    var showAccountDelete by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Danger Zone", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                OutlinedButton(onClick = onBack) { Text("Back") }
            }

            Text(
                "These actions can permanently remove Indoone data. Continue only when you are sure.",
                style = MaterialTheme.typography.bodyMedium,
            )

            DangerActionRow(
                title = "Delete local data",
                subtitle = "Remove data stored on this device",
                onClick = { showLocalDelete = true },
            )
            DangerActionRow(
                title = "Delete Indoone account",
                subtitle = "Permanently delete your Indoone account and cloud data",
                onClick = { showAccountDelete = true },
            )
        }
    }

    if (showLocalDelete) {
        LocalDataDeleteDialog(
            onDismiss = { showLocalDelete = false },
            onDeleted = {
                showLocalDelete = false
                onAccountsClick()
            },
        )
    }

    if (showAccountDelete) {
        AccountDeleteDialog(
            onDismiss = { showAccountDelete = false },
            onDeleted = {
                showAccountDelete = false
                onAccountsClick()
            },
        )
    }

    // Kept in the signature so this screen can use the same app chrome/navigation contract.
    LaunchedEffect(Unit) {
        onLobbyClick
        onConnectClick
        onSettingsClick
    }
}

@Composable
private fun DangerActionRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium,
    ) {
        TextButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().padding(4.dp),
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.padding(top = 2.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun LocalDataDeleteDialog(
    onDismiss: () -> Unit,
    onDeleted: () -> Unit,
) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val scope = rememberCoroutineScope()
    var working by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!working) onDismiss() },
        title = { Text("Delete local data?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("This removes Indoone data stored on this device, including the encrypted vault and local sign-in markers. Your Indoone account and cloud data will not be deleted.")
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
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
                            clearLocalData(context)
                            auth.signOut()
                        }.onSuccess {
                            onDeleted()
                        }.onFailure {
                            error = it.message ?: "Could not delete local data"
                            working = false
                        }
                    }
                },
                enabled = !working,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            ) { Text("Delete local data") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !working) { Text("Cancel") }
        },
    )
}

@Composable
private fun AccountDeleteDialog(
    onDismiss: () -> Unit,
    onDeleted: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseDatabase.getInstance().reference }
    val user = auth.currentUser

    var password by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var status by remember { mutableStateOf<String?>(null) }
    var working by remember { mutableStateOf(false) }

    if (user == null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Delete Indoone account?") },
            text = { Text("Please login first.") },
            confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        )
        return
    }

    AlertDialog(
        onDismissRequest = { if (!working) onDismiss() },
        title = { Text("Delete Indoone account?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("This permanently deletes your Indoone cloud data and Firebase account. This action cannot be undone.")
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("ACCOUNT PASSWORD") },
                    placeholder = { Text("Enter your password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    enabled = !working,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = confirmation,
                    onValueChange = { confirmation = it.uppercase() },
                    label = { Text("TYPE DELETE TO CONFIRM") },
                    placeholder = { Text("DELETE") },
                    singleLine = true,
                    enabled = !working,
                    modifier = Modifier.fillMaxWidth(),
                )
                status?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (working) return@Button
                    if (password.isBlank()) {
                        status = "Enter your account password"
                        return@Button
                    }
                    if (confirmation.trim().uppercase() != "DELETE") {
                        status = "Type DELETE to confirm"
                        return@Button
                    }
                    val email = user.email
                    if (email.isNullOrBlank()) {
                        status = "This account cannot be re-authenticated here."
                        return@Button
                    }

                    working = true
                    status = "Verifying your account…"
                    deleteAccount(
                        context = context,
                        auth = auth,
                        db = db,
                        password = password,
                        scope = scope,
                        onStatus = { status = it },
                        onSuccess = {
                            working = false
                            onDeleted()
                        },
                        onError = {
                            working = false
                            status = it
                        },
                    )
                },
                enabled = !working,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            ) { Text("Delete Indoone account") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !working) { Text("Cancel") }
        },
    )
}

private fun clearLocalData(context: Context) {
    context.filesDir.resolve("accounts.enc").delete()
    context.getSharedPreferences("indoone_app_lock", Context.MODE_PRIVATE).edit().clear().apply()
    context.getSharedPreferences("indoone_biometric_unlock", Context.MODE_PRIVATE).edit().clear().apply()
    context.getSharedPreferences("indoone_auto_lock", Context.MODE_PRIVATE).edit().clear().apply()
}

private fun deleteAccount(
    context: Context,
    auth: FirebaseAuth,
    db: com.google.firebase.database.DatabaseReference,
    password: String,
    scope: kotlinx.coroutines.CoroutineScope,
    onStatus: (String) -> Unit,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
) {
    val user = auth.currentUser ?: run {
        onError("Login session expired. Please login again.")
        return
    }
    val email = user.email ?: run {
        onError("This account cannot be re-authenticated here.")
        return
    }

    user.reauthenticate(EmailAuthProvider.getCredential(email, password))
        .addOnSuccessListener {
            user.reload().addOnSuccessListener {
                onStatus("Removing your Indoone cloud data…")
                db.child("users").child(user.uid).child("profile").get()
                    .addOnSuccessListener { profileSnapshot ->
                        val profile = profileSnapshot.value as? Map<*, *> ?: emptyMap<Any, Any>()
                        val mobile = profile["mobile"]?.toString()?.trim().orEmpty()
                        val updates = hashMapOf<String, Any?>()
                        updates["users/${user.uid}"] = null

                        db.child("users").orderByChild("profile/email").equalTo(email.trim().lowercase()).get()
                            .addOnSuccessListener { matches ->
                                val mobiles = linkedSetOf<String>()
                                if (mobile.isNotBlank()) mobiles += mobile
                                matches.children.forEach { child ->
                                    val matchedMobile = child.child("profile/mobile").value?.toString()?.trim().orEmpty()
                                    if (matchedMobile.isNotBlank()) mobiles += matchedMobile
                                    updates["users/${child.key}"] = null
                                }
                                mobiles.forEach { value ->
                                    updates["mobileIndex/${java.net.URLEncoder.encode(value, Charsets.UTF_8.name())}"] = null
                                }

                                db.updateChildren(updates)
                                    .addOnSuccessListener {
                                        onStatus("Deleting your Firebase account…")
                                        user.delete()
                                            .addOnSuccessListener {
                                                clearLocalData(context)
                                                onSuccess()
                                            }
                                            .addOnFailureListener { error ->
                                                onError(formatAuthError(error.code, error.message ?: "Account deletion failed."))
                                            }
                                    }
                                    .addOnFailureListener { error ->
                                        onError(error.message ?: "Firebase denied cloud-data deletion. Check Firebase database rules.")
                                    }
                            }
                            .addOnFailureListener { error ->
                                onError(error.message ?: "Could not find your account data.")
                            }
                    }
                    .addOnFailureListener { error ->
                        onError(error.message ?: "Could not read your account profile.")
                    }
            }
        }
        .addOnFailureListener { error ->
            onError(formatAuthError(error.code, error.message ?: "Account verification failed."))
        }
}

private fun formatAuthError(code: String, fallback: String): String {
    return when (code) {
        "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL" -> "Incorrect account password."
        "ERROR_REQUIRES_RECENT_LOGIN" -> "A fresh login is required. Please login again and retry."
        "ERROR_NETWORK_REQUEST_FAILED" -> "Network error. Check your connection and retry."
        else -> fallback
    }
}
