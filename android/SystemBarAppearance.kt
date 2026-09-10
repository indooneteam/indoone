package com.indoone

import android.app.Activity
import android.os.Build
import android.view.Window
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

fun Activity.configureIndooneSystemBars() {
    val window: Window = window
    window.statusBarColor = android.graphics.Color.WHITE
    window.navigationBarColor = android.graphics.Color.WHITE
    WindowCompat.setDecorFitsSystemWindows(window, true)
    val controller = WindowInsetsControllerCompat(window, window.decorView)
    controller.isAppearanceLightStatusBars = true
    controller.isAppearanceLightNavigationBars = true
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        window.isNavigationBarContrastEnforced = false
    }
}
