package com.localnightimage.platform

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

class AndroidImagePickerLauncher(
    private val launcher: androidx.activity.result.ActivityResultLauncher<String>,
    private val context: android.content.Context
) : ImagePickerLauncher {
    override fun launch() {
        launcher.launch("image/*")
    }

    fun handleResult(uri: Uri?, onImagePicked: (ByteArray) -> Unit) {
        uri ?: return
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            if (bytes != null) {
                onImagePicked(bytes)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@Composable
actual fun rememberImagePickerLauncher(onImagePicked: (ByteArray) -> Unit): ImagePickerLauncher {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()
                if (bytes != null) {
                    onImagePicked(bytes)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    return remember { AndroidImagePickerLauncher(launcher, context) }
}
