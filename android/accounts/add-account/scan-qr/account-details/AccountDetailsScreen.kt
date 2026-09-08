package com.indoone.accounts.addaccount.scanqr.accountdetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AccountDetailsScreen(
    state: AccountDetailsState,
    onNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onSecretChanged: (String) -> Unit,
    onDigitsChanged: (Int) -> Unit,
    onPeriodChanged: (Int) -> Unit,
    onAlgorithmChanged: (String) -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 10.dp),
    ) {
        TextButton(onClick = onBack) {
            Text(
                text = "‹  Back",
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.padding(top = 8.dp))

        Text(
            text = "ACCOUNT DETAILS",
            fontWeight = FontWeight.ExtraBold,
        )

        Text(
            text = "Enter Setup Key",
            modifier = Modifier.padding(top = 4.dp),
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = "Review the details used to generate your one-time codes.",
            modifier = Modifier.padding(top = 8.dp),
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = onNameChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("ACCOUNT NAME") },
                    placeholder = { Text("e.g. Google") },
                    singleLine = true,
                )

                OutlinedTextField(
                    value = state.email,
                    onValueChange = onEmailChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("EMAIL / USERNAME") },
                    placeholder = { Text("you@example.com") },
                    singleLine = true,
                )

                OutlinedTextField(
                    value = state.secret,
                    onValueChange = onSecretChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("SECRET KEY") },
                    placeholder = { Text("Base32 secret key") },
                    singleLine = true,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AccountDropdown(
                        label = "DIGITS",
                        selected = state.digits.toString(),
                        options = listOf("6", "8"),
                        onSelected = { onDigitsChanged(it.toInt()) },
                        modifier = Modifier.weight(1f),
                    )

                    AccountDropdown(
                        label = "PERIOD",
                        selected = state.period.toString(),
                        options = listOf("30", "60"),
                        onSelected = { onPeriodChanged(it.toInt()) },
                        modifier = Modifier.weight(1f),
                    )
                }

                AccountDropdown(
                    label = "ALGORITHM",
                    selected = state.algorithm,
                    options = listOf("SHA1", "SHA256", "SHA512"),
                    onSelected = onAlgorithmChanged,
                    modifier = Modifier.fillMaxWidth(),
                )

                state.errorMessage?.let { message ->
                    Text(text = message)
                }

                Button(
                    onClick = onSave,
                    enabled = state.canSave,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(
                        text = if (state.isSaving) "Saving…" else "Save Account",
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountDropdown(
    label: String,
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        expanded = false
                        onSelected(option)
                    },
                )
            }
        }
    }
}
