package com.indoone.authentication.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.PasswordVisualTransformation
import androidx.compose.material3.Text
import androidx.compose.material3.VisualTransformation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.indoone.authentication.AuthBrand
import com.indoone.authentication.AuthFieldLabel
import com.indoone.authentication.AuthHeading
import com.indoone.authentication.AuthPrimaryButton
import com.indoone.authentication.AuthSecondaryButton
import com.indoone.authentication.AuthStatus
import com.indoone.authentication.AuthTextField

@Composable
fun LoginScreen(
    busy: Boolean,
    status: String,
    error: String,
    otpVisible: Boolean,
    onSendOtp: (identifier: String, password: String) -> Unit,
    onVerifyOtp: (otp: String) -> Unit,
    onResendOtp: () -> Unit,
    onCreateAccount: () -> Unit,
) {
    var identifier by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var otp by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    val mobileOnly = identifier.isNotBlank() && identifier.none(Char::isLetter) && identifier.none { it == '@' }
    val actionEnabled = !busy && if (otpVisible) true else identifier.isNotBlank() && password.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 48.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        AuthBrand()
        Spacer(Modifier.padding(top = 44.dp))
        AuthHeading(
            eyebrow = "SECURE & PRIVATE",
            title = "Welcome back",
            description = "Sign in to protect and sync your authenticator vault.",
        )
        Spacer(Modifier.padding(top = 20.dp))

        AuthFieldLabel("EMAIL OR MOBILE NUMBER")
        AuthTextField(
            value = identifier,
            onValueChange = { identifier = it },
            placeholder = "you@example.com or 98765 43210",
            enabled = !busy,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
            leadingContent = if (mobileOnly) {
                { Text("+91", fontSize = 13.dp.value.spCompat(), fontWeight = FontWeight.Normal) }
            } else null,
        )

        Spacer(Modifier.padding(top = 14.dp))
        AuthFieldLabel("PASSWORD")
        AuthTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = "Enter your password",
            enabled = !busy,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingContent = {
                androidx.compose.material3.TextButton(onClick = { passwordVisible = !passwordVisible }, enabled = !busy) {
                    Text(if (passwordVisible) "◌" else "◉", fontSize = 17.sp)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        )

        Spacer(Modifier.padding(top = 20.dp))
        AuthPrimaryButton(
            text = when {
                busy && otpVisible -> "Sending…"
                busy -> "Checking…"
                otpVisible -> "Resend OTP"
                else -> "Send OTP"
            },
            enabled = actionEnabled,
            onClick = {
                if (otpVisible) onResendOtp() else onSendOtp(identifier, password)
            },
        )

        if (otpVisible) {
            Spacer(Modifier.padding(top = 6.dp))
            Text("OTP sent. Check your email.", fontSize = 12.sp)
            Spacer(Modifier.padding(top = 12.dp))
            AuthFieldLabel("VERIFICATION OTP")
            AuthTextField(
                value = otp,
                onValueChange = { otp = it.filter(Char::isDigit).take(6) },
                placeholder = "Enter 6-digit OTP",
                enabled = !busy,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            )
            Spacer(Modifier.padding(top = 4.dp))
            AuthPrimaryButton(
                text = if (busy) "Verifying…" else "Verify & Login",
                enabled = !busy && otp.length == 6,
                onClick = { onVerifyOtp(otp) },
            )
        }

        AuthStatus(error, error = true)
        Spacer(Modifier.padding(top = 10.dp))
        AuthSecondaryButton("Create Account", !busy, onCreateAccount)
        Spacer(Modifier.padding(top = 10.dp))
        Text(
            "Protect your Indoone account with password and email OTP verification.",
            color = androidx.compose.ui.graphics.Color(0xFF76717D),
            fontSize = 12.sp,
            lineHeight = 18.sp,
        )
    }
}

private fun Float.spCompat() = androidx.compose.ui.unit.sp
