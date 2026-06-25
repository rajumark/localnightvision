package com.localnightimage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.localnightimage.model.ImageBundle
import com.localnightimage.platform.rememberImageProcessor
import com.localnightimage.platform.rememberImagePickerLauncher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun CameraScreen(
    onImageProcessed: (ImageBundle) -> Unit
) {
    val scope = rememberCoroutineScope()
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var captureController by remember { mutableStateOf<CaptureController?>(null) }

    val imageProcessor = rememberImageProcessor()

    val imagePickerLauncher = rememberImagePickerLauncher { bytes ->
        scope.launch {
            processAndEnhance(bytes, imageProcessor, onImageProcessed) { isProcessing = it }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Local Night Vision",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
            }

            // Camera preview
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    onCaptureReady = { captureController = it }
                )

                if (isProcessing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = Color(0xFF6C63FF),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Enhancing image...",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Bottom controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 28.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gallery picker button
                FilledIconButton(
                    onClick = { imagePickerLauncher.launch() },
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color(0x33FFFFFF)
                    ),
                    enabled = !isProcessing
                ) {
                    Text("🖼", fontSize = 22.sp)
                }

                Spacer(modifier = Modifier.width(48.dp))

                // Capture button
                FilledIconButton(
                    onClick = {
                        scope.launch {
                            isProcessing = true
                            try {
                                val controller = captureController
                                if (controller != null) {
                                    val bytes = withContext(Dispatchers.Default) {
                                        controller.capture()
                                    }
                                    processAndEnhance(bytes, imageProcessor, onImageProcessed) {
                                        isProcessing = it
                                    }
                                } else {
                                    errorMessage = "Camera not ready"
                                    isProcessing = false
                                }
                            } catch (e: Exception) {
                                errorMessage = "Capture failed: ${e.message}"
                                isProcessing = false
                            }
                        }
                    },
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color.White
                    ),
                    enabled = !isProcessing
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDDDDDD))
                    )
                }

                Spacer(modifier = Modifier.width(48.dp))

                // Placeholder spacer for symmetry
                Spacer(modifier = Modifier.size(56.dp))
            }
        }

        // Error toast
        AnimatedVisibility(
            visible = errorMessage != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 64.dp)
        ) {
            Card(
                modifier = Modifier.padding(horizontal = 24.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE74C3C))
            ) {
                Text(
                    text = errorMessage ?: "",
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private suspend fun processAndEnhance(
    bytes: ByteArray,
    processor: com.localnightimage.platform.ImageProcessor,
    onProcessed: (ImageBundle) -> Unit,
    onLoading: (Boolean) -> Unit
) {
    onLoading(true)
    try {
        withContext(Dispatchers.Default) { processor.initialize() }
        val result = withContext(Dispatchers.Default) { processor.process(bytes, 0, 0) }
        onProcessed(
            ImageBundle(
                originalImageBytes = bytes,
                enhancedImageBytes = result.enhancedBytes
            )
        )
    } catch (e: Exception) {
        e.printStackTrace()
        onProcessed(
            ImageBundle(
                originalImageBytes = bytes,
                error = "Processing failed: ${e.message}"
            )
        )
    } finally {
        onLoading(false)
    }
}
