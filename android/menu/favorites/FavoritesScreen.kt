package com.indoone.menu.favorites

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indoone.accounts.AccountRecord
import com.indoone.accounts.accounts.list.AccountTotpGenerator
import com.indoone.menu.AppBottomNav
import com.indoone.menu.AppTab
import com.indoone.menu.AppTopBar
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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
            AppTopBar(onMenuClick = onBack)

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                item {
                    Text("FAVORITES", color = Color(0xFF7650D8), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.3.sp)
                    Text("Favorite accounts", modifier = Modifier.padding(top = 3.dp), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Quick access to the accounts you starred.", modifier = Modifier.padding(top = 5.dp, bottom = 4.dp), color = Color(0xFF85808B), fontSize = 12.sp)
                    Surface(shape = RoundedCornerShape(11.dp), color = Color(0xFFFAF9FD), border = BorderStroke(1.dp, Color(0xFFEEEAF4)), modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(horizontal = 11.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("★", color = Color(0xFFF1A900), fontSize = 13.sp)
                            Text("${favorites.size} favorite account${if (favorites.size == 1) "" else "s"}", modifier = Modifier.padding(start = 8.dp), color = Color(0xFF6E6878), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (favorites.isEmpty()) {
                    item { EmptyFavoritesState() }
                } else {
                    items(favorites, key = { it.id }) { account ->
                        FavoriteAccountRow(account, nowMillis, { onAccountClick(account) }, { onToggleFavorite(account) })
                    }
                }
            }

            AppBottomNav(AppTab.SETTINGS, onAccountsClick, onLobbyClick, onConnectClick, onSettingsClick)
        }
    }
}

@Composable
private fun FavoriteAccountRow(account: AccountRecord, nowMillis: Long, onClick: () -> Unit, onToggleFavorite: () -> Unit) {
    val period = account.period.coerceAtLeast(1)
    val nowSeconds = nowMillis / 1000L
    val elapsed = (nowSeconds % period).toInt()
    val secondsRemaining = (period - elapsed).coerceIn(1, period)
    val code = runCatching { AccountTotpGenerator.generate(account.secret, nowMillis, period, account.digits, account.algorithm) }.getOrDefault("------")

    Surface(modifier = Modifier.fillMaxWidth(), onClick = onClick, shape = RoundedCornerShape(18.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFECE9F0)), shadowElevation = 2.dp) {
        Row(modifier = Modifier.fillMaxWidth().padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).background(Color(0xFFF5F3F8), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                Text(account.name.firstOrNull()?.uppercase() ?: "A", color = Color(0xFF4285F4), fontSize = 21.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(account.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(account.email, modifier = Modifier.padding(top = 3.dp), color = Color(0xFF89838F), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(code, modifier = Modifier.padding(top = 4.dp), color = Color(0xFF6331DB), fontSize = 21.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            TextButton(onClick = onToggleFavorite) { Text("★", color = Color(0xFFF1A900), fontSize = 22.sp) }
            CountdownRing(secondsRemaining, period)
        }
    }
}

@Composable
private fun CountdownRing(secondsRemaining: Int, periodSeconds: Int) {
    val progress = (secondsRemaining.toFloat() / periodSeconds.coerceAtLeast(1)).coerceIn(0f, 1f)
    Box(modifier = Modifier.size(35.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(color = Color(0xFFEADFFB), style = Stroke(width = 3.dp.toPx()))
            drawArc(color = Color(0xFF703BE2), startAngle = -90f, sweepAngle = progress * 360f, useCenter = false, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
        }
        Text(secondsRemaining.toString(), color = Color(0xFF703BE2), fontSize = 10.sp)
    }
}

@Composable
private fun EmptyFavoritesState() {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 54.dp, horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(64.dp).background(Color(0xFFF3EDFF), RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) {
            Text("★", color = Color(0xFF7140DA), fontSize = 28.sp)
        }
        Text("No favorite accounts", modifier = Modifier.padding(top = 14.dp), fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Text("Star an account on the Accounts page and it will appear here.", modifier = Modifier.padding(top = 5.dp), color = Color(0xFF85808B), fontSize = 13.sp)
    }
}
