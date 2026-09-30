package com.indoone.menu.plugins.email.gmail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GmailPluginMenuScreen(
    onBack: () -> Unit,
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
            text = "Gmail",
            color = Color(0xFF5E2DD2),
            fontSize = 24.sp,
            modifier = Modifier.padding(top = 4.dp),
        )

        Text(
            text = "Gmail plugin foundation.",
            color = Color(0xFF77707F),
            fontSize = 12.sp,
        )

        Text(
            text = "Gmail connection, mailbox access, email actions, and safety controls will be implemented here.",
            color = Color(0xFF4B4553),
            fontSize = 13.sp,
            lineHeight = 18.sp,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
