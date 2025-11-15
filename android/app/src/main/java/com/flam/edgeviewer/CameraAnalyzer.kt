package com.flam.edgeviewer

import android.graphics.ImageFormat
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer
import kotlin.math.max
import kotlin.math.min

class CameraAnalyzer(
    private val onFrame: (rgbaBytes: ByteArray, width: Int, height: Int) -> Unit
) : ImageAnalysis.Analyzer {

    override fun analyze(image: ImageProxy) {
        if (image.format != ImageFormat.YUV_420_888) {
            image.close()
            return
        }

        val width = image.width
        val height = image.height

        val yPlane = image.planes[0].buffer.toByteArray()
        val rgba = ByteArray(width * height * 4)

        for (i in 0 until width * height) {
            val y = yPlane[i].toInt() and 0xFF
            val clamped = min(255, max(0, y))
            val base = i * 4
            rgba[base] = clamped.toByte()
            rgba[base + 1] = clamped.toByte()
            rgba[base + 2] = clamped.toByte()
            rgba[base + 3] = 0xFF.toByte()
        }

        onFrame(rgba, width, height)
        image.close()
    }

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()
        val data = ByteArray(remaining())
        get(data)
        return data
    }
}
