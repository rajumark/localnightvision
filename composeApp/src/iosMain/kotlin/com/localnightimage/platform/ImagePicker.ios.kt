package com.localnightimage.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

class IosImagePickerLauncher(
    private val onImagePicked: (ByteArray) -> Unit
) : ImagePickerLauncher {
    override fun launch() {
        // iOS implementation using UIImagePickerController
        // Will be implemented via UIKit interop
        throw NotImplementedError("iOS image picker - implement via UIKit interop")
    }
}

@Composable
actual fun rememberImagePickerLauncher(onImagePicked: (ByteArray) -> Unit): ImagePickerLauncher {
    return remember { IosImagePickerLauncher(onImagePicked) }
}
