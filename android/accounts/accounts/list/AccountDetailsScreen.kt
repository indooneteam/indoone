package com.indoone.accounts.accounts.list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indoone.accounts.AccountRecord

@Composable
fun AccountDetailsScreen(
    account: AccountRecord,
    code: String,
    secondsRemaining: Int,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onCopy: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
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

        Text(
            "ACCOUNT",
            color = Color(0xFF7650D8),
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.2.sp,
        )
        Text(
            account.name,
            modifier = Modifier.padding(top = 3.dp),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            account.email.ifBlank { "Authenticator account" },
            modifier = Modifier.padding(top = 8.dp),
            color = Color(0xFF77717F),
            fontSize = 13.sp,
        )

        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 22.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE8E3EC)),
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    "ONE-TIME CODE",
                    color = Color(0xFF77717F),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                )
                Text(code, fontSize = 32.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Text(
                    "Code expires in ${secondsRemaining.coerceAtLeast(0)}s",
                    color = Color(0xFF77717F),
                    fontSize = 12.sp,
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Button(
                        onClick = onCopy,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                    ) { Text("Copy code") }
                    Button(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                    ) { Text("Edit") }
                }

                OutlinedButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(if (account.favorite) "Remove from favorites" else "Add to favorites")
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF9FC)),
            border = BorderStroke(1.dp, Color(0xFFEEE9F1)),
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DetailLine("TYPE", "TOTP")
                DetailLine("PERIOD", "${account.period} seconds")
                DetailLine("ALGORITHM", account.algorithm)
                DetailLine("DIGITS", "${account.digits} digits")
            }
        }

        OutlinedButton(
            onClick = onDelete,
            modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
            shape = RoundedCornerShape(14.dp),
        ) { Text("Delete account", color = Color(0xFFB3261E)) }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Column {
        Text(label, color = Color(0xFF8A8491), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.8.sp)
        Text(value, modifier = Modifier.padding(top = 3.dp), fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
