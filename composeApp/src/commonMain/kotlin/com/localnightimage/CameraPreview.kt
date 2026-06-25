package com.localnightimage

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun CameraPreview(
    modifier: Modifier,
    onCaptureReady: (CaptureController) -> Unit
)

interface CaptureController {
    suspend fun capture(): ByteArray
}
