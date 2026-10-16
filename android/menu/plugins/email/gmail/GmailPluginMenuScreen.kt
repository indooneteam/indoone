package com.indoone.menu.plugins.email.gmail

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.launch

@Composable
fun GmailPluginMenuScreen(
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var connected by remember { mutableStateOf(false) }
    var checking by remember { mutableStateOf(true) }
    var connecting by remember { mutableStateOf(false) }
    var connectedEmailAddress by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf("") }

    fun checkStatus() {
        scope.launch {
            runCatching { GmailPluginApi.getConnectionStatus() }
                .onSuccess { result ->
                    connected = result.connected
                    connectedEmailAddress = result.emailAddress
                    message = when {
                        result.connected -> "Gmail is connected to Indoone and saved to your profile."
                        result.profileSaved -> "Gmail is listed in your Profile, but the backend could not confirm its authorization. Reconnect Gmail to restore access."
                        else -> ""
                    }
                }
                .onFailure {
                    connected = false
                    message = it.message ?: "Could not check Gmail connection."
                }
            checking = false
        }
    }

    fun finishAuthorization(result: AuthorizationResult) {
        val serverAuthCode = result.serverAuthCode?.trim().orEmpty()
        if (serverAuthCode.isBlank()) {
            connecting = false
            checking = false
            message = "Google did not return a server authorization code. Reconnect Gmail and approve offline access."
            return
        }
        val grantedScope = result.grantedScopes.orEmpty().joinToString(" ")
        scope.launch {
            runCatching {
                GmailPluginApi.storeServerAuthCode(
                    serverAuthCode = serverAuthCode,
                    grantedScope = grantedScope,
                )
            }.onSuccess {
                connected = it.connected
                connectedEmailAddress = it.emailAddress
                checking = false
                connecting = false
                message = if (it.connected && it.profileSaved) {
                    "Gmail connected successfully. Indoone securely stored refreshable credentials and synced the connection to your Profile."
                } else {
                    "Gmail authorization completed, but the connection was not saved everywhere."
                }
            }.onFailure {
                checking = false
                connecting = false
                message = it.message ?: "Could not save Gmail authorization."
            }
        }
    }

    val authorizationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { activityResult ->
        try {
            val authorizationResult = Identity
                .getAuthorizationClient(context)
                .getAuthorizationResultFromIntent(activityResult.data)
            finishAuthorization(authorizationResult)
        } catch (error: ApiException) {
            connecting = false
            checking = false
            message = "Google authorization failed (${error.statusCode}): " +
                (error.message ?: "unknown Google authorization error.")
        } catch (error: Exception) {
            connecting = false
            checking = false
            message = error.message ?: "Google authorization failed."
        }
    }

    fun startAuthorization() {
        if (connecting) return
        connecting = true
        checking = true
        message = "Preparing secure Gmail authorization..."
        scope.launch {
            try {
                val serverClientId = GmailPluginApi.getGoogleServerClientId()
                val authorizationRequest = AuthorizationRequest.builder()
                    .setRequestedScopes(listOf(Scope(GmailPluginApi.GMAIL_SCOPE)))
                    .requestOfflineAccess(serverClientId)
                    .setPrompt(AuthorizationRequest.Prompt.CONSENT)
                    .build()

                Identity.getAuthorizationClient(context)
                    .authorize(authorizationRequest)
                    .addOnSuccessListener { authorizationResult ->
                        if (authorizationResult.hasResolution()) {
                            val pendingIntent = authorizationResult.pendingIntent
                            if (pendingIntent == null) {
                                connecting = false
                                checking = false
                                message = "Google authorization could not start."
                            } else {
                                authorizationLauncher.launch(
                                    IntentSenderRequest.Builder(pendingIntent.intentSender).build()
                                )
                            }
                        } else {
                            finishAuthorization(authorizationResult)
                        }
                    }
                    .addOnFailureListener { error ->
                        connecting = false
                        checking = false
                        message = error.message ?: "Could not start Google authorization."
                    }
            } catch (error: Exception) {
                connecting = false
                checking = false
                message = error.message ?: "Could not prepare secure Gmail authorization."
            }
        }
    }

    LaunchedEffect(Unit) {
        checkStatus()
    }

    // Painted white like the other screens: without it the black window behind the
    // app showed through.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "Back",
            color = Color(0xFF5E2DD2),
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 2.dp).clickable(onClick = onBack),
        )

        Text(
            text = "Gmail",
            color = Color(0xFF5E2DD2),
            fontSize = 24.sp,
        )

        Text(
            text = "Connect your Gmail account securely to Indoone.",
            color = Color(0xFF77707F),
            fontSize = 12.sp,
            lineHeight = 18.sp,
        )

        Button(
            onClick = { startAuthorization() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !checking && !connecting,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5E2DD2)),
        ) {
            Text(if (connected) "REAUTHORIZE GMAIL" else "CONNECT GMAIL")
        }

        if (checking || connecting) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 4.dp))
        }

        Text(
            text = when {
                connected && !connectedEmailAddress.isNullOrBlank() ->
                    "✓ Gmail is connected as ${connectedEmailAddress}. Indoone can use the permitted Gmail capabilities."
                connected -> "✓ Gmail is connected. Connection status is saved to your Indoone Profile."
                message.isNotBlank() -> message
                else -> "No Gmail account is connected yet."
            },
            color = if (connected) Color(0xFF287A46) else Color(0xFF4B4553),
            fontSize = 13.sp,
            lineHeight = 19.sp,
        )

        Text(
            text = "Google will ask you to approve Gmail permissions. Indoone sends a one-time server authorization code over HTTPS; the backend exchanges it and stores the access and refresh tokens in encrypted form. Tokens are not stored in your Firebase profile or on the phone.",
            color = Color(0xFF77707F),
            fontSize = 11.sp,
            lineHeight = 17.sp,
        )

        androidx.compose.material3.TextButton(onClick = onBack) {
            Text("Back")
        }
    }
}
