package com.indoone.home.plus.camera

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.runtime.Composable
import com.indoone.home.plus.PlusMenuOption

@Composable
fun CameraOption(onClick: () -> Unit) {
    PlusMenuOption(
        title = "Camera",
        icon = Icons.Outlined.PhotoCamera,
        onClick = onClick,
    )
}
