package com.indoone.authentication.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

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

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 48.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Indoone", style = MaterialTheme.typography.headlineSmall)
        Text("Authenticator", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(44.dp))
        Text("SECURE & PRIVATE", style = MaterialTheme.typography.labelSmall)
        Text("Welcome back", style = MaterialTheme.typography.headlineLarge)
        Text("Sign in to protect and sync your authenticator vault.", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(20.dp))

        Text("EMAIL OR MOBILE NUMBER", style = MaterialTheme.typography.labelMedium)
        OutlinedTextField(
            value = identifier,
            onValueChange = { identifier = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("you@example.com or 98765 43210") },
            singleLine = true,
            enabled = !otpVisible && !busy,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
        )

        Spacer(Modifier.height(14.dp))
        Text("PASSWORD", style = MaterialTheme.typography.labelMedium)
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Enter your password") },
            singleLine = true,
            enabled = !otpVisible && !busy,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                TextButton(onClick = { passwordVisible = !passwordVisible }, enabled = !busy) {
                    Text(if (passwordVisible) "Hide" else "Show")
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        )

        Spacer(Modifier.height(14.dp))
        Button(
            onClick = { onSendOtp(identifier, password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !busy && !otpVisible && identifier.isNotBlank() && password.isNotBlank(),
        ) {
            Text(if (busy) "Sending…" else "Send OTP")
        }

        if (otpVisible) {
            Spacer(Modifier.height(16.dp))
            Text(status.ifBlank { "OTP sent. Check your email." }, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = otp,
                onValueChange = { otp = it.filter(Char::isDigit).take(6) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("VERIFICATION OTP") },
                placeholder = { Text("Enter 6-digit OTP") },
                singleLine = true,
                enabled = !busy,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { onVerifyOtp(otp) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !busy && otp.length == 6,
            ) {
                Text(if (busy) "Verifying…" else "Verify & Login")
            }
            Spacer(Modifier.height(4.dp))
            TextButton(onClick = onResendOtp, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                Text("Resend OTP")
            }
        }

        if (error.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(10.dp))
        TextButton(onClick = onCreateAccount, modifier = Modifier.fillMaxWidth(), enabled = !busy) {
            Text("Create Account")
        }
        Spacer(Modifier.height(10.dp))
        Text("Protect your Indoone account with password and email OTP verification.", style = MaterialTheme.typography.bodySmall)
    }
}
