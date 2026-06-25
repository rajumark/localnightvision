package com.localnightimage.platform

import androidx.compose.runtime.Composable
import com.localnightimage.model.MlConfig

enum class ProcessingStatus {
    IDLE,
    LOADING_MODEL,
    PREPROCESSING,
    INFERENCE,
    POSTPROCESSING,
    COMPLETED,
    ERROR
}

data class ProcessingResult(
    val enhancedBytes: ByteArray,
    val processingTimeMs: Long
)

interface ImageProcessor {
    suspend fun initialize()
    suspend fun process(inputBytes: ByteArray, width: Int, height: Int): ProcessingResult
    fun getStatus(): ProcessingStatus
    fun release()
}

@Composable
expect fun rememberImageProcessor(): ImageProcessor
