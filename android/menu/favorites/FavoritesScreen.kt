package com.indoone.menu.favorites

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.foundation.Canvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indoone.accounts.AccountRecord
import com.indoone.accounts.accounts.list.AccountTotpGenerator
import kotlinx.coroutines.delay

@Composable
fun FavoritesScreen(
    accounts: List<AccountRecord>,
    onBack: () -> Unit,
    onAccountClick: (AccountRecord) -> Unit,
    onToggleFavorite: (AccountRecord) -> Unit,
    onAccountsClick: () -> Unit,
    onLobbyClick: () -> Unit,
    onConnectClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    var nowMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(1_000L)
        }
    }

    val favorites = accounts
        .filter { it.favorite }
        .sortedBy { it.name.lowercase() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x5519141F))
            .clickable(onClick = onBack),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 5.dp, end = 5.dp, bottom = 14.dp)
                .clickable(onClick = {}),
            shape = RoundedCornerShape(25.dp),
            color = Color.White,
            shadowElevation = 14.dp,
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 21.dp, vertical = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Favorites",
                        modifier = Modifier.weight(1f),
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

                if (favorites.isEmpty()) {
                    EmptyFavoritesState()
                } else {
                    Spacer(Modifier.size(10.dp))
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.72f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(favorites, key = { it.id }) { account ->
                            FavoriteAccountRow(
                                account = account,
                                nowMillis = nowMillis,
                                onClick = { onAccountClick(account) },
                                onToggleFavorite = { onToggleFavorite(account) },
                            )
                        }
                    }
                }
            }
        }
    }

    @Suppress("UNUSED_VARIABLE")
    val keepNavigationContract = onAccountsClick to onLobbyClick to onConnectClick to onSettingsClick
}

@Composable
private fun FavoriteAccountRow(
    account: AccountRecord,
    nowMillis: Long,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val period = account.period.coerceAtLeast(1)
    val code = runCatching {
        AccountTotpGenerator.generate(
            account.secret,
            nowMillis,
            period,
            account.digits,
            account.algorithm,
        )
    }.getOrDefault("------")

    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE9E5EF)),
        shadowElevation = 3.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 11.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Color(0xFFF4F1F8), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    account.name.firstOrNull()?.uppercase() ?: "A",
                    color = Color(0xFF4285F4),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(Modifier.width(11.dp))

            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    account.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF211D27),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    account.email,
                    modifier = Modifier.padding(top = 4.dp),
                    color = Color(0xFF8A8490),
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(9.dp),
                    color = Color(0xFFF3EDFF),
                ) {
                    Text(
                        code,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                        color = Color(0xFF6331DB),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                    )
                }
                Text("›", modifier = Modifier.clickable(onClick = onClick).padding(horizontal = 8.dp, vertical = 4.dp), color = Color(0xFFAAA2B4), fontSize = 22.sp)
            }
        }
    }
}

@Composable
private fun EmptyFavoritesState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 14.dp, end = 14.dp, top = 32.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(Color(0xFFF0EAFF), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.size(28.dp)) {
                val path = Path().apply {
                    moveTo(size.width / 2f, 2f)
                    lineTo(size.width * 0.62f, size.height * 0.36f)
                    lineTo(size.width - 2f, size.height * 0.5f)
                    lineTo(size.width * 0.62f, size.height * 0.64f)
                    lineTo(size.width / 2f, size.height - 2f)
                    lineTo(size.width * 0.38f, size.height * 0.64f)
                    lineTo(2f, size.height * 0.5f)
                    lineTo(size.width * 0.38f, size.height * 0.36f)
                    close()
                }
                drawPath(path, color = Color(0xFF7140DA))
            }
        }
        Text(
            "No favorite accounts",
            modifier = Modifier.padding(top = 12.dp),
            color = Color(0xFF211D27),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            "Star an account on the Home page and it will appear here.",
            modifier = Modifier.padding(top = 5.dp),
            color = Color(0xFF85808B),
            fontSize = 12.sp,
        )
    }
}
