package com.localnightimage.platform

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.localnightimage.model.MlConfig
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.channels.FileChannel

@Composable
actual fun rememberImageProcessor(): ImageProcessor {
    val context = LocalContext.current
    return remember { AndroidImageProcessor(context) }
}

class AndroidImageProcessor(
    private val context: Context
) : ImageProcessor {

    private var interpreter: Interpreter? = null
    private var status = ProcessingStatus.IDLE

    override suspend fun initialize() {
        status = ProcessingStatus.LOADING_MODEL
        try {
            val modelBuffer = loadModelFile()
            val options = Interpreter.Options().apply {
                setNumThreads(4)
                val compatList = CompatibilityList()
                if (compatList.isDelegateSupportedOnThisDevice) {
                    addDelegate(GpuDelegate())
                }
            }
            interpreter = Interpreter(modelBuffer, options)
            status = ProcessingStatus.IDLE
        } catch (e: Exception) {
            status = ProcessingStatus.ERROR
            throw e
        }
    }

    override suspend fun process(inputBytes: ByteArray, width: Int, height: Int): ProcessingResult {
        val startTime = System.currentTimeMillis()
        status = ProcessingStatus.PREPROCESSING

        val interp = interpreter ?: throw IllegalStateException("Model not initialized")

        val inputBitmap = BitmapFactory.decodeByteArray(inputBytes, 0, inputBytes.size)
            ?: throw IllegalArgumentException("Failed to decode image bytes")

        val resizedBitmap = Bitmap.createScaledBitmap(
            inputBitmap, MlConfig.MODEL_INPUT_SIZE, MlConfig.MODEL_INPUT_SIZE, true
        )

        status = ProcessingStatus.INFERENCE

        val inputBuffer = convertBitmapToFloatBuffer(resizedBitmap)

        val outputArray = Array(1) {
            Array(MlConfig.MODEL_INPUT_SIZE) {
                Array(MlConfig.MODEL_INPUT_SIZE) {
                    FloatArray(MlConfig.MODEL_CHANNELS)
                }
            }
        }

        interp.run(inputBuffer, outputArray)

        status = ProcessingStatus.POSTPROCESSING

        val outputBitmap = convertArrayToBitmap(outputArray[0])
        val outputStream = java.io.ByteArrayOutputStream()
        outputBitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)

        val processingTime = System.currentTimeMillis() - startTime
        status = ProcessingStatus.COMPLETED

        return ProcessingResult(
            enhancedBytes = outputStream.toByteArray(),
            processingTimeMs = processingTime
        )
    }

    override fun getStatus(): ProcessingStatus = status

    override fun release() {
        interpreter?.close()
        interpreter = null
        status = ProcessingStatus.IDLE
    }

    private fun loadModelFile(): ByteBuffer {
        val assetFileDescriptor = context.assets.openFd(MlConfig.MODEL_FILE)
        val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        val modelBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        modelBuffer.order(ByteOrder.nativeOrder())
        fileChannel.close()
        inputStream.close()
        return modelBuffer
    }

    private fun convertBitmapToFloatBuffer(bitmap: Bitmap): FloatBuffer {
        val buffer = ByteBuffer.allocateDirect(
            MlConfig.MODEL_INPUT_SIZE * MlConfig.MODEL_INPUT_SIZE * MlConfig.MODEL_CHANNELS * 4
        ).order(ByteOrder.nativeOrder()).asFloatBuffer()

        val pixels = IntArray(MlConfig.MODEL_INPUT_SIZE * MlConfig.MODEL_INPUT_SIZE)
        bitmap.getPixels(pixels, 0, MlConfig.MODEL_INPUT_SIZE, 0, 0,
            MlConfig.MODEL_INPUT_SIZE, MlConfig.MODEL_INPUT_SIZE)

        for (pixel in pixels) {
            buffer.put(((pixel shr 16) and 0xFF) / 255.0f)
            buffer.put(((pixel shr 8) and 0xFF) / 255.0f)
            buffer.put((pixel and 0xFF) / 255.0f)
        }

        buffer.rewind()
        return buffer
    }

    private fun convertArrayToBitmap(array: Array<Array<FloatArray>>): Bitmap {
        val size = MlConfig.MODEL_INPUT_SIZE
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(size * size)

        for (y in 0 until size) {
            for (x in 0 until size) {
                val r = (array[y][x][0] * 255).coerceIn(0f, 255f).toInt()
                val g = (array[y][x][1] * 255).coerceIn(0f, 255f).toInt()
                val b = (array[y][x][2] * 255).coerceIn(0f, 255f).toInt()
                pixels[y * size + x] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
            }
        }

        bitmap.setPixels(pixels, 0, size, 0, 0, size, size)
        return bitmap
    }
}
