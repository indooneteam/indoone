package com.indoone.accounts.addaccount.entersetupkey

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Manual setup-key screen based on the current main account-add flow.
 */
@Composable
fun EnterSetupKeyScreen(
    state: EnterSetupKeyState,
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
                color = Color(0xFF242129),
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "ACCOUNT DETAILS",
            color = Color(0xFF7650D8),
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.2.sp,
        )

        Text(
            text = "Enter Setup Key",
            modifier = Modifier.padding(top = 3.dp),
            color = Color(0xFF242129),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = "Enter the details used to generate your one-time codes.",
            modifier = Modifier.padding(top = 10.dp),
            color = Color(0xFF2E2A33),
            fontSize = 14.sp,
            lineHeight = 20.sp,
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 22.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White,
            ),
            border = BorderStroke(1.dp, Color(0xFFE8E3EC)),
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

                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    if (maxWidth < 500.dp) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            SetupDropdown(
                                label = "DIGITS",
                                selected = state.digits.toString(),
                                options = listOf("6", "8"),
                                onSelected = { onDigitsChanged(it.toInt()) },
                            )
                            SetupDropdown(
                                label = "PERIOD",
                                selected = state.period.toString(),
                                options = listOf("30", "60"),
                                onSelected = { onPeriodChanged(it.toInt()) },
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            SetupDropdown(
                                modifier = Modifier.weight(1f),
                                label = "DIGITS",
                                selected = state.digits.toString(),
                                options = listOf("6", "8"),
                                onSelected = { onDigitsChanged(it.toInt()) },
                            )
                            SetupDropdown(
                                modifier = Modifier.weight(1f),
                                label = "PERIOD",
                                selected = state.period.toString(),
                                options = listOf("30", "60"),
                                onSelected = { onPeriodChanged(it.toInt()) },
                            )
                        }
                    }
                }

                SetupDropdown(
                    label = "ALGORITHM",
                    selected = state.algorithm,
                    options = listOf("SHA1", "SHA256", "SHA512"),
                    onSelected = onAlgorithmChanged,
                )

                state.errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = Color(0xFFB3261E),
                        fontSize = 12.sp,
                    )
                }

                Button(
                    onClick = onSave,
                    enabled = state.canSave,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(
                        text = if (state.isSaving) "Saving…" else "Save Account",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun SetupDropdown(
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
        modifier = modifier.fillMaxWidth(),
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
