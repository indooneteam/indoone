package com.indoone.home.plus.files

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.runtime.Composable
import com.indoone.home.plus.PlusMenuOption

@Composable
fun FilesOption(onClick: () -> Unit) {
    PlusMenuOption(
        title = "Files",
        icon = Icons.Outlined.AttachFile,
        subtitle = "PDFs, Docs, Videos, Audio, and more",
        onClick = onClick,
    )
}
