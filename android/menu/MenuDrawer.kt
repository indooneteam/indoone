package com.indoone.menu

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indoone.accounts.storage.AccountRepositoryProvider
import com.indoone.menu.dangerzone.DangerZoneScreen
import com.indoone.menu.logout.LogoutScreen
import com.indoone.menu.termsofuse.TermsOfUseScreen
import com.indoone.menu.trash.TrashScreen
import com.indoone.settings.applock.AppLockStore
import com.indoone.settings.unlock.UnlockAppActivity

@Composable
fun MenuDrawer(
    accountCount: Int,
    onDismiss: () -> Unit,
    onAccounts: () -> Unit,
    onFavorites: () -> Unit,
    onTrash: () -> Unit,
    onSecurity: () -> Unit,
    onTerms: () -> Unit,
    onPrivacy: () -> Unit,
    onAbout: () -> Unit,
    onLock: () -> Unit,
    onDangerZone: () -> Unit,
    onLogout: () -> Unit,
) {
    val context = LocalContext.current
    val repository = remember(context) { AccountRepositoryProvider(context.applicationContext) }
    var showTerms by remember { mutableStateOf(false) }
    var showDangerZone by remember { mutableStateOf(false) }
    var showLogout by remember { mutableStateOf(false) }
    var showTrash by remember { mutableStateOf(false) }

    if (showTerms) {
        TermsOfUseScreen(
            onBack = { showTerms = false },
            onAccountsClick = { showTerms = false; onAccounts() },
            onLobbyClick = { showTerms = false },
            onConnectClick = { showTerms = false },
            onSettingsClick = { showTerms = false; onDismiss() },
        )
        return
    }

    if (showDangerZone) {
        DangerZoneScreen(
            onBack = { showDangerZone = false },
            onAccountsClick = { showDangerZone = false; onAccounts() },
            onLobbyClick = { showDangerZone = false },
            onConnectClick = { showDangerZone = false },
            onSettingsClick = { showDangerZone = false; onDismiss() },
        )
        return
    }

    if (showTrash) {
        TrashScreen(
            repository = repository,
            onBack = { showTrash = false },
            onAccountsClick = { showTrash = false; onAccounts() },
            onLobbyClick = { showTrash = false },
            onConnectClick = { showTrash = false },
            onSettingsClick = { showTrash = false; onDismiss() },
        )
        return
    }

    if (showLogout) {
        LogoutScreen(
            onDismiss = { showLogout = false },
            onLoggedOut = {
                showLogout = false
                onDismiss()
                (context as? Activity)?.finishAndRemoveTask()
            },
        )
        return
    }

    Row(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .width(310.dp)
                .fillMaxHeight(),
            color = Color.White,
            shadowElevation = 16.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 25.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IndooneMenuLogo(modifier = Modifier.size(36.dp))

                    Column(modifier = Modifier.padding(start = 9.dp)) {
                        Text(
                            "Indoone",
                            color = Color(0xFF5E2DD2),
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 18.sp,
                        )
                        Text(
                            "Authenticator",
                            color = Color(0xFF77717D),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                Spacer(Modifier.height(17.dp))
                DrawerItem("▦", "All Accounts", if (accountCount > 0) accountCount.toString() else null, onAccounts)
                DrawerItem("☆", "Favorites", null, onFavorites)
                DrawerItem("♙", "Trash", null) { showTrash = true }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                    color = Color(0xFFEEEAF2),
                )

                DrawerItem("◈", "Security", null, onSecurity)
                DrawerItem("▤", "Terms of Use", null) { showTerms = true }
                DrawerItem("▥", "Privacy Policy", null, onPrivacy)
                DrawerItem("ⓘ", "About Indoone", null, onAbout)
                DrawerItem("▣", "Lock App", null) {
                    if (AppLockStore(context).isEnabled()) {
                        onDismiss()
                        context.startActivity(Intent(context, UnlockAppActivity::class.java))
                    } else {
                        onDismiss()
                        Toast.makeText(
                            context,
                            "App Lock is not enabled.",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
                DrawerItem("⚠", "Danger Zone", null) { showDangerZone = true }
                DrawerItem("⇥", "Log out", null) { showLogout = true }
            }
        }

        Spacer(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color.Black.copy(alpha = 0.33f))
                .clickable(onClick = onDismiss),
        )
    }
}

@Composable
private fun IndooneMenuLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val scaleFactor = size.minDimension / 48f
        scale(scaleFactor) {
            rotate(45f, pivot = Offset(24f, 24f)) {
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFC15CFF),
                            Color(0xFF7C3AED),
                            Color(0xFF22C7FF),
                        ),
                        start = Offset(11f, 11f),
                        end = Offset(37f, 37f),
                    ),
                    topLeft = Offset(11f, 11f),
                    size = Size(26f, 26f),
                    cornerRadius = CornerRadius(6f, 6f),
                )
            }

            val outer = Path().apply {
                moveTo(24f, 14f)
                lineTo(27.2f, 20.8f)
                lineTo(34f, 24f)
                lineTo(27.2f, 27.2f)
                lineTo(24f, 34f)
                lineTo(20.8f, 27.2f)
                lineTo(14f, 24f)
                lineTo(20.8f, 20.8f)
                close()
            }
            drawPath(outer, color = Color(0xFF0A0A18))

            val inner = Path().apply {
                moveTo(24f, 20.8f)
                lineTo(25.2f, 22.8f)
                lineTo(27.2f, 24f)
                lineTo(25.2f, 25.2f)
                lineTo(24f, 27.2f)
                lineTo(22.8f, 25.2f)
                lineTo(20.8f, 24f)
                lineTo(22.8f, 22.8f)
                close()
            }
            drawPath(inner, color = Color(0xFF60A5FA))
        }
    }
}

@Composable
private fun DrawerItem(
    icon: String,
    title: String,
    trailing: String?,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp)
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            icon,
            modifier = Modifier.width(32.dp),
            color = Color(0xFF413C48),
            fontSize = 19.sp,
        )
        Text(
            title,
            modifier = Modifier.weight(1f),
            color = Color(0xFF413C48),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        )
        trailing?.let {
            Surface(
                color = Color(0xFFF0EBFA),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text(
                    it,
                    color = Color(0xFF7041CE),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                )
            }
        }
    }
}
