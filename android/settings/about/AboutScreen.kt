package com.indoone.settings.about

import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Text
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                ) {
                    IndooneAboutLogo(modifier = Modifier.size(48.dp))
                    Column(modifier = Modifier.padding(start = 10.dp)) {
                        Text(
                            "Indoone",
                            color = Color(0xFF5E2DD2),
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 18.sp,
                        )
                        Text(
                            "Authenticator",
                            color = Color(0xFF77717D),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    text = "Private authenticator with cloud sync and secure device pairing.\nUpdates are checked automatically when the app opens.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 0.dp),
                    textAlign = TextAlign.Center,
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
private fun IndooneAboutLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val scaleFactor = size.minDimension / 48f
        scale(scaleFactor) {
            rotate(45f, pivot = androidx.compose.ui.geometry.Offset(24f, 24f)) {
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFC15CFF), Color(0xFF7C3AED), Color(0xFF22C7FF)),
                        start = androidx.compose.ui.geometry.Offset(11f, 11f),
                        end = androidx.compose.ui.geometry.Offset(37f, 37f),
                    ),
                    topLeft = androidx.compose.ui.geometry.Offset(11f, 11f),
                    size = androidx.compose.ui.geometry.Size(26f, 26f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
                )
            }
            val outer = Path().apply {
                moveTo(24f, 14f); lineTo(27.2f, 20.8f); lineTo(34f, 24f); lineTo(27.2f, 27.2f)
                lineTo(24f, 34f); lineTo(20.8f, 27.2f); lineTo(14f, 24f); lineTo(20.8f, 20.8f); close()
            }
            drawPath(outer, color = Color(0xFF0A0A18))
            val inner = Path().apply {
                moveTo(24f, 20.8f); lineTo(25.2f, 22.8f); lineTo(27.2f, 24f); lineTo(25.2f, 25.2f)
                lineTo(24f, 27.2f); lineTo(22.8f, 25.2f); lineTo(20.8f, 24f); lineTo(22.8f, 22.8f); close()
            }
            drawPath(inner, color = Color(0xFF60A5FA))
        }
    }
}

@Composable
private fun AboutMetaRow(label: String, value: String) {
    Column(Modifier.fillMaxWidth()) {
        HorizontalDivider(color = Color(0xFFEEE8F4))
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
