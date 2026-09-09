package com.indoone.settings.autolock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

@Composable
fun AutoLockScreen(
    currentMinutes: Int,
    appLockEnabled: Boolean,
    biometricEnabled: Boolean,
    onSelectMinutes: (Int) -> Unit,
    onBack: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp),
        color = Color.White,
        shadowElevation = 14.dp,
    ) {
        Column(
            modifier = Modifier.padding(23.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Auto-Lock", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2C2733))
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .clip(RoundedCornerShape(11.dp)),
                ) {
                    Text("×", fontSize = 21.sp, color = Color(0xFF2C2733))
                }
            }

            Text(
                "Automatically lock the encrypted vault after you stop using Indoone.",
                modifier = Modifier.padding(top = 7.dp),
                fontSize = 14.sp,
                lineHeight = 21.sp,
                color = Color(0xFF756F7F),
            )

            Spacer(Modifier.padding(top = 16.dp))

            listOf(
                0 to "Never",
                1 to "After 1 minute",
                5 to "After 5 minutes",
                15 to "After 15 minutes",
            ).forEach { (minutes, label) ->
                val selected = currentMinutes == minutes
                Surface(
                    onClick = { onSelectMinutes(minutes) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (selected) Color(0xFFFAF7FF) else Color.White,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (selected) Color(0xFFCBB8EC) else Color(0xFFE6E0EE),
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            label,
                            fontSize = 14.sp,
                            color = if (selected) Color(0xFF5C2AC7) else Color(0xFF2C2733),
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        )
                        Text("›", fontSize = 24.sp, color = Color(0xFF9A92A1))
                    }
                }
            }
        }
    }
}
