package com.localnightimage.model

data class ImageBundle(
    val originalImageBytes: ByteArray,
    val enhancedImageBytes: ByteArray? = null,
    val isProcessing: Boolean = false,
    val error: String? = null
) {
    fun hasEnhanced(): Boolean = enhancedImageBytes != null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as ImageBundle
        return originalImageBytes.contentEquals(other.originalImageBytes) &&
                enhancedImageBytes.contentEquals(other.enhancedImageBytes) &&
                isProcessing == other.isProcessing && error == other.error
    }

    override fun hashCode(): Int {
        var result = originalImageBytes.contentHashCode()
        result = 31 * result + (enhancedImageBytes?.contentHashCode() ?: 0)
        result = 31 * result + isProcessing.hashCode()
        result = 31 * result + (error?.hashCode() ?: 0)
        return result
    }
}

enum class CaptureMode {
    CAMERA,
    GALLERY
}
