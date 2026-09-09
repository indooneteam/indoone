package com.indoone.accounts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.Canvas
import com.indoone.menu.AppBottomNav
import com.indoone.menu.AppSearchIcon
import com.indoone.menu.AppTab
import com.indoone.menu.AppTopBar

@Composable
fun AccountsScreen(
    state: AccountsState,
    onSearchChanged: (String) -> Unit = {},
    onClearSearch: () -> Unit = {},
    onSort: () -> Unit = {},
    onToggleFavorite: (String) -> Unit = {},
    onAccountClick: (AccountItem) -> Unit = {},
    onAddAccount: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onLobbyClick: () -> Unit = {},
    onConnectClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
) {
    var searchVisible by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val navigationInset = with(density) { WindowInsets.navigationBars.getBottom(this).toDp() }

    Box(Modifier.fillMaxSize().background(Color.White)) {
        Column(Modifier.fillMaxSize()) {
            AppTopBar(
                onMenuClick = onMenuClick,
                onSearchClick = {
                    searchVisible = true
                    onSearchClick()
                },
            )

            if (searchVisible) {
                SearchAccountsField(
                    query = state.searchQuery,
                    onQueryChanged = onSearchChanged,
                    onClear = onClearSearch,
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 104.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                item {
                    AccountsHeading(
                        onSort = onSort,
                    )
                }
                items(items = state.filteredAccounts, key = { it.id }) { account ->
                    AccountRow(
                        account = account,
                        onClick = { onAccountClick(account) },
                        onFavoriteClick = { onToggleFavorite(account.id) },
                    )
                }
                if (state.filteredAccounts.isEmpty()) {
                    item { EmptyAccountsState(onAddAccount) }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 78.dp + navigationInset)
                .size(57.dp)
                .background(Color(0xFF703BE2), RoundedCornerShape(19.dp))
                .clickable(onClick = onAddAccount),
            contentAlignment = Alignment.Center,
        ) {
            Text("+", color = Color.White, fontSize = 31.sp, fontWeight = FontWeight.Medium)
        }

        AppBottomNav(
            activeTab = AppTab.ACCOUNTS,
            onAccountsClick = {},
            onLobbyClick = onLobbyClick,
            onConnectClick = onConnectClick,
            onSettingsClick = onSettingsClick,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun SearchAccountsField(
    query: String,
    onQueryChanged: (String) -> Unit,
    onClear: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 4.dp)
            .height(44.dp)
            .border(1.dp, Color(0xFFE6E2ED), RoundedCornerShape(14.dp))
            .background(Color(0xFFFAF9FC), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(AppSearchIcon, null, tint = Color(0xFF77717F), modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(9.dp))
        BasicTextField(
            value = query,
            onValueChange = onQueryChanged,
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, color = Color(0xFF17151D)),
            decorationBox = { innerTextField ->
                Box {
                    if (query.isEmpty()) Text("Search accounts", color = Color(0xFF77717F), fontSize = 14.sp)
                    innerTextField()
                }
            },
        )
        TextButton(onClick = onClear, contentPadding = PaddingValues(0.dp)) {
            Text("×", color = Color(0xFF77717F), fontSize = 22.sp)
        }
    }
}

@Composable
private fun AccountsHeading(onSort: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Column {
            Text("SECURE & PRIVATE", color = Color(0xFF7650D8), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.3.sp)
            Text("Your accounts", modifier = Modifier.padding(top = 1.dp), color = Color(0xFF17151D), fontSize = 19.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.25).sp)
        }
        Surface(onClick = onSort, shape = RoundedCornerShape(10.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E0ED))) {
            Text("Sort ↕", modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp), color = Color(0xFF5F566B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AccountRow(account: AccountItem, onClick: () -> Unit, onFavoriteClick: () -> Unit) {
    val seconds = account.secondsRemaining.coerceAtLeast(0)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFECE9F0)),
        shadowElevation = 2.dp,
    ) {
        Row(Modifier.fillMaxWidth().padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).background(Color(0xFFF5F3F8), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                Text(account.icon.ifBlank { account.name.firstOrNull()?.uppercase() ?: "?" }, color = account.serviceColor, fontSize = 21.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(account.name, color = Color(0xFF17151D), fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(account.email, Modifier.padding(top = 3.dp), color = Color(0xFF89838F), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(account.code, Modifier.padding(top = 2.dp), color = codeColor(seconds), fontSize = 23.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            TextButton(onClick = onFavoriteClick, contentPadding = PaddingValues(4.dp)) {
                Text(if (account.favorite) "★" else "☆", color = if (account.favorite) Color(0xFFF1A900) else Color(0xFFB7A8D3), fontSize = 19.sp)
            }
            CountdownRing(seconds, account.periodSeconds)
        }
    }
}

@Composable
private fun CountdownRing(secondsRemaining: Int, periodSeconds: Int) {
    val period = periodSeconds.coerceAtLeast(1)
    val progress = (secondsRemaining.toFloat() / period).coerceIn(0f, 1f)
    val ringColor = codeColor(secondsRemaining)
    Box(Modifier.size(35.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(ringBackground(secondsRemaining), style = androidx.compose.ui.graphics.drawscope.Stroke(3.dp.toPx()))
            drawArc(ringColor, -90f, progress * 360f, false, style = androidx.compose.ui.graphics.drawscope.Stroke(3.dp.toPx(), cap = StrokeCap.Round))
        }
        Text(secondsRemaining.toString(), color = ringColor, fontSize = 10.sp)
    }
}

@Composable
private fun EmptyAccountsState(onAddAccount: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(vertical = 70.dp, horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(64.dp).background(Color(0xFFF3EDFF), RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) {
            Text("+", color = Color(0xFF703BE2), fontSize = 30.sp)
        }
        Text("No accounts found", Modifier.padding(top = 14.dp), color = Color(0xFF17151D), fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Text("Add an account or try a different search.", Modifier.padding(top = 5.dp), color = Color(0xFF85808B), fontSize = 13.sp)
        TextButton(onClick = onAddAccount, Modifier.padding(top = 4.dp)) {
            Text("Add account", color = Color(0xFF703BE2), fontWeight = FontWeight.Bold)
        }
    }
}

private fun codeColor(seconds: Int): Color = when {
    seconds >= 15 -> Color(0xFF20883E)
    seconds >= 5 -> Color(0xFFAD8500)
    else -> Color(0xFFC62828)
}
private fun ringBackground(seconds: Int): Color = when {
    seconds >= 15 -> Color(0xFFD9F0DF)
    seconds >= 5 -> Color(0xFFF4EBC7)
    else -> Color(0xFFF3D7D7)
}
private val AccountItem.serviceColor: Color
    get() = when (serviceClass) {
        "github" -> Color(0xFF111111)
        "microsoft" -> Color(0xFFEF5B35)
        "binance" -> Color(0xFFD99C00)
        "dropbox" -> Color(0xFF1976FF)
        "zoho" -> Color(0xFFE42525)
        else -> Color(0xFF4285F4)
    }
