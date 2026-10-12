package com.indoone.menu.dataprivacy

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DataPrivacyMenuScreen(onBack: () -> Unit) {
    Surface(Modifier.fillMaxSize(), color = Color.White) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                }
                Text(
                    "Data & Privacy",
                    color = Color(0xFF211B29),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFFF5F9FF),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        modifier = Modifier.size(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFE3EEFF),
                    ) {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Outlined.PrivacyTip,
                                contentDescription = null,
                                tint = Color(0xFF2168D6),
                            )
                        }
                    }
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(
                            "Your data, your control",
                            color = Color(0xFF201B28),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "See what Indoone stores and how each part is used.",
                            modifier = Modifier.padding(top = 3.dp),
                            color = Color(0xFF6E6777),
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                        )
                    }
                }
            }

            PrivacyCard(
                icon = Icons.Outlined.PersonOutline,
                title = "Profile",
                body = "Name and other profile fields are user-controlled. Account email/mobile and existing authentication data stay in the existing Firebase structure.",
            )
            PrivacyCard(
                icon = Icons.Outlined.Memory,
                title = "AI Memory",
                body = "Only validated important long-term information should become memory. Name should not be silently changed by AI. Memory can be reviewed and deleted by the user.",
            )
            PrivacyCard(
                icon = Icons.Outlined.History,
                title = "Chat History",
                body = "Chats are account-linked and designed for multi-device sync. A conversation closes at 50 messages and the retention policy targets closed chats after 7 days of inactivity.",
            )
            PrivacyCard(
                icon = Icons.Outlined.Storage,
                title = "Storage",
                body = "Realtime Database stores small user/profile/memory data. Firestore stores chat conversations and messages. Large files can be added later through object storage without changing the account identity model.",
            )
            PrivacyCard(
                icon = Icons.Outlined.Security,
                title = "Security",
                body = "Reads and writes should be restricted to the authenticated user's UID. Destructive data actions will be added only after the server-side rules and retention worker are ready.",
            )

            Surface(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 10.dp),
                shape = RoundedCornerShape(15.dp),
                color = Color(0xFFF9F7FC),
                border = BorderStroke(1.dp, Color(0xFFE9E2F1)),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(13.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = null,
                        modifier = Modifier.size(19.dp),
                        tint = Color(0xFF756D80),
                    )
                    Text(
                        "This page reflects the current Indoone storage and security design. Future data-policy updates can be added as new sections without changing the existing layout.",
                        modifier = Modifier.padding(start = 9.dp),
                        color = Color(0xFF6E6777),
                        fontSize = 11.sp,
                        lineHeight = 17.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun PrivacyCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    body: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE9E2F1)),
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Surface(
                modifier = Modifier.size(38.dp),
                shape = RoundedCornerShape(11.dp),
                color = Color(0xFFF1F5FF),
            ) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.size(19.dp),
                        tint = Color(0xFF2168D6),
                    )
                }
            }
            Column(modifier = Modifier.padding(start = 11.dp)) {
                Text(
                    title,
                    color = Color(0xFF26202D),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    body,
                    modifier = Modifier.padding(top = 4.dp),
                    color = Color(0xFF6E6777),
                    fontSize = 11.sp,
                    lineHeight = 17.sp,
                )
            }
        }
    }
}
