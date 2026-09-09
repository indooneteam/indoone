package com.indoone.settings.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private fun profileIcon(name: String, content: androidx.compose.ui.graphics.vector.PathBuilder.() -> Unit): ImageVector =
    ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.8f,
            strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
            strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round,
            pathBuilder = content,
        )
    }.build()

private val ProfileChevron = profileIcon("ProfileChevron") {
    moveTo(9f, 5f); lineTo(16f, 12f); lineTo(9f, 19f)
}

@Composable
fun ProfileScreen(
    state: ProfileState,
    onMenuClick: () -> Unit = {},
    onBack: () -> Unit,
    onMobileClick: () -> Unit,
    onEmailClick: () -> Unit,
    onAccountsClick: () -> Unit,
    onLobbyClick: () -> Unit,
    onConnectClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x8819141F))
            .clickable(onClick = onBack)
            .padding(14.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 430.dp)
                .fillMaxHeight(fraction = 0.88f)
                .clickable(onClick = {}),
            shape = RoundedCornerShape(25.dp),
            color = Color.White,
            shadowElevation = 14.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(23.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        "Profile",
                        color = Color(0xFF17151D),
                        fontSize = 21.sp,
                        lineHeight = 25.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Surface(
                        modifier = Modifier
                            .size(35.dp)
                            .clickable(onClick = onBack),
                        shape = RoundedCornerShape(11.dp),
                        color = Color(0xFFF5F2F8),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("×", color = Color(0xFF242129), fontSize = 21.sp, lineHeight = 21.sp)
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFEEE8F5), RoundedCornerShape(14.dp)),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFFAF8FD),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Surface(
                            modifier = Modifier.size(42.dp),
                            shape = CircleShape,
                            color = Color(0xFFEEE6FF),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "P",
                                    color = Color(0xFF6330DB),
                                    fontSize = 16.sp,
                                    lineHeight = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                state.email,
                                color = Color(0xFF17151D),
                                fontSize = 15.sp,
                                lineHeight = 18.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                state.mobile,
                                modifier = Modifier.padding(top = 4.dp),
                                color = Color(0xFF77707F),
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                ProfileActionRow(
                    title = "Change mobile number",
                    description = "Update your verified phone number",
                    onClick = onMobileClick,
                )
                Spacer(Modifier.height(10.dp))
                ProfileActionRow(
                    title = "Change email",
                    description = "Update your account email address",
                    onClick = onEmailClick,
                )

                state.error?.let {
                    Text(
                        it,
                        modifier = Modifier.padding(top = 14.dp),
                        color = Color(0xFFD93025),
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                    )
                }
                state.message?.let {
                    Text(
                        it,
                        modifier = Modifier.padding(top = 14.dp),
                        color = Color(0xFF6330DB),
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileActionRow(
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    color = Color(0xFF2C2733),
                    fontSize = 13.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    description,
                    modifier = Modifier.padding(top = 3.dp),
                    color = Color(0xFF8A8392),
                    fontSize = 12.sp,
                    lineHeight = 16.dp.value.sp,
                )
            }
            Icon(
                ProfileChevron,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = Color(0xFF9A92A1),
            )
        }
        HorizontalDivider(color = Color(0xFFEEE8F4), thickness = 1.dp)
    }
}