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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun SignUpScreen(
    onCreateAccount: (String, String, String) -> Unit,
    onLogin: () -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var identifier by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 40.dp),
        verticalArrangement = Arrangement.Top,
    ) {
        Text(text = "Indoone")
        Text(text = "Authenticator")
        Spacer(modifier = Modifier.height(28.dp))
        Text(text = "SECURE & PRIVATE")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Create your account")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Create an Indoone account to protect and sync your authenticator vault.")
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Full name") },
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = identifier,
            onValueChange = { identifier = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email or mobile number") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                TextButton(onClick = { passwordVisible = !passwordVisible }) {
                    Text(if (passwordVisible) "Hide" else "Show")
                }
            },
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Confirm password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
        )
        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { onCreateAccount(name, identifier, password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = name.isNotBlank() && identifier.isNotBlank() && password.isNotBlank() && password == confirmPassword,
        ) {
            Text("Create Account")
        }
        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = onLogin,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Already have an account? Login")
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "Protect your Indoone account with password and email/mobile verification.")
    }
}
