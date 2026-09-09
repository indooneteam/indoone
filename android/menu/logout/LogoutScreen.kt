package com.indoone.menu.logout

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.indoone.authentication.AuthSessionStore
import kotlinx.coroutines.launch

private enum class LogoutView {
    ROOT,
    THIS_DEVICE,
}

@Composable
fun LogoutScreen(
    onDismiss: () -> Unit,
    onLoggedOut: () -> Unit,
) {
    var view by remember { mutableStateOf(LogoutView.ROOT) }

    when (view) {
        LogoutView.ROOT -> LogoutRoot(
            onBack = onDismiss,
            onThisDevice = { view = LogoutView.THIS_DEVICE },
        )

        LogoutView.THIS_DEVICE -> LogoutThisDevice(
            onBack = { view = LogoutView.ROOT },
            onLoggedOut = onLoggedOut,
        )
    }
}

@Composable
private fun LogoutRoot(
    onBack: () -> Unit,
    onThisDevice: () -> Unit,
) {
    LogoutModalShell(
        onBack = onBack,
        title = "Log out",
    ) {
        Text(
            text = "Sign out from this device.",
            color = Color(0xFF8A8492),
            fontSize = 12.sp,
            lineHeight = 18.sp,
        )

        Spacer(Modifier.height(12.dp))

        LogoutRow(
            title = "Log out on this device",
            subtitle = "Sign out only from this device",
            onClick = onThisDevice,
        )

        Spacer(Modifier.height(12.dp))

        TextButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) {
            Text(
                "Cancel",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6330DB),
            )
        }
    }
}

@Composable
private fun LogoutRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                color = Color(0xFFF4F1F8),
                shape = RoundedCornerShape(13.dp),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "▣",
                        color = Color(0xFF5B5564),
                        fontSize = 20.sp,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
            ) {
                Text(
                    title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3A3442),
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    subtitle,
                    fontSize = 10.sp,
                    lineHeight = 15.sp,
                    color = Color(0xFF8A8492),
                )
            }

            Text(
                "›",
                fontSize = 24.sp,
                color = Color(0xFF8A8492),
                modifier = Modifier.padding(start = 10.dp),
            )
        }
    }
}

@Composable
private fun LogoutThisDevice(
    onBack: () -> Unit,
    onLoggedOut: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }
    var working by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LogoutModalShell(
        onBack = onBack,
        title = "Log out on this device",
    ) {
        Text(
            text = "This signs you out only from this device.",
            color = Color(0xFF8A8492),
            fontSize = 12.sp,
            lineHeight = 18.sp,
        )

        error?.let {
            Spacer(Modifier.height(10.dp))
            Text(
                it,
                color = Color(0xFFB42318),
                fontSize = 12.sp,
                lineHeight = 18.sp,
            )
        }

        Spacer(Modifier.height(16.dp))

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
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6330DB),
            ),
        ) {
            Text(
                "Log out on this device",
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = onBack,
            enabled = !working,
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) {
            Text(
                "Cancel",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6330DB),
            )
        }
    }
}

@Composable
private fun LogoutModalShell(
    onBack: () -> Unit,
    title: String,
    content: @Composable Column.() -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x8819141F)),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 14.dp,
            shape = RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        title,
                        modifier = Modifier.weight(1f),
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1A22),
                    )

                    TextButton(
                        onClick = onBack,
                        modifier = Modifier.size(35.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                    ) {
                        Surface(
                            modifier = Modifier.size(35.dp),
                            color = Color(0xFFF5F2F8),
                            shape = RoundedCornerShape(11.dp),
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    "×",
                                    fontSize = 23.sp,
                                    lineHeight = 23.sp,
                                    color = Color(0xFF5D5666),
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))
                content()
            }
        }
    }
}

private fun clearLocalSession(context: Context) {
    context.filesDir.resolve("accounts.enc").delete()
    AuthSessionStore(context).clear()
    context.getSharedPreferences("indoone_app_lock", Context.MODE_PRIVATE).edit().clear().apply()
    context.getSharedPreferences("indoone_biometric_unlock", Context.MODE_PRIVATE).edit().clear().apply()
    context.getSharedPreferences("indoone_auto_lock", Context.MODE_PRIVATE).edit().clear().apply()
}
