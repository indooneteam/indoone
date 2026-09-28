package com.indoone.authentication.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indoone.authentication.AuthBrand
import com.indoone.authentication.AuthBusyAction
import com.indoone.authentication.AuthFieldLabel
import com.indoone.authentication.AuthPage
import com.indoone.authentication.AuthTextField
import com.indoone.authentication.AuthPrimaryButton
import com.indoone.authentication.AuthSecondaryButton
import com.indoone.authentication.AuthStatus

@Composable
fun LoginScreen(
    busy: Boolean,
    busyAction: AuthBusyAction?,
    status: String,
    error: String,
    otpVisible: Boolean,
    otpEmail: String,
    onSendOtp: (identifier: String) -> Unit,
    onPasswordLogin: (identifier: String, password: String) -> Unit,
    onVerifyOtp: (otp: String) -> Unit,
    onResendOtp: () -> Unit,
    onCreateAccount: () -> Unit,
) {
    var identifier by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var otp by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var passwordMode by rememberSaveable { mutableStateOf(false) }
    val otpFocusRequester = FocusRequester()

    val mobileOnly = identifier.isNotBlank() && identifier.none(Char::isLetter) && identifier.none { it == '@' }
    val actionEnabled = when {
        busy -> false
        otpVisible -> otpEmail.isNotBlank()
        passwordMode -> identifier.isNotBlank() && password.isNotBlank()
        else -> identifier.isNotBlank()
    }

    LaunchedEffect(otpVisible) {
        if (otpVisible) otpFocusRequester.requestFocus()
    }

    AuthPage {
        AuthBrand()
        Spacer(Modifier.height(44.dp))

        val welcomeTexts = listOf(
            "Welcome to your Indoone account",
            "Continue with your Indoone account",
        )
        var welcomeTextIndex by rememberSaveable { mutableStateOf(0) }

        LaunchedEffect(Unit) {
            while (true) {
                kotlinx.coroutines.delay(2000L)
                welcomeTextIndex = (welcomeTextIndex + 1) % welcomeTexts.size
            }
        }

        androidx.compose.animation.Crossfade(
            targetState = welcomeTextIndex,
            animationSpec = androidx.compose.animation.core.tween(durationMillis = 350),
            label = "login_title",
        ) { index ->
                Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            brush = if (index == 1) {
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF6330DB),
                                        Color(0xFF9147ED),
                                    ),
                                )
                            } else null,
                            color = Color(0xFF17151D),
                        )
                    ) {
                        append(welcomeTexts[index])
                    }
                },
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1).sp,
                lineHeight = 40.sp,
                maxLines = 1,
                softWrap = false,
                overflow = androidx.compose.ui.text.style.TextOverflow.Clip,
            )
        }

        Spacer(Modifier.height(20.dp))

        AuthFieldLabel("EMAIL OR MOBILE NUMBER")
        AuthTextField(
            value = identifier,
            onValueChange = { identifier = it },
            placeholder = "you@example.com or 98765 43210",
            enabled = !busy && !otpVisible,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
            leadingContent = if (mobileOnly) {
                { Text("+91", fontSize = 13.sp) }
            } else null,
        )

        if (!otpVisible) {
            if (passwordMode) {
                Spacer(Modifier.height(14.dp))
                AuthFieldLabel("PASSWORD")
                AuthTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "Enter your password",
                    enabled = !busy,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingContent = {
                        TextButton(onClick = { passwordVisible = !passwordVisible }, enabled = !busy) {
                            Text(if (passwordVisible) "◌" else "◉", fontSize = 17.sp)
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                )

                Spacer(Modifier.height(20.dp))
                AuthPrimaryButton(
                    text = if (busyAction == AuthBusyAction.PASSWORD_LOGIN) "Logging in…" else "Complete Login",
                    enabled = actionEnabled,
                    onClick = { onPasswordLogin(identifier, password) },
                )
            } else {
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AuthPrimaryButton(
                        text = if (busyAction == AuthBusyAction.SEND_OTP) "Sending…" else "Continue with OTP",
                        enabled = actionEnabled,
                        onClick = { onSendOtp(identifier) },
                        modifier = Modifier.weight(1f),
                        fontSize = 12.sp,
                    )
                    AuthPrimaryButton(
                        text = "Continue with Password",
                        enabled = actionEnabled,
                        onClick = { passwordMode = true },
                        modifier = Modifier.weight(1f),
                        fontSize = 12.sp,
                    )
                }
            }
        } else {
            Spacer(Modifier.height(6.dp))
            Text(
                buildAnnotatedString {
                    append("OTP sent to ")
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append(otpEmail) }
                },
                fontSize = 12.sp,
            )
            Spacer(Modifier.height(12.dp))
            AuthFieldLabel("VERIFICATION OTP")
            AuthTextField(
                value = otp,
                onValueChange = { otp = it.filter(Char::isDigit).take(6) },
                placeholder = "Enter 6-digit OTP",
                enabled = !busy,
                modifier = Modifier.focusRequester(otpFocusRequester),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
            )
            Spacer(Modifier.height(4.dp))
            AuthPrimaryButton(
                text = if (busyAction == AuthBusyAction.VERIFY_OTP) "Verifying…" else "Verify & Login",
                enabled = !busy && otp.length == 6,
                onClick = { onVerifyOtp(otp) },
            )
            AuthStatus(status, error = false)
        }

        AuthStatus(error, error = true)
        Spacer(Modifier.height(10.dp))
        AuthSecondaryButton(
            text = "Create Account",
            enabled = !busy,
            onClick = onCreateAccount,
            textColor = Color(0xFF2E7D32),
        )
    }
}
