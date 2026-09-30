package com.indoone.menu.plugins

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PluginsMenuScreen(
    onBack: () -> Unit,
    onEmailClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Back",
            color = Color(0xFF5E2DD2),
            fontSize = 14.sp,
            modifier = Modifier.clickable(onClick = onBack),
        )

        Text(
            text = "Plugins",
            color = Color(0xFF5E2DD2),
            fontSize = 24.sp,
            modifier = Modifier.padding(top = 4.dp),
        )

        Text(
            text = "Connected service plugins for Indoone.",
            color = Color(0xFF77707F),
            fontSize = 12.sp,
        )

        PluginRow(
            title = "Email",
            subtitle = "Email tools and providers",
            onClick = onEmailClick,
        )
    }
}

@Composable
private fun PluginRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.Email,
            contentDescription = null,
            tint = Color(0xFF5E2DD2),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp),
        ) {
            Text(
                text = title,
                color = Color(0xFF27212E),
                fontSize = 15.sp,
            )
            Text(
                text = subtitle,
                color = Color(0xFF77707F),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 3.dp),
            )
        }

        Icon(
            imageVector = Icons.Outlined.ArrowForwardIos,
            contentDescription = null,
            tint = Color(0xFF9A94A2),
        )
    }
}
