package com.flam.edgeviewer

import android.graphics.Bitmap
import java.nio.ByteBuffer

class NativeBridge {

    external fun processFrameRgba(data: ByteArray, width: Int, height: Int): ByteArray

    companion object {
        fun byteArrayToBitmap(bytes: ByteArray, width: Int, height: Int, grayscale: Boolean): Bitmap {
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val buffer = ByteBuffer.allocate(width * height * 4)

            if (grayscale) {
                var i = 0
                for (y in 0 until height) {
                    for (x in 0 until width) {
                        val g = bytes[i].toInt() and 0xFF
                        buffer.put(g.toByte())
                        buffer.put(g.toByte())
                        buffer.put(g.toByte())
                        buffer.put(0xFF.toByte())
                        i++
                    }
                }
            } else {
                buffer.put(bytes)
            }

            buffer.rewind()
            bmp.copyPixelsFromBuffer(buffer)
            return bmp
        }
    }
}
