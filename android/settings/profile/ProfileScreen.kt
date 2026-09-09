package com.indoone.settings.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.indoone.settings.SettingsScreen

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

private enum class ProfileSheet {
    NONE,
    MOBILE,
    EMAIL,
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
    val profileViewModel: ProfileViewModel = viewModel()
    var sheet by remember { mutableStateOf(ProfileSheet.NONE) }
    var mobile by remember(state.mobile, sheet) {
        mutableStateOf(state.mobile.takeUnless { it.contains("not set", true) }.orEmpty())
    }
    var email by remember(state.email, sheet) {
        mutableStateOf(state.email.takeUnless { it.contains("not available", true) }.orEmpty())
    }
    var password by remember(sheet) { mutableStateOf("") }

    Box(Modifier.fillMaxSize()) {
        SettingsScreen(
            onMenuClick = onMenuClick,
            onProfileClick = {},
            onAccountsClick = onAccountsClick,
            onLobbyClick = onLobbyClick,
            onConnectClick = onConnectClick,
            onSettingsClick = onSettingsClick,
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x8819141F))
                .clickable(onClick = if (sheet == ProfileSheet.NONE) onBack else { { sheet = ProfileSheet.NONE } }),
            contentAlignment = Alignment.BottomCenter,
        ) {
            if (sheet == ProfileSheet.NONE) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 430.dp)
                        .wrapContentHeight()
                        .padding(bottom = 14.dp)
                        .clickable(onClick = {}),
                    shape = RoundedCornerShape(25.dp),
                    color = Color.White,
                    shadowElevation = 14.dp,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 22.dp, vertical = 19.dp),
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
                            onClick = {
                                mobile = state.mobile.takeUnless { it.contains("not set", true) }.orEmpty()
                                sheet = ProfileSheet.MOBILE
                            },
                        )
                        Spacer(Modifier.height(10.dp))
                        ProfileActionRow(
                            title = "Change email",
                            description = "Update your account email address",
                            onClick = {
                                email = state.email.takeUnless { it.contains("not available", true) }.orEmpty()
                                password = ""
                                sheet = ProfileSheet.EMAIL
                            },
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
            } else {
                when (sheet) {
                    ProfileSheet.MOBILE -> {
                        ChangeMobileSheet(
                            value = mobile,
                            busy = state.busy,
                            error = state.error,
                            message = state.message,
                            onValueChange = { mobile = it },
                            onClose = { sheet = ProfileSheet.NONE },
                            onSave = { profileViewModel.updateMobile(mobile) },
                        )
                    }
                    ProfileSheet.EMAIL -> {
                        ChangeEmailSheet(
                            email = email,
                            password = password,
                            busy = state.busy,
                            error = state.error,
                            message = state.message,
                            onEmailChange = { email = it },
                            onPasswordChange = { password = it },
                            onClose = { sheet = ProfileSheet.NONE },
                            onSave = { profileViewModel.updateEmail(email, password) },
                        )
                    }
                    ProfileSheet.NONE -> Unit
                }
            }
        }
    }
}

@Composable
private fun ChangeMobileSheet(
    value: String,
    busy: Boolean,
    error: String?,
    message: String?,
    onValueChange: (String) -> Unit,
    onClose: () -> Unit,
    onSave: () -> Unit,
) {
    ProfileFormSheet(
        title = "Change mobile number",
        description = "Update the mobile number saved to your Indoone account.",
        onClose = onClose,
    ) {
        SheetFieldLabel("Mobile number")
        SheetTextField(value, onValueChange)
        Spacer(Modifier.height(24.dp))
        SheetGradientButton(if (busy) "Updating…" else "Update mobile number", enabled = !busy, onClick = onSave)
        FormFeedback(error, message)
    }
}

@Composable
private fun ChangeEmailSheet(
    email: String,
    password: String,
    busy: Boolean,
    error: String?,
    message: String?,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onClose: () -> Unit,
    onSave: () -> Unit,
) {
    ProfileFormSheet(
        title = "Change email",
        description = "Change the email used for your Indoone account. Enter your current password to confirm.",
        onClose = onClose,
    ) {
        SheetFieldLabel("Email address")
        SheetTextField(email, onEmailChange)
        Spacer(Modifier.height(12.dp))
        SheetFieldLabel("Current password")
        SheetTextField(password, onPasswordChange, placeholder = "Enter your current password", password = true)
        Spacer(Modifier.height(24.dp))
        SheetGradientButton(if (busy) "Changing…" else "Change email", enabled = !busy, onClick = onSave)
        FormFeedback(error, message)
    }
}

@Composable
private fun ProfileFormSheet(
    title: String,
    description: String,
    onClose: () -> Unit,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(bottom = 14.dp)
            .clickable(onClick = {}),
        shape = RoundedCornerShape(25.dp),
        color = Color.White,
        shadowElevation = 14.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 19.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(title, color = Color(0xFF17151D), fontSize = 20.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold)
                Surface(
                    modifier = Modifier.size(35.dp).clickable(onClick = onClose),
                    shape = RoundedCornerShape(11.dp),
                    color = Color(0xFFF5F2F8),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("×", color = Color(0xFF242129), fontSize = 21.sp, lineHeight = 21.sp)
                    }
                }
            }
            Text(
                description,
                modifier = Modifier.padding(top = 17.dp, bottom = 14.dp),
                color = Color(0xFF77707F),
                fontSize = 12.sp,
                lineHeight = 18.sp,
            )
            content()
        }
    }
}

@Composable
private fun SheetFieldLabel(text: String) {
    Text(text, color = Color(0xFF625D68), fontSize = 9.sp, lineHeight = 11.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(5.dp))
}

@Composable
private fun SheetTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    password: Boolean = false,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(41.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFAF8FD))
            .border(1.dp, Color(0xFFE4DDEA), RoundedCornerShape(12.dp))
            .padding(horizontal = 11.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(color = Color(0xFF2C2733), fontSize = 12.sp, lineHeight = 16.sp),
            visualTransformation = if (password) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            cursorBrush = SolidColor(Color(0xFF6330DB)),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                if (value.isEmpty() && placeholder.isNotEmpty()) {
                    Text(placeholder, color = Color(0xFF9A92A1), fontSize = 12.sp, lineHeight = 16.sp)
                }
                inner()
            },
        )
    }
}

@Composable
private fun SheetGradientButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(
                if (enabled) Brush.horizontalGradient(listOf(Color(0xFF632FE5), Color(0xFF9648EE)))
                else SolidColor(Color(0xFFB9A9D3)),
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = Color.White, fontSize = 12.sp, lineHeight = 15.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FormFeedback(error: String?, message: String?) {
    error?.let {
        Text(it, Modifier.padding(top = 12.dp), color = Color(0xFFD93025), fontSize = 11.sp)
    }
    message?.let {
        Text(it, Modifier.padding(top = 12.dp), color = Color(0xFF6330DB), fontSize = 11.sp)
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
                    lineHeight = 16.sp,
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