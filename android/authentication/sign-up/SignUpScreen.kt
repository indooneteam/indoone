package com.indoone.authentication.signup

import androidx.compose.foundation.layout.Spacer
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
import com.indoone.authentication.AuthFieldLabel
import com.indoone.authentication.AuthHeading
import com.indoone.authentication.AuthPage
import com.indoone.authentication.AuthPrimaryButton
import com.indoone.authentication.AuthSecondaryButton
import com.indoone.authentication.AuthStatus
import com.indoone.authentication.AuthTextField

@Composable
fun SignUpScreen(
    busy: Boolean,
    status: String,
    error: String,
    otpVisible: Boolean,
    otpEmail: String,
    onSendOtp: (email: String, mobile: String, password: String) -> Unit,
    onVerifyOtp: (otp: String) -> Unit,
    onResendOtp: () -> Unit,
    onLogin: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var mobile by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var otp by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val otpFocusRequester = FocusRequester()

    val hasMobile = mobile.filter(Char::isDigit).isNotEmpty()
    val actionEnabled = !busy && if (otpVisible) otpEmail.isNotBlank() else email.isNotBlank() && mobile.isNotBlank() && password.length >= 6

    LaunchedEffect(otpVisible) {
        if (otpVisible) otpFocusRequester.requestFocus()
    }

    AuthPage {
        AuthBrand()
        Spacer(Modifier.height(44.dp))
        AuthHeading(
            eyebrow = "GET STARTED",
            title = "Create your account",
            description = "Securely create an Indoone account for your authenticator vault.",
        )
        Spacer(Modifier.height(20.dp))

        AuthFieldLabel("EMAIL ID")
        AuthTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = "you@example.com",
            enabled = !busy,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
        )

        Spacer(Modifier.height(14.dp))
        AuthFieldLabel("MOBILE NUMBER")
        AuthTextField(
            value = mobile,
            onValueChange = { mobile = it.filter { char -> char.isDigit() }.take(10) },
            placeholder = "98765 43210",
            enabled = !busy,
            leadingContent = if (hasMobile) {
                { Text("+91", fontSize = 13.sp) }
            } else null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
        )

        Spacer(Modifier.height(14.dp))
        AuthFieldLabel("PASSWORD")
        AuthTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = "Create a strong password",
            enabled = !busy,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingContent = {
                TextButton(onClick = { passwordVisible = !passwordVisible }, enabled = !busy) {
                    Text(if (passwordVisible) "◌" else "◉", fontSize = 17.sp)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        )

        Spacer(Modifier.height(20.dp))
        AuthPrimaryButton(
            text = when {
                busy && otpVisible -> "Sending…"
                busy -> "Sending OTP…"
                otpVisible -> "Resend OTP"
                else -> "Send OTP"
            },
            enabled = actionEnabled,
            onClick = { if (otpVisible) onResendOtp() else onSendOtp(email, "+91${mobile.filter(Char::isDigit).take(10)}", password) },
        )

        if (otpVisible) {
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            )
            Spacer(Modifier.height(4.dp))
            AuthPrimaryButton(
                text = if (busy) "Verifying…" else "Verify & Create Account",
                enabled = !busy && otp.length == 6,
                onClick = { onVerifyOtp(otp) },
            )
            AuthStatus(status, error = false)
        }

        AuthStatus(error, error = true)
        Spacer(Modifier.height(10.dp))
        AuthSecondaryButton("Already have an account? Login", !busy, onLogin)
        Spacer(Modifier.height(10.dp))
        Text(
            "Your Indoone account is activated after successful email OTP verification.",
            color = androidx.compose.ui.graphics.Color(0xFF76717D),
            fontSize = 12.sp,
            lineHeight = 18.sp,
        )
    }
}
