package com.indoone.menu.plugins

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.Email
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

private data class PluginMenuEntry(
    val id: String,
    val title: String,
    val subtitle: String,
)

private val pluginMenuEntries = listOf(
    PluginMenuEntry(
        id = "email",
        title = "Email",
        subtitle = "Connect email providers and manage email capabilities.",
    ),
)

@Composable
fun PluginsMenuScreen(
    onBack: () -> Unit,
    onEmailClick: () -> Unit,
) {
    Surface(Modifier.fillMaxSize(), color = Color.White) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                }
                Text(
                    "Plugins",
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
                                Icons.Outlined.Apps,
                                contentDescription = null,
                                tint = Color(0xFF2168D6),
                            )
                        }
                    }
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(
                            "Connected tools",
                            color = Color(0xFF201B28),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "Available plugin capabilities appear here.",
                            modifier = Modifier.padding(top = 3.dp),
                            color = Color(0xFF6E6777),
                            fontSize = 12.sp,
                        )
                    }
                }
            }

            Text(
                "Available plugins",
                modifier = Modifier.padding(top = 20.dp, bottom = 9.dp),
                color = Color(0xFF201B28),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                pluginMenuEntries.forEach { plugin ->
                    PluginRow(
                        entry = plugin,
                        onClick = {
                            when (plugin.id) {
                                "email" -> onEmailClick()
                            }
                        },
                    )
                }
            }

            Spacer(Modifier.size(10.dp))

            Text(
                "New plugins added to this registry will use the same card layout automatically.",
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                color = Color(0xFF8A8392),
                fontSize = 11.sp,
            )
        }
    }
}

@Composable
private fun PluginRow(
    entry: PluginMenuEntry,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(15.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(13.dp),
                color = Color(0xFFF1F5FF),
            ) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = when (entry.id) {
                            "email" -> Icons.Outlined.Email
                            else -> Icons.Outlined.Apps
                        },
                        contentDescription = null,
                        tint = Color(0xFF2168D6),
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f).padding(start = 12.dp),
            ) {
                Text(
                    entry.title,
                    color = Color(0xFF26202D),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    entry.subtitle,
                    color = Color(0xFF77717F),
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(top = 3.dp),
                )
            }

            Icon(
                Icons.Outlined.ArrowForwardIos,
                contentDescription = "Open " + entry.title,
                modifier = Modifier.size(16.dp),
                tint = Color(0xFFAAA3B0),
            )
        }
    }
}
