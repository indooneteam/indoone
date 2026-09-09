package com.indoone.menu.security

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SecurityScreen(
    onBack: () -> Unit,
    onAccountsClick: () -> Unit,
    onLobbyClick: () -> Unit,
    onConnectClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x8819141F)),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f),
            shape = RoundedCornerShape(25.dp),
            color = Color.White,
            shadowElevation = 14.dp,
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(23.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        "Security",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C2733),
                    )
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(35.dp)
                            .background(Color(0xFFF5F2F8), RoundedCornerShape(11.dp)),
                    ) {
                        Text("×", fontSize = 21.sp, color = Color(0xFF2C2733))
                    }
                }

                Spacer(Modifier.size(10.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                ) {
                    Text(
                        text = "When creating an account, the user provides email or mobile, password, and OTP. OTP is securely verified via IndoVerification system. Upon verification, the account is created securely in Firebase. During login, email or mobile, password, and OTP are entered again. The app features auto-lock, app lock, and biometric. Once enabled, the app is secure. Additionally, the accounts page prevents screenshots. This ensures complete security.",
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = Color(0xFF211D27),
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                    )
                }
            }
        }
    }
}
