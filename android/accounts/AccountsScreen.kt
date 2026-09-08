package com.indoone.accounts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions

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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AccountsTopBar(
                onMenuClick = onMenuClick,
                onSearchClick = onSearchClick,
            )

            SearchAccountsField(
                query = state.searchQuery,
                onQueryChanged = onSearchChanged,
                onClear = onClearSearch,
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 18.dp,
                    end = 18.dp,
                    top = 12.dp,
                    bottom = 104.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                item {
                    AccountsHeading(
                        count = state.filteredAccounts.size,
                        sortAscending = state.sortAscending,
                        onSort = onSort,
                    )
                }

                items(
                    items = state.filteredAccounts,
                    key = { it.id },
                ) { account ->
                    AccountRow(
                        account = account,
                        onClick = { onAccountClick(account) },
                        onFavoriteClick = { onToggleFavorite(account.id) },
                    )
                }

                if (state.filteredAccounts.isEmpty()) {
                    item {
                        EmptyAccountsState(
                            hasSearch = state.searchQuery.isNotBlank(),
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddAccount,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 80.dp)
                .size(57.dp),
            shape = RoundedCornerShape(19.dp),
            containerColor = Color(0xFF703BE2),
            contentColor = Color.White,
        ) {
            Text(
                text = "+",
                fontSize = 31.sp,
                fontWeight = FontWeight.Medium,
            )
        }

        AccountsBottomNav(
            modifier = Modifier.align(Alignment.BottomCenter),
            onAccountsClick = {},
            onLobbyClick = onLobbyClick,
            onConnectClick = onConnectClick,
            onSettingsClick = onSettingsClick,
        )
    }
}

@Composable
private fun AccountsTopBar(
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = onMenuClick) {
                Text(
                    text = "☰",
                    color = Color(0xFF242129),
                    fontSize = 21.sp,
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                modifier = Modifier.clickable(onClick = onSearchClick),
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(
                            color = Color(0xFF6D35E8),
                            shape = RoundedCornerShape(10.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "✦",
                        color = Color.White,
                        fontSize = 16.sp,
                    )
                }
                Text(
                    text = "Indoone",
                    color = Color(0xFF5E2DD2),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            IconButton(onClick = onSearchClick) {
                Text(
                    text = "⌕",
                    color = Color(0xFF242129),
                    fontSize = 25.sp,
                )
            }
        }

        HorizontalDivider(color = Color(0xFFF0EEF5))
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
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChanged,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = {
                Text(
                    text = "Search accounts",
                    color = Color(0xFF77717F),
                )
            },
            leadingIcon = {
                Text(
                    text = "⌕",
                    color = Color(0xFF77717F),
                    fontSize = 20.sp,
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    TextButton(onClick = onClear) {
                        Text(
                            text = "×",
                            color = Color(0xFF77717F),
                            fontSize = 20.sp,
                        )
                    }
                }
            },
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        )
    }
}

@Composable
private fun AccountsHeading(
    count: Int,
    sortAscending: Boolean,
    onSort: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 1.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Column {
            Text(
                text = "SECURE & PRIVATE",
                color = Color(0xFF7650D8),
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.3.sp,
            )
            Text(
                text = "Your accounts",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.25).sp,
            )
        }

        Surface(
            onClick = onSort,
            shape = RoundedCornerShape(10.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE5E0ED)),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = "Sort",
                    color = Color(0xFF5F566B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = if (sortAscending) "↕" else "↕",
                    color = Color(0xFF5F566B),
                    fontSize = 15.sp,
                )
            }
        }
    }
}

@Composable
private fun AccountRow(
    account: AccountItem,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFECE9F0)),
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = Color(0xFFF5F3F8),
                        shape = RoundedCornerShape(14.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = account.name.firstOrNull()?.uppercase() ?: "A",
                    color = Color(0xFF4285F4),
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(Modifier.size(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = account.email,
                    modifier = Modifier.padding(top = 3.dp),
                    color = Color(0xFF89838F),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = account.code,
                    modifier = Modifier.padding(top = 4.dp),
                    color = codeColor(account.secondsRemaining),
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                )
            }

            TextButton(onClick = onFavoriteClick) {
                Text(
                    text = if (account.favorite) "★" else "☆",
                    color = if (account.favorite) Color(0xFFF1A900) else Color(0xFFB7A8D3),
                    fontSize = 22.sp,
                )
            }

            CountdownRing(
                secondsRemaining = account.secondsRemaining,
                periodSeconds = account.periodSeconds,
            )
        }
    }
}

@Composable
private fun CountdownRing(
    secondsRemaining: Int,
    periodSeconds: Int,
) {
    val safePeriod = periodSeconds.coerceAtLeast(1)
    val progress = (secondsRemaining.toFloat() / safePeriod).coerceIn(0f, 1f)
    val ringColor = codeColor(secondsRemaining)

    Box(
        modifier = Modifier.size(35.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color(0xFFEADFFB),
                style = Stroke(width = 3.dp.toPx()),
            )
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = progress * 360f,
                useCenter = false,
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                ),
            )
        }
        Text(
            text = secondsRemaining.coerceAtLeast(0).toString(),
            color = ringColor,
            fontSize = 10.sp,
        )
    }
}

@Composable
private fun EmptyAccountsState(hasSearch: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 56.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    color = Color(0xFFF3EDFF),
                    shape = RoundedCornerShape(20.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "+",
                color = Color(0xFF703BE2),
                fontSize = 30.sp,
            )
        }
        Text(
            text = if (hasSearch) "No matching accounts" else "No accounts yet",
            modifier = Modifier.padding(top = 14.dp),
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = if (hasSearch) {
                "Try a different account name or email."
            } else {
                "Add an authenticator account to get started."
            },
            modifier = Modifier.padding(top = 5.dp),
            color = Color(0xFF85808B),
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun AccountsBottomNav(
    modifier: Modifier = Modifier,
    onAccountsClick: () -> Unit,
    onLobbyClick: () -> Unit,
    onConnectClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFEEEAF2)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(67.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BottomNavItem(
                modifier = Modifier.weight(1f),
                icon = "♟",
                label = "Accounts",
                active = true,
                onClick = onAccountsClick,
            )
            BottomNavItem(
                modifier = Modifier.weight(1f),
                icon = "◆",
                label = "Lobby",
                active = false,
                onClick = onLobbyClick,
            )
            BottomNavItem(
                modifier = Modifier.weight(1f),
                icon = "↔",
                label = "Connect",
                active = false,
                onClick = onConnectClick,
            )
            BottomNavItem(
                modifier = Modifier.weight(1f),
                icon = "☷",
                label = "Settings",
                active = false,
                onClick = onSettingsClick,
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    modifier: Modifier,
    icon: String,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    val contentColor = if (active) Color(0xFF6B34DF) else Color(0xFF99939F)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(67.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = icon,
            color = contentColor,
            fontSize = 21.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
        )
        Text(
            text = label,
            modifier = Modifier.padding(top = 2.dp),
            color = contentColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

private fun codeColor(secondsRemaining: Int): Color = when {
    secondsRemaining <= 5 -> Color(0xFFD93025)
    secondsRemaining <= 10 -> Color(0xFFD7A500)
    else -> Color(0xFF24A148)
}
