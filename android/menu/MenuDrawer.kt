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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
        Spacer(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color.Black.copy(alpha = 0.30f))
                .clickable(onClick = onDismiss),
        )
        Surface(
            modifier = Modifier
                .width(320.dp)
                .fillMaxHeight(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 16.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 20.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        color = Color(0xFF6D35E8),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                    ) {
                        Text("I", color = Color.White, modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp))
                    }
                    Column(modifier = Modifier.padding(start = 10.dp)) {
                        Text("Indoone", style = MaterialTheme.typography.titleLarge)
                        Text("Authenticator", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(Modifier.padding(top = 10.dp))
                DrawerItem("▦", "All Accounts", if (accountCount > 0) accountCount.toString() else null, onAccounts)
                DrawerItem("☆", "Favorites", null, onFavorites)
                DrawerItem("♙", "Trash", null) { showTrash = true }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

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
            .padding(horizontal = 20.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(icon, modifier = Modifier.width(32.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        trailing?.let {
            Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
