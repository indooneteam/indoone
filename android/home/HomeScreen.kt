package com.indoone.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indoone.menu.AppBottomNav
import com.indoone.menu.AppTab
import com.indoone.menu.AppTopBar

private data class ChatMessage(
    val text: String,
    val fromUser: Boolean,
)

@Composable
fun HomeScreen(
    onMenuClick: () -> Unit,
    onLobbyClick: () -> Unit,
    onConnectClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    var input by remember { mutableStateOf("") }
    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage(
                    "Namaskara 👋 I’m Indoone AI. Ask me anything.",
                    fromUser = false,
                ),
            ),
        )
    }

    fun sendMessage() {
        val text = input.trim()
        if (text.isEmpty()) return
        messages = messages + ChatMessage(text, fromUser = true)
        input = ""
        messages = messages + ChatMessage(
            "Your message is ready for the Indoone AI backend. AI responses will appear here once the backend connection is enabled.",
            fromUser = false,
        )
    }

    Box(Modifier.fillMaxSize().background(Color.White)) {
        Column(Modifier.fillMaxSize()) {
            AppTopBar(
                onMenuClick = onMenuClick,
                trailingIcon = "✦",
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
            ) {
                Text(
                    "INDOONE AI",
                    color = Color(0xFF7650D8),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.3.sp,
                )
                Text(
                    "How can I help?",
                    modifier = Modifier.padding(top = 3.dp),
                    color = Color(0xFF17151D),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 4.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(messages) { message ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (message.fromUser) Arrangement.End else Arrangement.Start,
                    ) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = if (message.fromUser) Color(0xFF703BE2) else Color(0xFFF5F2FB),
                        ) {
                            Text(
                                message.text,
                                modifier = Modifier.padding(horizontal = 15.dp, vertical = 12.dp),
                                color = if (message.fromUser) Color.White else Color(0xFF292331),
                                fontSize = 13.sp,
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFFFAF9FC),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E0ED)),
                ) {
                    BasicTextField(
                        value = input,
                        onValueChange = { input = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 15.dp, vertical = 13.dp),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = Color(0xFF17151D),
                            fontSize = 14.sp,
                        ),
                        decorationBox = { innerTextField ->
                            Box {
                                if (input.isEmpty()) {
                                    Text("Message Indoone AI", color = Color(0xFF8A8492), fontSize = 14.sp)
                                }
                                innerTextField()
                            }
                        },
                    )
                }

                TextButton(
                    onClick = ::sendMessage,
                    modifier = Modifier.padding(start = 6.dp).size(54.dp),
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Text("↑", color = Color(0xFF703BE2), fontSize = 26.sp, fontWeight = FontWeight.Bold)
                }
            }

            AppBottomNav(
                activeTab = AppTab.HOME,
                onAccountsClick = {},
                onLobbyClick = onLobbyClick,
                onConnectClick = onConnectClick,
                onSettingsClick = onSettingsClick,
            )
        }
    }
}
