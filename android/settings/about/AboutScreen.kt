package com.indoone.settings.about

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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AboutScreen(
    onBack: () -> Unit,
    onAccountsClick: () -> Unit,
    onLobbyClick: () -> Unit,
    onConnectClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
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
            shape = RoundedCornerShape(25.dp),
            color = Color.White,
            shadowElevation = 14.dp,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 23.dp, vertical = 21.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "About Indoone",
                        modifier = Modifier.weight(1f),
                        color = Color(0xFF201C25),
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Box(
                        modifier = Modifier
                            .size(35.dp)
                            .background(Color(0xFFF5F2F8), RoundedCornerShape(11.dp))
                            .clickable(onClick = onBack),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("×", color = Color(0xFF5D5666), fontSize = 22.sp, lineHeight = 22.sp)
                    }
                }

                Spacer(Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.CenterHorizontally)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF6B2FE8), Color(0xFF8B43EC)),
                            ),
                            shape = RoundedCornerShape(9.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("I", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Black)
                }

                Text(
                    text = "Indoone Authenticator",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = Color(0xFF201C25),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                )

                Text(
                    text = "Private authenticator with cloud sync and secure device pairing.\nUpdates are checked automatically when the app opens.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 5.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = Color(0xFF756F7F),
                    fontSize = 12.sp,
                    lineHeight = 19.sp,
                )

                Spacer(Modifier.height(14.dp))
                AboutMetaRow("Version", "2.1.16")
                AboutMetaRow("Updates", "Automatic")

                Spacer(Modifier.height(1.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .padding(top = 0.dp)
                        .clickable(onClick = onBack),
                    shape = RoundedCornerShape(13.dp),
                    color = Color.Transparent,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(listOf(Color(0xFF6330DB), Color(0xFF9147ED))),
                                RoundedCornerShape(13.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Done", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    @Suppress("UNUSED_VARIABLE")
    val keepNavigationContract = onAccountsClick to onLobbyClick to onConnectClick to onSettingsClick
}

@Composable
private fun AboutMetaRow(label: String, value: String) {
    Column(Modifier.fillMaxWidth()) {
        androidx.compose.material3.HorizontalDivider(color = Color(0xFFEEE8F4))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(37.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, color = Color(0xFF756F7F), fontSize = 12.sp)
            Spacer(Modifier.weight(1f))
            Text(value, color = Color(0xFF24202A), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}
