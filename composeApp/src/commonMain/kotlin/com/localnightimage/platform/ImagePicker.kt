package com.localnightimage.platform

import androidx.compose.runtime.Composable

@Composable
expect fun rememberImagePickerLauncher(onImagePicked: (ByteArray) -> Unit): ImagePickerLauncher

interface ImagePickerLauncher {
    fun launch()
}
