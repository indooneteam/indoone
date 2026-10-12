package com.indoone.notifications

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Surface
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

@Composable
fun NotificationSettingsScreen(
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val preferences = remember { NotificationPreferences(context) }
    var masterEnabled by remember { mutableStateOf(preferences.isEnabled()) }
    var permissionGranted by remember { mutableStateOf(hasPermission(context)) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        permissionGranted = granted
        if (granted) preferences.markPermissionRequested()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White,
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF2C2733),
                )
            }
        }

        Column(Modifier.padding(horizontal = 18.dp, vertical = 8.dp)) {
            Text("NOTIFICATIONS", color = Color(0xFF2877E8), fontSize = 9.sp)
            Text(
                "Notifications",
                modifier = Modifier.padding(top = 3.dp),
                color = Color(0xFF1F1B24),
                fontSize = 26.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            )
            Text(
                "Control security, AI, Vibe, account and future integration alerts.",
                modifier = Modifier.padding(top = 7.dp),
                color = Color(0xFF77717F),
                fontSize = 12.sp,
                lineHeight = 19.sp,
            )
        }

        NotificationToggle(
            "Allow notifications",
            if (permissionGranted) "Notifications are enabled on this device."
            else "Android system notification permission is off.",
            masterEnabled && permissionGranted,
        ) { enabled ->
            if (enabled) {
                preferences.setEnabled(true)
                masterEnabled = true
                if (!permissionGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            } else {
                preferences.setEnabled(false)
                masterEnabled = false
            }
        }

        NotificationCategory.entries.forEach { category ->
            var enabled by remember(category) {
                mutableStateOf(preferences.isCategoryEnabled(category))
            }
            NotificationToggle(
                category.title,
                category.description,
                enabled && masterEnabled,
            ) { value ->
                preferences.setCategoryEnabled(category, value)
                enabled = value
            }
        }

        TextButton(
            onClick = { IndooneNotificationManager.showTest(context) },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        ) { Text("Send test notification") }

        TextButton(
            onClick = { openSystemNotificationSettings(context) },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        ) { Text("Open Android notification settings") }
        }
    }
}

@Composable
private fun NotificationToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!checked) }
                .padding(horizontal = 18.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    color = Color(0xFF2C2733),
                    fontSize = 13.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                )
                Text(
                    subtitle,
                    modifier = Modifier.padding(top = 3.dp),
                    color = Color(0xFF8A8392),
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF2877E8),
                    checkedBorderColor = Color(0xFF2877E8),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFE9E6EC),
                    uncheckedBorderColor = Color(0xFFB8B2BE),
                ),
            )
        }
        HorizontalDivider(color = Color(0xFFEEE8F4), thickness = 1.dp)
    }
}

private fun hasPermission(context: Context): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED

private fun openSystemNotificationSettings(context: Context) {
    context.startActivity(
        Intent(
            Settings.ACTION_APP_NOTIFICATION_SETTINGS,
            Uri.parse("package:" + context.packageName),
        ),
    )
}
