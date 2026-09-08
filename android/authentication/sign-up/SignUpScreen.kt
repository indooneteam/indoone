package com.indoone.authentication.signup

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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun SignUpScreen(
    busy: Boolean,
    status: String,
    error: String,
    otpVisible: Boolean,
    onSendOtp: (email: String, mobile: String, password: String) -> Unit,
    onVerifyOtp: (otp: String) -> Unit,
    onResendOtp: () -> Unit,
    onLogin: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var mobile by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var otp by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    val canSend = !busy && !otpVisible && email.isNotBlank() && mobile.isNotBlank() && password.length >= 6 && password == confirmPassword

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 40.dp),
        verticalArrangement = Arrangement.Top,
    ) {
        Text("Indoone", style = MaterialTheme.typography.headlineSmall)
        Text("Authenticator", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(36.dp))
        Text("GET STARTED", style = MaterialTheme.typography.labelSmall)
        Text("Create your account", style = MaterialTheme.typography.headlineLarge)
        Text("Securely create an Indoone account for your authenticator vault.", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(22.dp))

        Text("EMAIL ID", style = MaterialTheme.typography.labelMedium)
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("you@example.com") },
            singleLine = true,
            enabled = !otpVisible && !busy,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        Spacer(Modifier.height(12.dp))

        Text("MOBILE NUMBER", style = MaterialTheme.typography.labelMedium)
        OutlinedTextField(
            value = mobile,
            onValueChange = { mobile = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("98765 43210") },
            singleLine = true,
            enabled = !otpVisible && !busy,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        )
        Spacer(Modifier.height(12.dp))

        Text("PASSWORD", style = MaterialTheme.typography.labelMedium)
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Create a strong password") },
            singleLine = true,
            enabled = !otpVisible && !busy,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                TextButton(onClick = { passwordVisible = !passwordVisible }, enabled = !busy) {
                    Text(if (passwordVisible) "Hide" else "Show")
                }
            },
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Confirm password") },
            singleLine = true,
            enabled = !otpVisible && !busy,
            visualTransformation = PasswordVisualTransformation(),
        )
        Spacer(Modifier.height(18.dp))

        Button(
            onClick = { onSendOtp(email, mobile, password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = canSend,
        ) {
            Text(if (busy) "Sending OTP…" else "Send OTP")
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
                Text(if (busy) "Verifying…" else "Verify & Create Account")
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
        TextButton(onClick = onLogin, modifier = Modifier.fillMaxWidth(), enabled = !busy) {
            Text("Already have an account? Login")
        }
        Spacer(Modifier.height(10.dp))
        Text("Your Indoone account is activated after successful email OTP verification.", style = MaterialTheme.typography.bodySmall)
    }
}
