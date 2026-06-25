package com.localnightimage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.localnightimage.model.ImageBundle

@Composable
fun ResultScreen(
    imageBundle: ImageBundle,
    onDismiss: () -> Unit
) {
    var showOriginal by remember { mutableStateOf(false) }

    val originalBitmap = remember(imageBundle.originalImageBytes) {
        bytesToImageBitmap(imageBundle.originalImageBytes)
    }

    val enhancedBitmap = remember(imageBundle.enhancedImageBytes) {
        imageBundle.enhancedImageBytes?.let { bytesToImageBitmap(it) }
    }

    val displayBitmap = when {
        showOriginal && originalBitmap != null -> originalBitmap
        enhancedBitmap != null -> enhancedBitmap
        originalBitmap != null -> originalBitmap
        else -> null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Result",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                if (enhancedBitmap != null) {
                    Text(
                        text = if (showOriginal) "Original" else "Enhanced",
                        color = Color(0xFF6C63FF),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Image with touch-to-compare
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF1A1A1A))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                if (enhancedBitmap != null) {
                                    showOriginal = true
                                    tryAwaitRelease()
                                    showOriginal = false
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (displayBitmap != null) {
                    Image(
                        painter = BitmapPainter(displayBitmap),
                        contentDescription = if (showOriginal) "Original image" else "Enhanced image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }

                if (imageBundle.isProcessing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            androidx.compose.material3.CircularProgressIndicator(
                                color = Color(0xFF6C63FF),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Processing...", color = Color.White, fontSize = 16.sp)
                        }
                    }
                }

                if (imageBundle.error != null) {
                    Text(
                        text = imageBundle.error,
                        color = Color(0xFFE74C3C),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }

            // Touch-to-compare hint
            if (enhancedBitmap != null) {
                Text(
                    text = "Hold to compare with original",
                    color = Color(0x99FFFFFF),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )
            }

            // Bottom buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 28.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button
                FilledIconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color(0x33FFFFFF)
                    )
                ) {
                    Text("✕", fontSize = 22.sp, color = Color.White)
                }

                Spacer(modifier = Modifier.width(48.dp))

                // Save button (placeholder - would trigger platform save)
                FilledIconButton(
                    onClick = { /* TODO: save to gallery */ },
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color(0xFF6C63FF)
                    )
                ) {
                    Text("💾", fontSize = 22.sp)
                }

                Spacer(modifier = Modifier.width(48.dp))

                // Share button placeholder
                FilledIconButton(
                    onClick = { /* TODO: share */ },
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color(0x33FFFFFF)
                    )
                ) {
                    Text("↗", fontSize = 22.sp, color = Color.White)
                }
            }
        }
    }
}

internal expect fun bytesToImageBitmap(bytes: ByteArray): ImageBitmap?
