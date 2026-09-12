package com.indoone.settings.logout

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.indoone.authentication.AuthSessionStore
import kotlinx.coroutines.launch

@Composable
fun LogoutScreen(
    onDismiss: () -> Unit,
    onLoggedOut: () -> Unit,
) {
    var showDevice by remember { mutableStateOf(false) }
    if (showDevice) {
        LogoutThisDevice(
            onBack = { showDevice = false },
            onLoggedOut = onLoggedOut,
        )
        return
    }
    LogoutRoot(
        onBack = onDismiss,
        onThisDevice = { showDevice = true },
    )
}

@Composable
private fun LogoutRoot(onBack: () -> Unit, onThisDevice: () -> Unit) {
    LogoutModalShell(onBack, "Log out") {
        Text("Sign out from this device.", color = Color(0xFF8A8492), fontSize = 12.sp, lineHeight = 18.sp)
        Spacer(Modifier.height(12.dp))
        LogoutRow("Log out on this device", "Sign out only from this device", onThisDevice)
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            Text("Cancel", fontWeight = FontWeight.Bold, color = Color(0xFF6330DB))
        }
    }
}

@Composable
private fun LogoutRow(title: String, subtitle: String, onClick: () -> Unit) {
    Surface(Modifier.fillMaxWidth().clickable(onClick = onClick), color = Color.White, shape = RoundedCornerShape(16.dp)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(Modifier.size(42.dp), color = Color(0xFFF4F1F8), shape = RoundedCornerShape(13.dp)) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.size(24.dp)) {
                        val stroke = 1.8.dp.toPx()
                        val left = 5.dp.toPx()
                        val top = 8.dp.toPx()
                        val width = 14.dp.toPx()
                        val height = 11.dp.toPx()
                        drawRoundRect(
                            color = Color(0xFF5B5564),
                            topLeft = Offset(left, top),
                            size = Size(width, height),
                            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                            style = Stroke(width = stroke),
                        )
                        drawArc(
                            color = Color(0xFF5B5564),
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(8.dp.toPx(), 4.dp.toPx()),
                            size = Size(8.dp.toPx(), 8.dp.toPx()),
                            style = Stroke(width = stroke, cap = StrokeCap.Round),
                        )
                    }
                }
            }
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3A3442))
                Spacer(Modifier.height(3.dp))
                Text(subtitle, fontSize = 10.sp, lineHeight = 15.sp, color = Color(0xFF8A8492))
            }
            Text("›", fontSize = 24.sp, color = Color(0xFF8A8492), modifier = Modifier.padding(start = 10.dp))
        }
    }
}

@Composable
private fun LogoutThisDevice(onBack: () -> Unit, onLoggedOut: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }
    var working by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    LogoutModalShell(onBack, "Log out on this device") {
        Text("This signs you out only from this device.", color = Color(0xFF8A8492), fontSize = 12.sp, lineHeight = 18.sp)
        error?.let { Spacer(Modifier.height(10.dp)); Text(it, color = Color(0xFFB42318), fontSize = 12.sp, lineHeight = 18.sp) }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                if (working) return@Button
                working = true
                scope.launch {
                    runCatching {
                        clearLocalSession(context)
                        auth.signOut()
                    }.onSuccess { onLoggedOut() }
                        .onFailure { working = false; error = it.message ?: "Could not log out" }
                }
            },
            enabled = !working,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6330DB)),
        ) { Text("Log out on this device", fontWeight = FontWeight.Bold) }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onBack, enabled = !working, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            Text("Cancel", fontWeight = FontWeight.Bold, color = Color(0xFF6330DB))
        }
    }
}

@Composable
private fun LogoutModalShell(onBack: () -> Unit, title: String, content: @Composable () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0x5519141F))
            .clickable(onClick = onBack),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Surface(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp)
                .clickable(onClick = {}),
            color = Color.White,
            shadowElevation = 14.dp,
            shape = RoundedCornerShape(25.dp),
        ) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(title, Modifier.weight(1f), fontSize = 21.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1A22))
                    Box(Modifier.size(35.dp).background(Color(0xFFF5F2F8), RoundedCornerShape(11.dp)).clickable(onClick = onBack), contentAlignment = Alignment.Center) {
                        Text("×", fontSize = 23.sp, lineHeight = 23.sp, color = Color(0xFF5D5666))
                    }
                }
                Spacer(Modifier.height(14.dp))
                content()
            }
        }
    }
}

private fun clearLocalSession(context: Context) {
    context.getSharedPreferences("indoone_auth_session", Context.MODE_PRIVATE).edit().clear().apply()
    runCatching { AuthSessionStore(context).clear() }
}
