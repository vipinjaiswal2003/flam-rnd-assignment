package com.flam.edgeviewer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.flam.edgeviewer.databinding.ActivityMainBinding
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {

    private lateinit var binding: ActivityMainBinding
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private var showEdges = true
    private var lastFrameTime = 0L
    private var frameCount = 0

    private val nativeBridge = NativeBridge()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startCamera()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }

        binding.toggleButton.setOnClickListener {
            showEdges = !showEdges
            binding.toggleButton.text = if (showEdges) "Show RAW" else "Show EDGES"
        }

        // Optionally attach GLView (basic OpenGL ES pipeline)
        val glView = GLView(this)
        binding.glContainer.addView(glView)

        try {
            System.loadLibrary("native-lib")
        } catch (e: UnsatisfiedLinkError) {
            Log.e("MainActivity", "Failed to load native-lib: ${e.message}")
        }
    }

    private fun allPermissionsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            val analyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(
                        cameraExecutor,
                        CameraAnalyzer { rgbaBytes, width, height ->
                            processFrame(rgbaBytes, width, height)
                        }
                    )
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, analyzer
                )
            } catch (exc: Exception) {
                Log.e("MainActivity", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun processFrame(rgbaBytes: ByteArray, width: Int, height: Int) {
        val now = System.currentTimeMillis()
        if (lastFrameTime == 0L) lastFrameTime = now
        frameCount++

        val processed = try {
            nativeBridge.processFrameRgba(rgbaBytes, width, height)
        } catch (e: UnsatisfiedLinkError) {
            null
        }

        if (processed != null && showEdges) {
            val bmp = NativeBridge.byteArrayToBitmap(processed, width, height, grayscale = true)
            runOnUiThread { binding.processedView.setImageBitmap(bmp) }
        } else if (!showEdges) {
            val bmp = NativeBridge.byteArrayToBitmap(rgbaBytes, width, height, grayscale = false)
            runOnUiThread { binding.processedView.setImageBitmap(bmp) }
        }

        val elapsed = now - lastFrameTime
        if (elapsed >= 1000L) {
            val fps = frameCount * 1000f / elapsed.toFloat()
            frameCount = 0
            lastFrameTime = now
            runOnUiThread {
                binding.fpsText.text = String.format("FPS: %.1f", fps)
            }
        }
    }
}
