package com.localnightimage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.localnightimage.model.ImageBundle
import com.localnightimage.ui.theme.LocalNightVisionTheme

@Composable
fun App() {
    LocalNightVisionTheme(darkTheme = true) {
        var imageBundle by remember { mutableStateOf<ImageBundle?>(null) }
        var permissionGranted by remember { mutableStateOf(false) }

        if (!permissionGranted) {
            PermissionScreen(
                onGranted = { permissionGranted = true },
                onDenied = { /* stay on permission screen */ }
            )
        } else {
            AnimatedVisibility(
                visible = imageBundle == null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                CameraScreen(
                    onImageProcessed = { bundle ->
                        imageBundle = bundle
                    }
                )
            }

            imageBundle?.let { bundle ->
                ResultScreen(
                    imageBundle = bundle,
                    onDismiss = {
                        imageBundle = null
                    }
                )
            }
        }
    }
}
