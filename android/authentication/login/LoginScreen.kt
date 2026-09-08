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
    onSendOtp: (identifier: String, password: String) -> Unit = { _, _ -> },
    onVerifyOtp: (otp: String) -> Unit = {},
    onForgotPassword: () -> Unit = {},
    onCreateAccount: () -> Unit = {},
) {
    var identifier by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var otp by rememberSaveable { mutableStateOf("") }
    var otpVisible by rememberSaveable { mutableStateOf(false) }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp, vertical = 48.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Indoone",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = "Authenticator",
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(Modifier.height(44.dp))

        Text(
            text = "SECURE & PRIVATE",
            style = MaterialTheme.typography.labelSmall,
        )
        Text(
            text = "Welcome back",
            style = MaterialTheme.typography.headlineLarge,
        )
        Text(
            text = "Sign in to protect and sync your authenticator vault.",
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(Modifier.height(20.dp))

        Text(
            text = "EMAIL OR MOBILE NUMBER",
            style = MaterialTheme.typography.labelMedium,
        )
        OutlinedTextField(
            value = identifier,
            onValueChange = { identifier = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("you@example.com or 98765 43210") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
        )

        Spacer(Modifier.height(14.dp))

        Text(
            text = "PASSWORD",
            style = MaterialTheme.typography.labelMedium,
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Enter your password") },
            singleLine = true,
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                TextButton(onClick = { passwordVisible = !passwordVisible }) {
                    Text(if (passwordVisible) "Hide" else "Show")
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
        )

        TextButton(
            onClick = onForgotPassword,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Forgot password?")
        }

        Button(
            onClick = {
                onSendOtp(identifier.trim(), password)
                otpVisible = true
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Send OTP")
        }

        if (otpVisible) {
            Spacer(Modifier.height(8.dp))
            Text("OTP sent to ${identifier.ifBlank { "your account" }}")
            OutlinedTextField(
                value = otp,
                onValueChange = { value ->
                    otp = value.filter(Char::isDigit).take(6)
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("VERIFICATION OTP") },
                placeholder = { Text("Enter 6-digit OTP") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = { onVerifyOtp(otp) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Verify & Login")
            }
        }

        Spacer(Modifier.height(10.dp))

        TextButton(
            onClick = onCreateAccount,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Create Account")
        }

        Spacer(Modifier.height(10.dp))
        Text(
            text = "Protect your Indoone account with password and email OTP verification.",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
