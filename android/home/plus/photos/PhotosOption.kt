package com.indoone.home.plus.photos

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.runtime.Composable
import com.indoone.home.plus.PlusMenuOption

@Composable
fun PhotosOption(onClick: () -> Unit) {
    PlusMenuOption(
        title = "Photos",
        icon = Icons.Outlined.Photo,
        onClick = onClick,
    )
}
