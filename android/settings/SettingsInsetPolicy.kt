package com.indoone.settings

import androidx.compose.ui.Modifier

/**
 * Settings is hosted in the main Activity, where IndooneApplication already
 * applies the status-bar inset to the Compose content root. Keep the legacy
 * safeDrawingPadding call in SettingsScreen harmless so the header is not
 * pushed down a second time.
 */
@Suppress("UNUSED_PARAMETER")
fun Modifier.safeDrawingPadding(): Modifier = this
