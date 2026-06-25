package com.localnightimage.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberImageProcessor(): ImageProcessor {
    return remember { IosImageProcessor() }
}

class IosImageProcessor : ImageProcessor {

    private var status = ProcessingStatus.IDLE

    override suspend fun initialize() {
        status = ProcessingStatus.IDLE
    }

    override suspend fun process(inputBytes: ByteArray, width: Int, height: Int): ProcessingResult {
        throw NotImplementedError("iOS ML processing - implement via TFLite C interop")
    }

    override fun getStatus(): ProcessingStatus = status

    override fun release() {}
}
