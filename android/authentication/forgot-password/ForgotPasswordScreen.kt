package com.indoone.authentication.forgotpassword

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun ForgotPasswordScreen(
    viewModel: ForgotPasswordViewModel,
    onBackToLogin: () -> Unit,
    onVerifyOtp: (identifier: String, otp: String) -> Unit,
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Indoone / Authenticator",
            style = MaterialTheme.typography.labelLarge,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Forgot password",
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Enter your email or mobile number to reset your Indoone account.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(
            value = state.identifier,
            onValueChange = viewModel::updateIdentifier,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email or mobile number") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = viewModel::requestOtp,
            modifier = Modifier.fillMaxWidth(),
            enabled = state.identifier.isNotBlank() && !state.loading,
        ) {
            Text("Send OTP")
        }

        if (state.otpSent) {
            Spacer(Modifier.height(20.dp))
            Text(
                text = "OTP sent to ${state.identifier}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.otp,
                onValueChange = viewModel::updateOtp,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("6-digit OTP") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { onVerifyOtp(state.identifier, state.otp) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.otp.length == 6 && !state.loading,
            ) {
                Text("Verify & Reset Password")
            }
        }

        Spacer(Modifier.height(12.dp))
        TextButton(
            onClick = onBackToLogin,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Back to Login")
        }
    }
}
