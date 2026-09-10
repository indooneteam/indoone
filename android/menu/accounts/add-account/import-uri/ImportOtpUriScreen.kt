package com.indoone.accounts.addaccount.importuri

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
 * OTPAuth import screen based on the current main add-account import flow.
 */
@Composable
fun ImportOtpUriScreen(
    state: ImportOtpUriState,
    onUriChanged: (String) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit,
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
            text = "ADD ACCOUNT",
            color = Color(0xFF7650D8),
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.2.sp,
        )

        Text(
            text = "Import OTPAUTH",
            modifier = Modifier.padding(top = 3.dp),
            color = Color(0xFF242129),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = "Paste an otpauth:// URI to continue.",
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
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE8E3EC)),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                OutlinedTextField(
                    value = state.uri,
                    onValueChange = onUriChanged,
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 6,
                    label = { Text("OTPAUTH URI") },
                    placeholder = { Text("otpauth://totp/...") },
                    singleLine = false,
                )

                Text(
                    text = state.errorMessage
                        ?: "The URI is processed on this device until you save the account.",
                    color = if (state.errorMessage == null) {
                        Color(0xFF77717F)
                    } else {
                        Color(0xFFB3261E)
                    },
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                )

                Button(
                    onClick = onContinue,
                    enabled = state.canContinue,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(
                        text = "Continue",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
