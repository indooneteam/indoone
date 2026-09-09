package com.indoone.menu

import android.app.Activity
import android.content.Intent
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
import androidx.compose.ui.graphics.Color
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
                    Surface(
                        color = Color(0xFF5E2CE2),
                        shape = RoundedCornerShape(11.dp),
                    ) {
                        Text(
                            "I",
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        )
                    }
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
                        onLock()
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
