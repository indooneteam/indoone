package com.indoone.accounts.accounts.edit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp

@Composable
fun EditAccountScreen(
    state: EditAccountState,
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
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 10.dp),
    ) {
        TextButton(onClick = onBack) {
            Text("‹  Back", fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(10.dp))
        Text("ACCOUNT DETAILS", color = Color(0xFF7650D8), fontSize = androidx.compose.ui.unit.sp(9f), fontWeight = FontWeight.ExtraBold)
        Text("Edit Account", modifier = Modifier.padding(top = 3.dp), fontSize = androidx.compose.ui.unit.sp(22f), fontWeight = FontWeight.Bold)
        Text("Update the details used to generate your one-time codes.", modifier = Modifier.padding(top = 10.dp), color = Color(0xFF77717F))

        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 22.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE8E3EC)),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(value = state.name, onValueChange = onNameChanged, modifier = Modifier.fillMaxWidth(), label = { Text("ACCOUNT NAME") }, placeholder = { Text("e.g. Google") }, singleLine = true)
                OutlinedTextField(value = state.email, onValueChange = onEmailChanged, modifier = Modifier.fillMaxWidth(), label = { Text("EMAIL / USERNAME") }, placeholder = { Text("you@example.com") }, singleLine = true)
                OutlinedTextField(value = state.secret, onValueChange = onSecretChanged, modifier = Modifier.fillMaxWidth(), label = { Text("SECRET KEY") }, placeholder = { Text("Base32 secret key") }, singleLine = true)

                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    if (maxWidth < 500.dp) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            EditDropdown("DIGITS", state.digits.toString(), listOf("6", "8")) { onDigitsChanged(it.toInt()) }
                            EditDropdown("PERIOD", state.period.toString(), listOf("30", "60")) { onPeriodChanged(it.toInt()) }
                        }
                    } else {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            EditDropdown("DIGITS", state.digits.toString(), listOf("6", "8"), Modifier.weight(1f)) { onDigitsChanged(it.toInt()) }
                            EditDropdown("PERIOD", state.period.toString(), listOf("30", "60"), Modifier.weight(1f)) { onPeriodChanged(it.toInt()) }
                        }
                    }
                }

                EditDropdown("ALGORITHM", state.algorithm, listOf("SHA1", "SHA256", "SHA512")) { onAlgorithmChanged(it) }

                state.errorMessage?.let { Text(it, color = Color(0xFFB3261E)) }
                Button(onClick = onSave, enabled = state.canSave, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                    Text(if (state.isSaving) "Saving…" else "Save Changes")
                }
            }
        }
    }
}

@Composable
private fun EditDropdown(
    label: String,
    selected: String,
    options: List<String>,
    modifier: Modifier = Modifier,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(value = selected, onValueChange = {}, readOnly = true, modifier = Modifier.fillMaxWidth().menuAnchor(), label = { Text(label) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) })
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { expanded = false; onSelected(option) }) }
        }
    }
}
