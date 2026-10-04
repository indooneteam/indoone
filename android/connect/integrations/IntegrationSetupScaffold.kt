package com.indoone.connect.integrations

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun IntegrationSetupScaffold(
    platform: IntegrationPlatform,
    flowSteps: List<String>,
    platformNote: String,
    onBack: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                TextButton(onClick = onBack) {
                    Text("BACK")
                }
                Text(
                    text = platform.title,
                    modifier = Modifier.padding(start = 4.dp, top = 11.dp),
                    color = Color(0xFF2C2830),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Text(
                        text = "BUSINESS INTEGRATION ARCHITECTURE",
                        color = Color(0xFF2877E8),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.45.sp,
                    )
                    Text(
                        text = platform.title,
                        modifier = Modifier.padding(top = 5.dp),
                        color = Color(0xFF1F1B24),
                        fontSize = 27.sp,
                        lineHeight = 31.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = platform.description,
                        modifier = Modifier.padding(top = 8.dp),
                        color = Color(0xFF77717E),
                        fontSize = 12.sp,
                        lineHeight = 19.sp,
                    )
                }

                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFBFAFD),
                        border = BorderStroke(1.dp, Color(0xFFE8E5EE)),
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "ACCOUNT BOUNDARY",
                                color = Color(0xFF6F6876),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.4.sp,
                            )
                            Text(
                                text = platform.accountLabel,
                                modifier = Modifier.padding(top = 5.dp),
                                color = Color(0xFF2C2830),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "Each connection belongs to the signed-in Indoone user and is resolved again on the backend before any automation runs.",
                                modifier = Modifier.padding(top = 5.dp),
                                color = Color(0xFF77717E),
                                fontSize = 11.sp,
                                lineHeight = 17.sp,
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "FLOW",
                        modifier = Modifier.padding(top = 8.dp),
                        color = Color(0xFF6F6876),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp,
                    )
                }

                itemsIndexed(flowSteps) { index, step ->
                    IntegrationFlowStep(
                        number = index + 1,
                        text = step,
                    )
                }

                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF7F4FF),
                        border = BorderStroke(1.dp, Color(0xFFE3DCF7)),
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "PLATFORM RULE",
                                color = Color(0xFF5E2DD2),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.4.sp,
                            )
                            Text(
                                text = platformNote,
                                modifier = Modifier.padding(top = 5.dp),
                                color = Color(0xFF4F4859),
                                fontSize = 11.sp,
                                lineHeight = 17.sp,
                            )
                        }
                    }
                }

                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE8E5EE)),
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "SECURITY BOUNDARY",
                                color = Color(0xFF6F6876),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.4.sp,
                            )
                            Text(
                                text = "Provider access tokens stay on the backend. The Android app keeps only Indoone session state and the integration UI.",
                                modifier = Modifier.padding(top = 5.dp),
                                color = Color(0xFF77717E),
                                fontSize = 11.sp,
                                lineHeight = 17.sp,
                            )
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "The Meta authorization action will plug into IntegrationApi.startConnection(...) without changing the Connect screen or platform-specific UI.",
                        color = Color(0xFF89838F),
                        fontSize = 10.sp,
                        lineHeight = 15.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun IntegrationFlowStep(
    number: Int,
    text: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE8E5EE)),
    ) {
        Row(modifier = Modifier.padding(horizontal = 13.dp, vertical = 12.dp)) {
            Text(
                text = number.toString(),
                color = Color(0xFF5E2DD2),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = text,
                modifier = Modifier.padding(start = 12.dp),
                color = Color(0xFF4F4859),
                fontSize = 11.sp,
                lineHeight = 17.sp,
            )
        }
    }
}
