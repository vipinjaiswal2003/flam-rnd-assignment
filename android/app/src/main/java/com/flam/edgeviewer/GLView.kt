package com.flam.edgeviewer

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet

class GLView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : GLSurfaceView(context, attrs) {

    private val renderer: GLRenderer

    init {
        setEGLContextClientVersion(2)
        renderer = GLRenderer()
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    fun updateFrameTexture(pixels: ByteArray, width: Int, height: Int) {
        renderer.updateTexture(pixels, width, height)
        requestRender()
    }
}
