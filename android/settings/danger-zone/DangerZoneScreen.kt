package com.indoone.settings.dangerzone

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

private enum class DangerView { ROOT, LOCAL_DATA, ACCOUNT_DELETE }

@Composable
fun DangerZoneScreen(
    onBack: () -> Unit,
    onAccountsClick: () -> Unit,
    onLobbyClick: () -> Unit,
    onConnectClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    var view by remember { mutableStateOf(DangerView.ROOT) }
    when (view) {
        DangerView.ROOT -> DangerZoneRoot(onBack, { view = DangerView.LOCAL_DATA }, { view = DangerView.ACCOUNT_DELETE })
        DangerView.LOCAL_DATA -> DeleteLocalDataScreen({ view = DangerView.ROOT }, onAccountsClick)
        DangerView.ACCOUNT_DELETE -> DeleteAccountScreen({ view = DangerView.ROOT }, onAccountsClick)
    }
    @Suppress("UNUSED_VARIABLE")
    val keepNavigationContract = onLobbyClick to onConnectClick to onSettingsClick
}

@Composable
private fun DangerZoneRoot(onBack: () -> Unit, onDeleteLocal: () -> Unit, onDeleteAccount: () -> Unit) {
    DangerModalShell(onBack) {
        Text("Danger Zone", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1A22))
        Spacer(Modifier.height(8.dp))
        Text("These actions can permanently remove Indoone data. Continue only when you are sure.", color = Color(0xFF8A8492), fontSize = 12.sp, lineHeight = 18.sp)
        Spacer(Modifier.height(12.dp))
        DangerRow("Delete local data", "Remove data stored on this device", onDeleteLocal)
        Spacer(Modifier.height(8.dp))
        DangerRow("Delete Indoone account", "Permanently delete your Indoone account and cloud data", onDeleteAccount)
    }
}

@Composable
private fun DangerRow(title: String, subtitle: String, onClick: () -> Unit) {
    Surface(Modifier.fillMaxWidth().clickable(onClick = onClick), color = Color.White, shape = RoundedCornerShape(16.dp)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3A3442))
                Spacer(Modifier.height(3.dp))
                Text(subtitle, fontSize = 11.sp, lineHeight = 16.sp, color = Color(0xFF8A8492))
            }
            Text("›", fontSize = 24.sp, color = Color(0xFF8A8492), modifier = Modifier.padding(start = 10.dp))
        }
    }
}

@Composable
private fun DeleteLocalDataScreen(onBack: () -> Unit, onDeleted: () -> Unit) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val scope = rememberCoroutineScope()
    var working by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    DangerModalShell(onBack, "Delete local data?") {
        Text("This removes Indoone data stored on this device, including the encrypted vault and local sign-in markers. Your Indoone account and cloud data will not be deleted.", color = Color(0xFF8A8492), fontSize = 12.sp, lineHeight = 19.sp)
        error?.let { Spacer(Modifier.height(10.dp)); Text(it, color = Color(0xFFB42318), fontSize = 12.sp, lineHeight = 18.sp) }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                if (working) return@Button
                working = true; error = null
                scope.launch {
                    runCatching { clearLocalData(context); auth.signOut() }
                        .onSuccess { onDeleted() }
                        .onFailure { working = false; error = it.message ?: "Could not delete local data" }
                }
            },
            enabled = !working,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB42318)),
        ) { Text("Delete local data", fontWeight = FontWeight.Bold) }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onBack, enabled = !working, modifier = Modifier.fillMaxWidth().height(48.dp)) { Text("Cancel", fontWeight = FontWeight.Bold, color = Color(0xFF6330DB)) }
    }
}

@Composable
private fun DeleteAccountScreen(onBack: () -> Unit, onDeleted: () -> Unit) {
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
        DangerModalShell(onBack, "Delete Indoone account?") {
            Text("Please login first.", color = Color(0xFF8A8492), fontSize = 12.sp, lineHeight = 19.sp)
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(48.dp)) { Text("Close", fontWeight = FontWeight.Bold, color = Color(0xFF6330DB)) }
        }
        return
    }

    DangerModalShell(onBack, "Delete Indoone account?") {
        Text("This permanently deletes your Indoone cloud data and Firebase account. This action cannot be undone.", color = Color(0xFF8A8492), fontSize = 12.sp, lineHeight = 19.sp)
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(password, { password = it }, label = { Text("ACCOUNT PASSWORD") }, placeholder = { Text("Enter your password") }, visualTransformation = PasswordVisualTransformation(), singleLine = true, enabled = !working, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp))
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(confirmation, { confirmation = it.uppercase() }, label = { Text("TYPE DELETE TO CONFIRM") }, placeholder = { Text("DELETE") }, singleLine = true, enabled = !working, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp))
        status?.let { Spacer(Modifier.height(10.dp)); Text(it, color = Color(0xFFB42318), fontSize = 12.sp, lineHeight = 18.sp) }
        Spacer(Modifier.height(14.dp))
        Button(
            onClick = {
                if (working) return@Button
                if (password.isBlank()) { status = "Enter your account password"; return@Button }
                if (confirmation.trim().uppercase() != "DELETE") { status = "Type DELETE to confirm"; return@Button }
                val email = user.email
                if (email.isNullOrBlank()) { status = "This account cannot be re-authenticated here."; return@Button }
                working = true; status = "Verifying your account…"
                deleteAccount(context, auth, db, password, scope, { status = it }, { working = false; onDeleted() }, { working = false; status = it })
            },
            enabled = !working,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB42318)),
        ) { Text("Delete Indoone account", fontWeight = FontWeight.Bold) }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onBack, enabled = !working, modifier = Modifier.fillMaxWidth().height(48.dp)) { Text("Cancel", fontWeight = FontWeight.Bold, color = Color(0xFF6330DB)) }
    }
}

@Composable
private fun DangerModalShell(onBack: () -> Unit, title: String? = null, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x5519141F))
            .clickable(onClick = onBack),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp)
                .clickable(onClick = {}),
            color = Color.White,
            shadowElevation = 14.dp,
            shape = RoundedCornerShape(25.dp),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 21.dp, vertical = 20.dp),
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        title ?: "Danger Zone",
                        Modifier.weight(1f),
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1A22),
                    )
                    Box(
                        Modifier
                            .size(35.dp)
                            .background(Color(0xFFF5F2F8), RoundedCornerShape(11.dp))
                            .clickable(onClick = onBack),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("×", fontSize = 23.sp, lineHeight = 23.sp, color = Color(0xFF5D5666))
                    }
                }
                Spacer(Modifier.height(14.dp))
                content()
            }
        }
    }
}

private fun clearLocalData(context: Context) {
    context.filesDir.resolve("accounts.enc").delete()
    context.getSharedPreferences("indoone_app_lock", Context.MODE_PRIVATE).edit().clear().apply()
    context.getSharedPreferences("indoone_biometric_unlock", Context.MODE_PRIVATE).edit().clear().apply()
    context.getSharedPreferences("indoone_auto_lock", Context.MODE_PRIVATE).edit().clear().apply()
}

private fun deleteAccount(context: Context, auth: FirebaseAuth, db: com.google.firebase.database.DatabaseReference, password: String, scope: kotlinx.coroutines.CoroutineScope, onStatus: (String) -> Unit, onSuccess: () -> Unit, onError: (String) -> Unit) {
    val user = auth.currentUser ?: run { onError("Login session expired. Please login again."); return }
    val email = user.email ?: run { onError("This account cannot be re-authenticated here."); return }
    user.reauthenticate(EmailAuthProvider.getCredential(email, password)).addOnSuccessListener {
        user.reload().addOnSuccessListener {
            onStatus("Removing your Indoone cloud data…")
            db.child("users").child(user.uid).child("profile").get().addOnSuccessListener { profileSnapshot ->
                val profile = profileSnapshot.value as? Map<*, *> ?: emptyMap<Any, Any>()
                val mobile = profile["mobile"]?.toString()?.trim().orEmpty()
                val updates = hashMapOf<String, Any?>()
                updates["users/${user.uid}"] = null
                db.child("users").orderByChild("profile/email").equalTo(email.trim().lowercase()).get().addOnSuccessListener { matches ->
                    val mobiles = linkedSetOf<String>()
                    if (mobile.isNotBlank()) mobiles += mobile
                    matches.children.forEach { child ->
                        val matchedMobile = child.child("profile/mobile").value?.toString()?.trim().orEmpty()
                        if (matchedMobile.isNotBlank()) mobiles += matchedMobile
                        updates["users/${child.key}"] = null
                    }
                    mobiles.forEach { value -> updates["mobileIndex/${java.net.URLEncoder.encode(value, Charsets.UTF_8.name())}"] = null }
                    db.updateChildren(updates).addOnSuccessListener {
                        onStatus("Deleting your Firebase account…")
                        user.delete().addOnSuccessListener { clearLocalData(context); onSuccess() }.addOnFailureListener { e -> onError(formatAuthError(e.code, e.message ?: "Account deletion failed.")) }
                    }.addOnFailureListener { e -> onError(e.message ?: "Firebase denied cloud-data deletion. Check Firebase database rules.") }
                }.addOnFailureListener { e -> onError(e.message ?: "Could not find your account data.") }
            }.addOnFailureListener { e -> onError(e.message ?: "Could not read your account profile.") }
        }
    }.addOnFailureListener { e -> onError(formatAuthError(e.code, e.message ?: "Account verification failed.")) }
}

private fun formatAuthError(code: String, fallback: String): String = when (code) {
    "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL" -> "Incorrect account password."
    "ERROR_USER_MISMATCH" -> "The account credentials do not match this Indoone account."
    "ERROR_TOO_MANY_REQUESTS" -> "Too many attempts. Please try again later."
    "ERROR_NETWORK_REQUEST_FAILED" -> "Network error. Check your connection and try again."
    else -> fallback
}
