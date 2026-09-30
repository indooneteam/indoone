package com.indoone.menu.dataprivacy

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DataPrivacyMenuScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(onClick = onBack) { Text("Back") }
        Text("Data & Privacy", color = Color(0xFF5E2DD2), fontSize = 24.sp)
        Text("Control how Indoone handles profile data, AI memory, and chat history.", color = Color(0xFF77707F), fontSize = 12.sp)
        PrivacyCard("Profile", "Name and other profile fields are user-controlled. Account email/mobile and existing authentication data stay in the existing Firebase structure.")
        PrivacyCard("AI Memory", "Only validated important long-term information should become memory. Name should not be silently changed by AI. Memory can be reviewed and deleted by the user.")
        PrivacyCard("Chat History", "Chats are account-linked and designed for multi-device sync. A conversation closes at 50 messages and the retention policy targets closed chats after 7 days of inactivity.")
        PrivacyCard("Storage", "Realtime Database stores small user/profile/memory data. Firestore stores chat conversations and messages. Large files can be added later through object storage without changing the account identity model.")
        PrivacyCard("Security", "Reads and writes should be restricted to the authenticated user's UID. Destructive data actions will be added only after the server-side rules and retention worker are ready.")
    }
}

@Composable
private fun PrivacyCard(title: String, body: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFFAF8FD),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFFE8E1F0)),
    ) {
        Column(modifier = Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(title, color = Color(0xFF5E2DD2), fontSize = 14.sp)
            Text(body, color = Color(0xFF4B4553), fontSize = 12.sp, lineHeight = 17.sp)
        }
    }
}
