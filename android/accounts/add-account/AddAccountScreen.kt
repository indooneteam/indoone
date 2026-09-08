package com.indoone.accounts.addaccount

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Add Account method-selection screen.
 *
 * The layout adapts from two side-by-side options on wider screens to
 * a single-column layout on compact screens.
 */
@Composable
fun AddAccountScreen(
    state: AddAccountState,
    onOtpUriChanged: (String) -> Unit,
    onBack: () -> Unit,
    onScanQr: () -> Unit,
    onEnterSetupKey: () -> Unit,
    onImportOtpUri: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 10.dp),
    ) {
        TextButton(
            onClick = onBack,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 12.dp,
                vertical = 8.dp,
            ),
        ) {
            Text(
                text = "‹  Back",
                color = Color(0xFF242129),
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "NEW AUTHENTICATOR",
            color = Color(0xFF7650D8),
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.3.sp,
        )

        Text(
            text = "Add Account",
            modifier = Modifier.padding(top = 2.dp),
            color = Color(0xFF242129),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = "Choose how you want to add your TOTP account.",
            modifier = Modifier.padding(top = 11.dp),
            color = Color(0xFF2E2A33),
            fontSize = 14.sp,
            lineHeight = 20.sp,
        )

        Spacer(modifier = Modifier.height(22.dp))

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isCompact = maxWidth < 600.dp

            if (isCompact) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    AddAccountOptionCard(
                        icon = "▦",
                        title = "Scan QR Code",
                        description = "Use your camera to scan a TOTP QR code.",
                        onClick = onScanQr,
                    )
                    AddAccountOptionCard(
                        icon = "⌨",
                        title = "Enter Setup Key",
                        description = "Enter the secret key and account details manually.",
                        onClick = onEnterSetupKey,
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    AddAccountOptionCard(
                        modifier = Modifier.weight(1f),
                        icon = "▦",
                        title = "Scan QR Code",
                        description = "Use your camera to scan a TOTP QR code.",
                        onClick = onScanQr,
                    )
                    AddAccountOptionCard(
                        modifier = Modifier.weight(1f),
                        icon = "⌨",
                        title = "Enter Setup Key",
                        description = "Enter the secret key and account details manually.",
                        onClick = onEnterSetupKey,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF9FC)),
            border = BorderStroke(1.dp, Color(0xFFEEE9F1)),
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "PASTE OTPAUTH URI",
                    color = Color(0xFF66606E),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp,
                )

                OutlinedTextField(
                    value = state.otpUri,
                    onValueChange = onOtpUriChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    singleLine = true,
                    placeholder = {
                        Text(
                            text = "otpauth://totp/...",
                            color = Color(0xFF9B95A1),
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                )

                Button(
                    onClick = { onImportOtpUri(state.otpUri) },
                    enabled = state.isImportEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(
                        text = "Import OTP URI",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun AddAccountOptionCard(
    modifier: Modifier = Modifier,
    icon: String,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color(0xFFE8E3EC)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = icon, color = Color(0xFF302A38), fontSize = 28.sp, lineHeight = 30.sp)
            Text(text = title, color = Color(0xFF17141B), fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text(text = description, color = Color(0xFF77717F), fontSize = 12.sp, lineHeight = 18.sp)
        }
    }
}
