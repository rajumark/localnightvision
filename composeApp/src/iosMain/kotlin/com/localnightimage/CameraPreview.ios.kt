package com.localnightimage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class IosCaptureController : CaptureController {
    override suspend fun capture(): ByteArray {
        // iOS implementation using AVCapturePhotoOutput
        // Requires UIKit interop setup
        throw NotImplementedError("iOS capture - implement via AVFoundation interop")
    }
}

@Composable
actual fun CameraPreview(
    modifier: Modifier,
    onCaptureReady: (CaptureController) -> Unit
) {
    val controller = remember { IosCaptureController() }
    onCaptureReady(controller)

    // Placeholder: iOS will use UIKitView with AVCaptureVideoPreviewLayer
    // For now, shows a placeholder
    androidx.compose.foundation.layout.Box(
        modifier = modifier,
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.material3.Text(
            text = "Camera Preview (iOS)",
            color = androidx.compose.ui.graphics.Color.White
        )
    }
}
